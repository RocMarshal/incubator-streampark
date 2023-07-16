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

package org.apache.streampark.testcontainer.hadoop;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/** Test for {@link HadoopContainer}. */
class HadoopContainerTest {

  private final HadoopContainer hadoopContainer = new HadoopContainer();

  @BeforeEach
  void setup() {
    hadoopContainer.start();
  }

  @AfterEach
  void teardown() {
    hadoopContainer.stop();
  }

  void testOverview() throws IOException {

    String url = String.format("http://%s:%s/ws/v1/cluster/info", hadoopContainer.getHost(), 8088);
    CloseableHttpClient httpClient = HttpClients.createDefault();
    CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
    assertThat(response.getCode()).isEqualTo(200);

    url = String.format("http://%s:%s", hadoopContainer.getHost(), 50070);
    httpClient = HttpClients.createDefault();
    response = httpClient.execute(new HttpGet(url));
    assertThat(response.getCode()).isEqualTo(200);
  }
}
