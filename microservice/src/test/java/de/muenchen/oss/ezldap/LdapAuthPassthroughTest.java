/*
 * The MIT License
 * Copyright © 2024 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.muenchen.oss.ezldap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(
        properties = {
                "app.auth-mode=ldap",
                "ezldap.ldap.ou-search-base=dc=example,dc=org",
                "ezldap.ldap.user-search-base=dc=example,dc=org" },
        webEnvironment = WebEnvironment.RANDOM_PORT
)
@Slf4j
class LdapAuthPassthroughTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private Integer webPort;

    @TestConfiguration(proxyBeanMethods = false)
    static class LdapAuthPassthroughConfig {

        @Bean
        public GenericContainer<?> ldapContainer(DynamicPropertyRegistry props) {
            GenericContainer<?> ldapContainer = new GenericContainer<>(
                    DockerImageName.parse("osixia/openldap:1.5.0"))
                            .withExposedPorts(389)
                            .withCommand("--copy-service")
                            .withCopyFileToContainer(MountableFile.forHostPath("../lib-core/src/test/resources/ldap/schema/lhm.schema"),
                                    "/container/service/slapd/assets/config/bootstrap/schema/lhm.schema")
                            .withCopyFileToContainer(MountableFile.forHostPath("../lib-core/src/test/resources/ldap/data"),
                                    "/container/service/slapd/assets/config/bootstrap/ldif/custom")
                            .withEnv("LDAP_ORGANISATION", "Example Inc.")
                            .withEnv("LDAP_DOMAIN", "example.org")
                            .withEnv("LDAP_READONLY_USER", "true")
                            .withEnv("LDAP_READONLY_USER_USERNAME", "readonly")
                            .withEnv("LDAP_READONLY_USER_PASSWORD", "readonly")
                            .waitingFor(Wait.forLogMessage(".*slapd starting.*\n", 1));

            props.add("ezldap.ldap.url", () -> "ldap://localhost:" + ldapContainer.getMappedPort(389));
            return ldapContainer;
        }

    }

    @Test
    void unauthenticated() {
        ResponseEntity<String> responseEntity = testRestTemplate
                .getForEntity("http://localhost:" + webPort + "/v1/ldap/search/findByUidWildcard?uid=erika.%2A&size=10", String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void basic_auth_invalid_dn_as_username() {
        ResponseEntity<String> responseEntity = testRestTemplate
                .withBasicAuth("notAValidDn", "unkownPassword")
                .getForEntity("http://localhost:" + webPort + "/v1/ldap/search/findByUidWildcard?uid=erika.%2A&size=10", String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void basic_auth_invalid_password() {
        ResponseEntity<String> responseEntity = testRestTemplate
                .withBasicAuth("cn=admin,dc=example,dc=org", "wrongPassword")
                .getForEntity("http://localhost:" + webPort + "/v1/ldap/search/findByUidWildcard?uid=erika.%2A&size=10", String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void basic_auth_with_ldap_ok() {
        ResponseEntity<String> responseEntity = testRestTemplate
                .withBasicAuth("cn=admin,dc=example,dc=org", "admin")
                .getForEntity("http://localhost:" + webPort + "/v1/ldap/search/findByUidWildcard?uid=erika.%2A&size=10", String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
