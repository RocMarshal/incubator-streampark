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

package org.apache.streampark.base;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.server.MockServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.apache.streampark.console.core.runner.EnvInitializer.SKIP_RUN_METHOD;

/** base tester. */
@ExtendWith(MockitoExtension.class)
@Transactional
@TestPropertySource(locations = {"classpath:application.yml"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@AutoConfigureWebTestClient(timeout = "60000")
@AutoConfigureTestEntityManager
public abstract class SpringTestBase {

  protected static final Logger LOG = LoggerFactory.getLogger(SpringTestBase.class);

  protected static final DockerImageName MOCKSERVER_IMAGE =
      DockerImageName.parse("mockserver/mockserver")
          .withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion());

  protected static final DockerImageName MYSQL_57_IMAGE = DockerImageName.parse("mysql:5.7.29");

  protected static final String MYSQL_INIT_SCRIPT = "initsql/mysql/init.sql";
  protected static final String DEFAULT_USER = "root";
  protected static final String DEFAULT_DB = "streampark";
  protected static final String DEFAULT_PWD = DEFAULT_DB;
  protected static final Map<String, String> DEFAULT_CONTAINER_ENV_MAP =
      new HashMap<String, String>() {
        {
          put("MYSQL_ROOT_HOST", "%");
        }
      };

  @Container
  protected static MySQLContainer MYSQLDB =
      new MySQLContainer<>(MYSQL_57_IMAGE)
          .withUsername(DEFAULT_USER)
          .withPassword(DEFAULT_PWD)
          .withDatabaseName(DEFAULT_DB)
          .withEnv(DEFAULT_CONTAINER_ENV_MAP)
          .withInitScript(MYSQL_INIT_SCRIPT)
          .withLogConsumer(new Slf4jLogConsumer(LOG));

  static Integer port = 12400;

  //  @Container
  //  public static BrowserWebDriverContainer<?> chrome =
  //      new BrowserWebDriverContainer<>().withCapabilities(new ChromeOptions());

  //  @Container
  //  public static MockServerContainer mockServer = new MockServerContainer(MOCKSERVER_IMAGE);

  static {
    Instant start = Instant.now();
    Startables.deepStart(Stream.of(MYSQLDB)).join();
    LOG.info("Controller test container started in {}", Duration.between(start, Instant.now()));
  }

  protected RestTemplate restTemplate = createRestTemplate();

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQLDB::getJdbcUrl);
    registry.add("spring.datasource.driver-class-name", MYSQLDB::getDriverClassName);
    registry.add("spring.datasource.username", MYSQLDB::getUsername);
    registry.add("spring.datasource.password", MYSQLDB::getPassword);
  }

  @BeforeAll
  public static void init(@TempDir File tempPath) {
    System.setProperty(SKIP_RUN_METHOD, "true");
    // Skip the EnvInitializer#run method by flag in System.properties.
    String mockedHome = tempPath.getAbsolutePath();

    // Skip the EnvInitializer#run method by mocking file into app.home
    /*File file =
        new File(SpringTestBase.class.getClassLoader().getResource("mock/appHome").getPath());
    for (String filename : file.list()) {
      FileUtils.copyDirectoryToDirectory(
          new File(file.getAbsolutePath(), filename), new File(mockedHome));
    }*/

    System.setProperty(org.apache.streampark.common.conf.ConfigConst.KEY_APP_HOME(), mockedHome);
  }

  protected RestTemplate createRestTemplate() {
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory();
    int timeoutMills = (int) TimeUnit.MINUTES.toMillis(3);
    requestFactory.setReadTimeout(timeoutMills);
    requestFactory.setConnectTimeout(timeoutMills);

    RestTemplate restTemplate = new RestTemplate(requestFactory);
    restTemplate
        .getMessageConverters()
        .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    return restTemplate;
  }
}
