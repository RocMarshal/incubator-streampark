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

package org.apache.streampark.console.system.service;

import org.apache.streampark.base.SpringTestBase;
import org.apache.streampark.console.system.entity.Team;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeamServiceITest extends SpringTestBase {

  @Autowired private TeamService teamService;

  /*@Test
  void test() {
    MockServerClient mockServerClient =
        new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
    String targetUrl =
        String.format(
            "http://%s/%s/person?name=peter", mockServer.getHost(), mockServer.getServerPort());
    String format = String.format("http://%s/person?name=peter", targetUrl);
    mockServerClient
        .when(
            request().withPath("/person").withQueryStringParameter(new Parameter("name", "peter")))
        .respond(response().withBody("Peter the person!"));

    // ...a GET request to '/person?name=peter' returns "Peter the person!"

    assertThat(
            restTemplate.exchange(format, HttpMethod.GET, HttpEntity.EMPTY, String.class).getBody())
        .as("Expectation returns expected response body")
        .contains("Peter the person");
  }*/

  @Test
  void testSelect() {
    List<Team> list = teamService.list();
    System.out.println(list);
    assertThat(list).hasSize(1);
  }
}
