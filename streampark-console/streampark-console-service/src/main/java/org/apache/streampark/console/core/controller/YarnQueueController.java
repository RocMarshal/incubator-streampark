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

package org.apache.streampark.console.core.controller;

import org.apache.streampark.console.base.domain.RestRequest;
import org.apache.streampark.console.base.domain.RestResponse;
import org.apache.streampark.console.core.annotation.ApiAccess;
import org.apache.streampark.console.core.bean.ResponseResult;
import org.apache.streampark.console.core.entity.YarnQueue;
import org.apache.streampark.console.core.service.YarnQueueService;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("flink/yarnQueue")
public class YarnQueueController {

  @Autowired private YarnQueueService yarnQueueService;

  /**
   * * List the queues in the specified team by the paging & optional search hint message.
   *
   * @param teamId team id
   * @param restRequest page request information.
   * @param yarnQueue optional fields used to search.
   * @return RestResponse with IPage<{@link YarnQueue}> object.
   */
  @ApiAccess
  @ApiOperation(value = "Trigger savepoint for specified application by id.")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "appId",
        value = "application id",
        required = true,
        paramType = "query",
        dataTypeClass = Long.class),
    @ApiImplicitParam(
        name = "savepointPath",
        value = "specified savepoint path",
        paramType = "query",
        dataTypeClass = String.class)
  })
  @PostMapping("list")
  public RestResponse list(Long teamId, RestRequest restRequest, YarnQueue yarnQueue) {
    IPage<YarnQueue> queuePage = yarnQueueService.findYarnQueues(teamId, yarnQueue, restRequest);
    return RestResponse.success(queuePage);
  }

  /**
   * Check for the yarn queue if exists in the team or the queueLabel format is invalid. status msg
   * 0 Success 1 The queue already existed in the team 2 The queueLabel format in invalid.
   */
  @ApiAccess
  @ApiOperation(value = "Trigger savepoint for specified application by id.")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "appId",
        value = "application id",
        required = true,
        paramType = "query",
        dataTypeClass = Long.class),
    @ApiImplicitParam(
        name = "savepointPath",
        value = "specified savepoint path",
        paramType = "query",
        dataTypeClass = String.class)
  })
  @PostMapping("check")
  public RestResponse check(YarnQueue yarnQueue) {
    ResponseResult checkResult = null;
    return RestResponse.success(checkResult);
  }

  @ApiAccess
  @ApiOperation(value = "Trigger savepoint for specified application by id.")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "appId",
        value = "application id",
        required = true,
        paramType = "query",
        dataTypeClass = Long.class),
    @ApiImplicitParam(
        name = "savepointPath",
        value = "specified savepoint path",
        paramType = "query",
        dataTypeClass = String.class)
  })
  @PostMapping("create")
  @RequiresPermissions("yarnQueue:create")
  public RestResponse create(YarnQueue yarnQueue) {
    return RestResponse.success(true);
  }

  @ApiAccess
  @ApiOperation(value = "Trigger savepoint for specified application by id.")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "appId",
        value = "application id",
        required = true,
        paramType = "query",
        dataTypeClass = Long.class),
    @ApiImplicitParam(
        name = "savepointPath",
        value = "specified savepoint path",
        paramType = "query",
        dataTypeClass = String.class)
  })
  @PostMapping("update")
  @RequiresPermissions("yarnQueue:update")
  public RestResponse update(YarnQueue yarnQueue) {
    return RestResponse.success();
  }

  @ApiAccess
  @ApiOperation(value = "Trigger savepoint for specified application by id.")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "appId",
        value = "application id",
        required = true,
        paramType = "query",
        dataTypeClass = Long.class),
    @ApiImplicitParam(
        name = "savepointPath",
        value = "specified savepoint path",
        paramType = "query",
        dataTypeClass = String.class)
  })
  @PostMapping("get")
  public RestResponse get(Long queueId) {
    YarnQueue yarnQueue = null;
    return RestResponse.success(yarnQueue);
  }

  @ApiAccess
  @ApiOperation(value = "Delete a yarn queue by (team id & yarn queue) or yarn queue id.")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "yarnQueue",
        value = "Yarn Queue json value",
        required = true,
        paramType = "body",
        dataTypeClass = YarnQueue.class)
  })
  @PostMapping("delete")
  @RequiresPermissions("yarnQueue:delete")
  public RestResponse delete(YarnQueue yarnQueue) {
    return RestResponse.success();
  }
}
