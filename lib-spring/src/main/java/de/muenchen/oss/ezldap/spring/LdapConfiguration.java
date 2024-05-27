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
package de.muenchen.oss.ezldap.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import de.muenchen.oss.ezldap.core.DtoMapperImpl;
import de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper;
import de.muenchen.oss.ezldap.core.LdapOuAttributesMapper;
import de.muenchen.oss.ezldap.core.LdapService;
import de.muenchen.oss.ezldap.core.LdapUserAttributesMapper;
import de.muenchen.oss.ezldap.spring.props.EzLdapConfigurationProperties;
import de.muenchen.oss.ezldap.spring.rest.v1.LdapServiceAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.prankl
 */
@Configuration
@Slf4j
public class LdapConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.auth-mode", havingValue = "none")
    LdapContextSource ldapContextSourceDefault(final EzLdapConfigurationProperties props) {
        final LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(props.getLdap().getUrl());
        contextSource.setUserDn(props.getLdap().getUserDn());
        contextSource.setPassword(props.getLdap().getPassword());
        log.info("Initiating LDAP connection with url='{}' and user-dn='{}'.", props.getLdap().getUrl(),
                props.getLdap().getUserDn());
        return contextSource;
    }

    @Bean
    LdapTemplate ldapTemplate(final LdapContextSource ldapContextSource) {
        return new LdapTemplate(ldapContextSource);
    }

    @Bean
    LdapService ldapService(final LdapTemplate template, final EzLdapConfigurationProperties props) {
        final LdapBaseUserAttributesMapper ldapBaseUserAttributesMapper = new LdapBaseUserAttributesMapper();
        final LdapOuAttributesMapper ldapOuAttributesMapper = new LdapOuAttributesMapper();
        final LdapUserAttributesMapper ldapUserAttributesMapper = new LdapUserAttributesMapper(
                ldapBaseUserAttributesMapper);
        return new LdapService(template, ldapUserAttributesMapper, ldapBaseUserAttributesMapper, ldapOuAttributesMapper,
                new DtoMapperImpl(), props.getLdap().getUserSearchBase(), props.getLdap().getOuSearchBase());
    }

    @Bean
    LdapServiceAdapter ldapServiceAdapter(final LdapService ldapService) {
        return new LdapServiceAdapter(ldapService);
    }

}
