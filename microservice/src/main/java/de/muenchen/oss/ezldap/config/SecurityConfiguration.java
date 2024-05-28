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
package de.muenchen.oss.ezldap.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.SpringSecurityAuthenticationSource;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import de.muenchen.oss.ezldap.config.AppConfigurationProperties.AuthMode;
import de.muenchen.oss.ezldap.spring.props.EzLdapConfigurationProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security Filter Chain Konfiguration
 *
 * @author michael.prankl
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, EzLdapConfigurationProperties configProps,
            AppConfigurationProperties appProps) throws Exception {
        if (AuthMode.NONE.equals(appProps.getAuthMode())) {
            log.info("Bootstrapping Spring Security filter chain for auth-mode 'none' ...");
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            http.sessionManagement(
                    sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        } else if (AuthMode.LDAP.equals(appProps.getAuthMode())) {
            log.info("Bootstrapping Spring Security filter chain for auth-mode 'ldap' ...");
            configureMatchers(http);
            http.sessionManagement(
                    sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
            http.httpBasic(Customizer.withDefaults());
        } else {
            log.info("Bootstrapping Spring Security filter chain for auth-mode 'basic' ...");
            configureMatchers(http);
            http.sessionManagement(
                    sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            http.httpBasic(Customizer.withDefaults());
        }
        return http.build();
    }

    private void configureMatchers(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> {
            authz.requestMatchers("/openapi/v3/**", "/swagger-ui/**").permitAll();
            authz.requestMatchers("/actuator/prometheus", "/actuator/info", "/actuator/health/**").permitAll();
            authz.anyRequest().authenticated();
        });
    }

    @Bean
    @ConditionalOnProperty(name = "app.auth-mode", havingValue = "basic")
    InMemoryUserDetailsManager userDetailsService(AppConfigurationProperties appProps) {
        UserDetails userDetails = User.withUsername(appProps.getBasicAuth().getUser())
                .password(appProps.getBasicAuth().getPassword()).roles("USER").build();
        return new InMemoryUserDetailsManager(userDetails);
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth, final EzLdapConfigurationProperties props,
            final AppConfigurationProperties appProps) throws Exception {
        if (appProps.getAuthMode().equals(AuthMode.LDAP)) {
            auth.ldapAuthentication()
                    .userSearchBase(appProps.getLdapAuth().getUserSearchBase())
                    .userSearchFilter(appProps.getLdapAuth().getUserSearchFilter())
                    .groupSearchBase(props.getLdap().getOuSearchBase()).contextSource().url(props.getLdap().getUrl());
            auth.eraseCredentials(false);
        }
    }

    @Bean(name = "ezldapQueryContextSource")
    @ConditionalOnProperty(name = "app.auth-mode", havingValue = "ldap")
    LdapContextSource ezldapQueryContextSource(final EzLdapConfigurationProperties props) {
        final LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(props.getLdap().getUrl());
        contextSource.setAuthenticationSource(new SpringSecurityAuthenticationSource());
        log.info(
                "Initiating LDAP context-source with url='{}' and SpringSecurityAuthenticationSource for Web LDAP authentication credentials passthrough to LDAP queries.",
                props.getLdap().getUrl());
        return contextSource;
    }

}
