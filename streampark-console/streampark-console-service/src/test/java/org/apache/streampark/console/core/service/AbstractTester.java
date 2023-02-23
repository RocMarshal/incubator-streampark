package org.apache.streampark.console.core.service;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/** Service test base with testcontainers mysql db. */
@Transactional
//@TestPropertySource(locations = {"classpath:application-test.properties"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@AutoConfigureWebTestClient(timeout = "60000")
@AutoConfigureTestEntityManager
@ActiveProfiles("test")
public class AbstractTester {


        protected static final Logger LOG = LoggerFactory.getLogger(AbstractTester.class);

        protected static final DockerImageName MYSQL_57_IMAGE = DockerImageName.parse("mysql:5.7.29");
        protected static final String MYSQL_INIT_SCRIPT = "schema.sql";
        protected static final Map<String, String> DEFAULT_CONTAINER_ENV_MAP =
            new HashMap<String, String>() {
                {
                    put("MYSQL_ROOT_HOST", "%");
                }
            };

        static Integer port = 12400;

        @Autowired
        protected TestEntityManager entityManager;

        @Autowired
        protected WebTestClient webTestClient;

        protected static final String TEST_USER_NAME = "min.guo";


        private static final MySQLContainer mysqldb;

        static {
            Instant start = Instant.now();
            mysqldb = new MySQLContainer<>(MYSQL_57_IMAGE)
                .withUsername("root")
                .withPassword("123456789")
                .withDatabaseName("shopee_flink_manager_db")
                .withEnv(DEFAULT_CONTAINER_ENV_MAP)
                .withInitScript(MYSQL_INIT_SCRIPT)
                .withLogConsumer(new Slf4jLogConsumer(LOG));
            Startables.deepStart(Stream.of(mysqldb)).join();
            LOG.info("Controller Testcontainer started in {}", Duration.between(start, Instant.now()));

        }

        @DynamicPropertySource
        static void properties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", mysqldb::getJdbcUrl);
            registry.add("spring.datasource.driver-class-name", mysqldb::getDriverClassName);
            registry.add("spring.datasource.username", mysqldb::getUsername);
            registry.add("spring.datasource.password", mysqldb::getPassword);
        }

        @BeforeAll
        public static void init() {
            initRamUrl();
            initLauncherUrl();
            initSqlServiceUrl();
            initDataStudioUrl();
            initDataMapUrl();
        }

        protected static void initDataMapUrl() {
            String url = getHost();
            GlobalConfiguration.getGlobalConfiguration().set(DATA_MAP_URL, url);
        }

        private static void initSqlServiceUrl() {
            Map<String, String> localMap = new LinkedHashMap<>();
            String url = getHost();
            localMap.put("SG", url);
            localMap.put("US_EAST", url);
            GlobalConfiguration.getGlobalConfiguration().set(SQL_SERVICE_URL, localMap);
            // use different ConfigOption~
            com.shopee.di.fm.common.configuration.ConfigOption<Map<String, String>> conf =
                com.shopee.di.fm.common.configuration.ConfigOptions.key("flink-manager.sql.service.url")
                    .mapType().defaultValue(new HashMap<>());
            com.shopee.di.fm.common.configuration.GlobalConfiguration.getGlobalConfiguration().set(conf, localMap);
        }

        private static void initDataStudioUrl() {
            String url = getHost() + "/datastudio/api/v1";
            com.shopee.di.fm.common.configuration.GlobalConfiguration.getGlobalConfiguration().set(DATASTUDIO_ADDRESS, url);
        }

        private static void initLauncherUrl() {
            Map<String, String> localMap = new LinkedHashMap<>();
            String url = getHost();
            localMap.put("SG", url);
            localMap.put("US_EAST", url);
            GlobalConfiguration.getGlobalConfiguration().set(FLINK_LAUNCHER_URL, localMap);
        }

        private static void initRamUrl() {
            GlobalConfiguration.getGlobalConfiguration().set(RAM_BASE_URL, getHost());
        }

        protected static String getHost() {
            return String.format("http://127.0.0.1:%s", port);
        }

        protected void truncateTable(String filePath) throws IOException {
            String sql = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8.toString());
            for (String statement : sql.split(";")) {
                entityManager.getEntityManager().createNativeQuery(statement).executeUpdate();
            }
        }

}
