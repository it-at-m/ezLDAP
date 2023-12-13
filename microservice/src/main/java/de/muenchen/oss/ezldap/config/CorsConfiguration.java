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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.muenchen.oss.ezldap.spring.props.EzLdapConfigurationProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.prankl
 *
 */
@Configuration
@ConditionalOnProperty(name = "ezldap.cors.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CorsConfiguration {

    @Bean
    WebMvcConfigurer corsConfigurer(EzLdapConfigurationProperties configProps, CorsConfigurationProperties corsProps) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if (!corsProps.getAllowedOriginPatterns().isEmpty()) {
                    String mapping = configProps.getApiPath() + "/v1/ldap/**";
                    registry.addMapping(mapping).allowedOriginPatterns(corsProps.getAllowedOriginPatterns().toArray(new String[0]));
                    log.info("Configured CORS for '{}' with allowedOriginsPatterns: {}", mapping, corsProps.getAllowedOriginPatterns());
                }
            }
        };
    }

}
