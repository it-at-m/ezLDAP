/*
 * The MIT License
 * Copyright © 2023 Landeshauptstadt München | it@M
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
package de.muenchen.oss.ezldap.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrationtest zu {@link LdapService} mit einem embedded LDAP Server.
 *
 * @author michael.prankl
 *
 */
@Testcontainers
public class LdapServiceIntegrationTest {

    private LdapService sut;

    private static final int OPENLDAP_EXPOSED_PORT = 389;
    private static final String USER_BASE = "o=users,dc=example,dc=org";
    private static final String ORG_BASE = "o=oubase,dc=example,dc=org";
    private static final String LDAP_DOMAIN = "example.org";

    @Container
    private static final GenericContainer<?> openldapContainer = new GenericContainer<>("osixia/openldap:1.5.0")
            .withNetworkAliases("openldap")
            .withCommand("--copy-service --loglevel debug")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("openldap")))
            .withEnv("LDAP_ORGANISATION", "Example Inc.")
            .withEnv("LDAP_DOMAIN", LDAP_DOMAIN)
            .withExposedPorts(OPENLDAP_EXPOSED_PORT)
            .waitingFor(Wait.forLogMessage(".*\\/container\\/run\\/process\\/slapd\\/run started as PID.*\\n", 1))
            .withFileSystemBind(MountableFile.forClasspathResource("/ldap/schema/lhm.schema").getResolvedPath(),
                    "/container/service/slapd/assets/config/bootstrap/schema/lhm.schema",
                    BindMode.READ_ONLY)
            .withFileSystemBind(MountableFile.forClasspathResource("/ldap/data").getResolvedPath(),
                    "/container/service/slapd/assets/config/bootstrap/ldif/custom",
                    BindMode.READ_ONLY);

    private LdapTemplate ldapTemplate(final LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    private LdapContextSource contextSource(final int port) {
        final LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl("ldap://localhost:" + port);
        ldapContextSource.setUserDn("cn=admin,dc=example,dc=org");
        ldapContextSource.setPassword("admin");
        //        ldapContextSource.setAnonymousReadOnly(true);
        // we need to call this manually if no Spring context present
        ldapContextSource.afterPropertiesSet();
        return ldapContextSource;
    }

    @BeforeEach
    public void beforeEach() {
        Integer exposedPort = openldapContainer.getMappedPort(OPENLDAP_EXPOSED_PORT);
        System.out.println(exposedPort);
        final LdapContextSource contextSource = this.contextSource(exposedPort);
        final LdapBaseUserAttributesMapper baseUserAttributesMapper = new LdapBaseUserAttributesMapper();
        this.sut = new LdapService(this.ldapTemplate(contextSource),
                new LdapUserAttributesMapper(baseUserAttributesMapper),
                baseUserAttributesMapper, new LdapOuAttributesMapper(), new DtoMapperImpl(), USER_BASE,
                ORG_BASE);
    }

    @Test
    void get_ou_with_manager_attributes() {
        final Optional<LdapOuDTO> ouOpt = this.sut.getOu("30002");
        assertThat(ouOpt).isPresent();
        final LdapOuDTO ou = ouOpt.get();
        assertThat(ou.getFacsimileTelephoneNumber()).isNull();
        assertThat(ou.getLhmObjectId()).isEqualTo("30002");
        assertThat(ou.getLhmOUKey()).isNull();
        assertThat(ou.getLhmOULongname()).isNull();
        assertThat(ou.getLhmOUShortname()).isEqualTo("RBS-A-1");
        assertThat(ou.getMail()).isEqualTo("rbs.a1@" + LDAP_DOMAIN);
        assertThat(ou.getOu()).isEqualTo("Abteilung 1");
        // check leitung/stvt lookup
        assertThat(ou.getLeitung().getCn()).isEqualTo("Maxi Mustermann");
        assertThat(ou.getStellvertretung().getCn()).isEqualTo("Petra Mustermann");
    }

    @Test
    void get_ou_without_manager_attributes() {
        final Optional<LdapOuDTO> ouOpt = this.sut.getOu("30003");
        assertThat(ouOpt).isPresent();
        final LdapOuDTO ou = ouOpt.get();
        assertThat(ou.getFacsimileTelephoneNumber()).isEqualTo("123123");
        assertThat(ou.getLhmObjectId()).isEqualTo("30003");
        assertThat(ou.getLhmOUKey()).isEqualTo("09707139");
        assertThat(ou.getLhmOULongname()).isEqualTo("Referat, Abteilung 2");
        assertThat(ou.getLhmOUShortname()).isEqualTo("RBS-A-2");
        assertThat(ou.getMail()).isEqualTo("rbs.a2@" + LDAP_DOMAIN);
        assertThat(ou.getOu()).isEqualTo("Abteilung 2");
        // check leitung/stvt lookup
        assertThat(ou.getLeitung().getCn()).isEqualTo("Peter Lustig");
        assertThat(ou.getStellvertretung().getCn()).isEqualTo("Petra Lustig");
    }

    @Test
    void get_person() {
        final Optional<LdapUserDTO> person = this.sut.getPerson("20011");
        assertThat(person).isPresent();
        assertThat(person.get().getCn()).isEqualTo("Maxi Mustermann");
    }

    @Test
    void find_persons_by_ou_shortcode_exists() {
        final Optional<List<LdapBaseUserDTO>> result = this.sut.findPersonsByOuShortcode("rbs");
        assertThat(result).isPresent();
        assertThat(result.get()).extracting("uid").containsExactlyInAnyOrder("maxi.mustermann", "petra.mustermann", "peter.lustig", "petra.lustig", "john.doe");
    }

    @Test
    void find_persons_by_ou_shortcode_ou_not_existing() {
        final Optional<List<LdapBaseUserDTO>> result = this.sut.findPersonsByOuShortcode("hammaned");
        assertThat(result).isEmpty();
    }

    @Test
    void search_person_with_wildcard() {
        final List<LdapBaseUserDTO> result = this.sut.searchFor("*.mustermann", 10);
        assertThat(result).extracting("uid").containsExactlyInAnyOrder("maxi.mustermann", "petra.mustermann");
    }

    @Test
    void find_ou_with_shortname() {
        final Optional<LdapOuDTO> result = this.sut.findOuByShortcode("RBS-A-1");
        assertThat(result).isPresent();
        assertThat(result.get().getLhmObjectId()).isEqualTo("30002");
    }

    @Test
    void find_ou_with_shortname_not_found() {
        final Optional<LdapOuDTO> result = this.sut.findOuByShortcode("hammaned");
        assertThat(result).isEmpty();
    }

    @Test
    void find_ou_tree_by_ou_shortcode() {
        final Optional<List<String>> result = this.sut.findOuTreeByOuShortCode("RBS-A-1");
        assertThat(result).isPresent();
        Assertions.assertEquals(List.of("LHM", "RBS", "RBS-A-1"), result.get());
    }

    @Test
    void find_ou_tree_by_ou_shortcode_not_found() {
        final Optional<List<String>> result = this.sut.findOuTreeByOuShortCode("hammaned");
        assertThat(result).isEmpty();
    }

    @Test
    void find_ou_tree_by_user() {
        final Optional<List<String>> result = this.sut.findOuTreeByUserId("99999");
        assertThat(result).isPresent();
        Assertions.assertEquals(List.of("LHM", "RBS", "RBS-A-1"), result.get());
    }

    @Test
    void find_ou_tree_by_user_not_found() {
        final Optional<List<String>> result = this.sut.findOuTreeByUserId("00000");
        assertThat(result).isEmpty();
    }

    @Test
    void calculate_shade_tree() {

        var shadetree = this.sut.calculateSubtreeWithUsers("o=oubase,dc=example,dc=org", null);
        Assertions.assertTrue(shadetree.isPresent());
        var rootNode = shadetree.get().values().iterator().next();
        Assertions.assertEquals("o=oubase,dc=example,dc=org", rootNode.getDistinguishedName());
        Assertions.assertEquals("342", rootNode.getNode().getLhmObjectId());

        var rbs = rootNode.getChildNodes().values().iterator().next();
        Assertions.assertEquals("Referat für Bildung und Sport", rbs.getNode().getOu());

        var departments = rbs.getChildNodes();
        var abt_1 = departments.get("Abteilung 1");
        Assertions.assertEquals("ou=Abteilung 1,ou=Referat für Bildung und Sport,o=oubase,dc=example,dc=org", abt_1.getDistinguishedName());
        Assertions.assertEquals("ou=Abteilung 1,ou=Referat für Bildung und Sport,o=oubase,dc=example,dc=org", abt_1.getUsers().get(0).getLhmObjectPath());

        var logTree = shadetree.get().values().iterator().next().logTree("");
        Assertions.assertTrue(logTree.contains("***** New LDAP entry : RBS-A-2 Abteilung 2 *****"));
    }

    @Test
    void calculate_shade_tree_select_user_with_modifyTimestamp() {

        var shadetree = this.sut.calculateSubtreeWithUsers("o=oubase,dc=example,dc=org", "20240226083627Z");
        Assertions.assertTrue(shadetree.isPresent());
        Assertions.assertEquals(1, shadetree.get().size());
        var rootNode = shadetree.get().values().iterator().next();
        Assertions.assertNotNull(rootNode.getNode().getModifyTimeStamp(), "Operational ldap ou attribute modifyTimestamp not selected.");
        var rbs = rootNode.getChildNodes().values().iterator().next();
        Assertions.assertEquals(1, rbs.getUsers().size(), "User expected. All users were created after the timestamp");
        Assertions.assertNotNull(rbs.getUsers().get(0).getModifyTimeStamp(), "Operational ldap user attribute modifyTimestamp not selected.");

        shadetree = this.sut.calculateSubtreeWithUsers("o=oubase,dc=example,dc=org", "30000000000000Z");
        Assertions.assertTrue(shadetree.isPresent());
        Assertions.assertEquals(1, shadetree.get().size());
        rootNode = shadetree.get().values().iterator().next();
        rbs = rootNode.getChildNodes().values().iterator().next();
        Assertions.assertEquals(0, rbs.getUsers().size(), "No user expected. The timestamp is too far in the future.");

    }

}
