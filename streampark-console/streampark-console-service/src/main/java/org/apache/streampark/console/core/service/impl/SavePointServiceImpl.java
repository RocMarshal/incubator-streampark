/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.streampark.console.core.service.impl;

import org.apache.streampark.common.util.AssertUtils;
import org.apache.streampark.common.util.HttpClientUtils;
import org.apache.streampark.console.base.domain.Constant;
import org.apache.streampark.console.base.domain.RestRequest;
import org.apache.streampark.console.base.exception.InternalException;
import org.apache.streampark.console.base.mybatis.pager.MybatisPager;
import org.apache.streampark.console.base.util.CommonUtils;
import org.apache.streampark.console.core.entity.Application;
import org.apache.streampark.console.core.entity.FlinkEnv;
import org.apache.streampark.console.core.entity.SavePoint;
import org.apache.streampark.console.core.enums.CheckPointType;
import org.apache.streampark.console.core.mapper.ApplicationMapper;
import org.apache.streampark.console.core.mapper.SavePointMapper;
import org.apache.streampark.console.core.service.FlinkEnvService;
import org.apache.streampark.console.core.service.SavePointService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.runtime.rest.handler.async.TriggerResponse;
import org.apache.flink.runtime.rest.messages.job.savepoints.SavepointTriggerRequestBody;
import org.apache.flink.runtime.rest.util.RestMapperUtils;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;

@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SavePointServiceImpl extends ServiceImpl<SavePointMapper, SavePoint>
    implements SavePointService {

  private static final ObjectMapper objMapper = RestMapperUtils.getStrictObjectMapper();

  @Autowired private FlinkEnvService flinkEnvService;

  @Autowired private ApplicationMapper appMapper;

  @Override
  public void expire(Long appId) {
    SavePoint savePoint = new SavePoint();
    savePoint.setLatest(false);
    LambdaQueryWrapper<SavePoint> queryWrapper =
        new LambdaQueryWrapper<SavePoint>().eq(SavePoint::getAppId, appId);
    this.update(savePoint, queryWrapper);
  }

  @Override
  public boolean save(SavePoint entity) {
    this.expire(entity);
    this.expire(entity.getAppId());
    return super.save(entity);
  }

  private void expire(SavePoint entity) {
    FlinkEnv flinkEnv = flinkEnvService.getByAppId(entity.getAppId());
    AssertUtils.state(flinkEnv != null);
    int cpThreshold =
        Integer.parseInt(
            flinkEnv.convertFlinkYamlAsMap().getOrDefault("state.checkpoints.num-retained", "5"));

    if (CheckPointType.CHECKPOINT.equals(CheckPointType.of(entity.getType()))) {
      cpThreshold = cpThreshold - 1;
    }

    if (cpThreshold == 0) {
      LambdaQueryWrapper<SavePoint> queryWrapper =
          new LambdaQueryWrapper<SavePoint>()
              .eq(SavePoint::getAppId, entity.getAppId())
              .eq(SavePoint::getType, 1);
      this.remove(queryWrapper);
    } else {
      LambdaQueryWrapper<SavePoint> queryWrapper =
          new LambdaQueryWrapper<SavePoint>()
              .select(SavePoint::getTriggerTime)
              .eq(SavePoint::getAppId, entity.getAppId())
              .eq(SavePoint::getType, CheckPointType.CHECKPOINT.get())
              .orderByDesc(SavePoint::getTriggerTime);

      Page<SavePoint> savePointPage =
          this.baseMapper.selectPage(new Page<>(1, cpThreshold + 1), queryWrapper);
      if (!savePointPage.getRecords().isEmpty()
          && savePointPage.getRecords().size() > cpThreshold) {
        SavePoint savePoint = savePointPage.getRecords().get(cpThreshold - 1);
        LambdaQueryWrapper<SavePoint> lambdaQueryWrapper =
            new LambdaQueryWrapper<SavePoint>()
                .eq(SavePoint::getAppId, entity.getAppId())
                .eq(SavePoint::getType, 1)
                .lt(SavePoint::getTriggerTime, savePoint.getTriggerTime());
        this.remove(lambdaQueryWrapper);
      }
    }
  }

  @Override
  public SavePoint getLatest(Long id) {
    LambdaQueryWrapper<SavePoint> queryWrapper =
        new LambdaQueryWrapper<SavePoint>()
            .eq(SavePoint::getAppId, id)
            .eq(SavePoint::getLatest, true);
    return this.getOne(queryWrapper);
  }

  @Override
  public Boolean triggerSavepoint(Long appId, @Nullable String savepointPath) {
    log.info("Start to trigger savepoint for app {}", appId);
    Application app = appMapper.selectById(appId);
    Preconditions.checkState(app != null, "The application %s doesn't exist.", appId);
    String trackingUrl = app.getJobManagerUrl();
    Preconditions.checkState(
        StringUtils.isNotEmpty(trackingUrl), "The flink trackingUrl for app[%] isn't available.");
    String jobId = app.getJobId();
    Preconditions.checkState(
        StringUtils.isNotEmpty(jobId), "The jobId of application[%s] is absent.", appId);
    String triggerId = triggerSavepoint(trackingUrl, jobId, savepointPath);
    log.info("Request savepoint successful in triggerId {}", triggerId);
    return true;
  }

  private String triggerSavepoint(
      String trackingUrl, String jobId, @Nullable String savepointPath) {
    String response = null;
    try {
      String url = String.format("%s/jobs/%s/savepoints", trackingUrl, jobId);
      String postBody =
          objMapper.writeValueAsString(new SavepointTriggerRequestBody(savepointPath, false));
      response = HttpClientUtils.httpPostRequest(url, postBody);
      TriggerResponse triggerResponse = objMapper.readValue(response, TriggerResponse.class);
      return triggerResponse.getTriggerId().toString();
    } catch (Exception e) {
      throw new RuntimeException(
          String.format(
              "Fail to trigger savepoint, trackingUrl = %s, response = %s", trackingUrl, response),
          e);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean delete(Long id, Application application) throws InternalException {
    SavePoint savePoint = getById(id);
    try {
      if (CommonUtils.notEmpty(savePoint.getPath())) {
        application.getFsOperator().delete(savePoint.getPath());
      }
      removeById(id);
      return true;
    } catch (Exception e) {
      throw new InternalException(e.getMessage());
    }
  }

  @Override
  public IPage<SavePoint> page(SavePoint savePoint, RestRequest request) {
    Page<SavePoint> page =
        new MybatisPager<SavePoint>().getPage(request, "trigger_time", Constant.ORDER_DESC);
    LambdaQueryWrapper<SavePoint> queryWrapper =
        new LambdaQueryWrapper<SavePoint>().eq(SavePoint::getAppId, savePoint.getAppId());
    return this.page(page, queryWrapper);
  }

  @Override
  public void removeApp(Application application) {
    Long appId = application.getId();

    LambdaQueryWrapper<SavePoint> queryWrapper =
        new LambdaQueryWrapper<SavePoint>().eq(SavePoint::getAppId, appId);
    this.remove(queryWrapper);

    try {
      application
          .getFsOperator()
          .delete(application.getWorkspace().APP_SAVEPOINTS().concat("/").concat(appId.toString()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
