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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.prankl
 */
@Configuration
@ConditionalOnProperty(name = "ezldap.cache.enabled", havingValue = "true")
@EnableCaching
@Slf4j
public class CachingConfiguration implements CachingConfigurer {

    @Bean
    EhCachePropertiesReplacer replacer() {
        EhCachePropertiesReplacer replacer = new EhCachePropertiesReplacer();
        // pass through spring environment property as System property (that is resolved in ehcache.xml)
        replacer.setEhcahePropertyNames(Arrays.asList("ezldap.cache.disk.dir"));
        return replacer;
    }

    @Override
    @Bean
    public org.springframework.cache.CacheManager cacheManager() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager;
        try {
            URL url = ResourceUtils.getURL("classpath:ezldap-ehcache.xml");
            log.info("Configuring caching for ezLDAP from {} ...", url.toString());
            cacheManager = cachingProvider.getCacheManager(
                    url.toURI(),
                    getClass().getClassLoader());
            return new JCacheCacheManager(cacheManager);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("ezLDAP EHCache configuration failed.", e);
        }
    }

}
