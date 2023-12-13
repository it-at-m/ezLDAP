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

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.muenchen.oss.ezldap.config.AppConfigurationProperties.AuthMode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * SpringDocs Swagger API Docs configuration.
 *
 * @author michael.prankl
 *
 */
@Configuration
public class SpringdocsSwaggerConfig {

    @Bean
    GroupedOpenApi publicApi(OpenApiCustomizer globalResponseCodeCustomiser) {
        return GroupedOpenApi.builder()
                .group("ezLDAP")
                .packagesToScan("de.muenchen.oss.ezldap.spring.rest.v1")
                .addOpenApiCustomizer(globalResponseCodeCustomiser)
                .build();
    }

    @Bean
    OpenApiCustomizer globalResponseCodeCustomiser(AppConfigurationProperties appProps) {
        return openApi -> {
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                ApiResponses apiResponses = operation.getResponses();
                ApiResponse response401 = new ApiResponse().description("Unauthenticated");
                ApiResponse response403 = new ApiResponse().description("Forbidden");
                ApiResponse response404 = new ApiResponse().description("Not Found");
                ApiResponse response500 = new ApiResponse().description("Server Error");
                apiResponses.addApiResponse("401", response401);
                apiResponses.addApiResponse("403", response403);
                apiResponses.addApiResponse("404", response404);
                apiResponses.addApiResponse("500", response500);
            }));

            if (AuthMode.BASIC.equals(appProps.getAuthMode())) {
                openApi.addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                        .components(new Components()
                                .addSecuritySchemes("basicAuth", new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")));
            }
        };
    }

    @Bean
    OpenAPI openApi(BuildProperties buildProperties, AppConfigurationProperties appProps) {
        return new OpenAPI()
        // @formatter:off
                .info(new Info()
                        .title("ezLDAP API")
                        .version(buildProperties.getVersion())
                        .description(appProps.getSwaggerDescription())
                        .contact(new Contact()
                                .name(appProps.getSwaggerContactName())
                                .email(appProps.getSwaggerContactMail())
                                .url(appProps.getSwaggerContactUrl()))
                        );
        // @formatter:on
    }

}
