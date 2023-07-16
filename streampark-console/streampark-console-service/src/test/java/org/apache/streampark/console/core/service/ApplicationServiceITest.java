/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.streampark.console.core.service;

import org.apache.streampark.common.enums.ExecutionMode;
import org.apache.streampark.console.SpringIntegrationTestBase;
import org.apache.streampark.console.core.entity.Application;
import org.apache.streampark.console.core.entity.FlinkCluster;
import org.apache.streampark.console.core.entity.FlinkEnv;
import org.apache.streampark.console.core.entity.FlinkSql;
import org.apache.streampark.console.core.enums.ReleaseState;
import org.apache.streampark.console.core.service.impl.FlinkClusterServiceImpl;
import org.apache.streampark.testcontainer.flink.FlinkStandaloneSessionCluster;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration test for {@link
 * org.apache.streampark.console.core.service.impl.ApplicationServiceImpl}.
 */
class ApplicationServiceITest extends SpringIntegrationTestBase {

  static FlinkStandaloneSessionCluster cluster =
      FlinkStandaloneSessionCluster.builder().slotsNumPerTm(4).slf4jLogConsumer(null).build();

  @Autowired private ApplicationService appService;

  @Autowired private FlinkClusterService clusterService;

  @Autowired private FlinkEnvService envService;

  @Autowired private AppBuildPipeService appBuildPipeService;

  @Autowired private FlinkSqlService sqlService;

  @BeforeAll
  static void setup() {
    cluster.start();
  }

  @AfterAll
  static void teardown() {
    cluster.stop();
  }

  @AfterEach
  void clear() {
    appService.getBaseMapper().delete(new QueryWrapper<>());
    clusterService.getBaseMapper().delete(new QueryWrapper<>());
    envService.getBaseMapper().delete(new QueryWrapper<>());
    appBuildPipeService.getBaseMapper().delete(new QueryWrapper<>());
    sqlService.getBaseMapper().delete(new QueryWrapper<>());
  }

  @Test
  void testStartAppOnRemoteSessionMode() throws Exception {
    FlinkEnv flinkEnv = new FlinkEnv();
    flinkEnv.setFlinkHome(defaultFlinkHome);
    flinkEnv.setFlinkName(DEFAULT_FLINK_VERSION);
    flinkEnv.setId(1L);
    envService.create(flinkEnv);
    FlinkCluster flinkCluster = new FlinkCluster();
    flinkCluster.setId(1L);
    flinkCluster.setAddress("http://localhost:8081");
    flinkCluster.setExecutionMode(ExecutionMode.REMOTE.getMode());
    flinkCluster.setClusterName("docker-Cluster-1.17.1");
    flinkCluster.setVersionId(1L);
    flinkCluster.setUserId(100000L);
    ((FlinkClusterServiceImpl) clusterService).internalCreate(flinkCluster);
    // two callings.
    FlinkSql effective = sqlService.getEffective(100000L, true);
    // error here.
    FlinkSql effective1 = sqlService.getEffective(100000L, true);
    Application appParam = new Application();
    appParam.setId(100000L);
    appParam.setTeamId(100000L);
    Application application = appService.getApp(appParam);
    application.setFlinkClusterId(1L);
    application.setSqlId(100000L);
    application.setVersionId(1L);
    application.setExecutionMode(ExecutionMode.REMOTE.getMode());

    appService.update(application);
    appBuildPipeService.buildApplication(100000L, false);
    while (application.getReleaseState() != ReleaseState.DONE) {
      application = appService.getApp(application);
    }
    appService.start(appService.getById(100000L), false);
    while (true) {}
  }
}
