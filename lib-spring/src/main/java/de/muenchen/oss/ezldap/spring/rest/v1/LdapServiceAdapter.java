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
package de.muenchen.oss.ezldap.spring.rest.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;

import de.muenchen.oss.ezldap.core.LdapBaseUserDTO;
import de.muenchen.oss.ezldap.core.LdapOuDTO;
import de.muenchen.oss.ezldap.core.LdapService;
import de.muenchen.oss.ezldap.core.LdapUserDTO;

import java.util.List;
import java.util.Optional;

/**
 * @author michael.prankl
 *
 */
@Slf4j
public class LdapServiceAdapter {

    private final LdapService ldapService;

    public LdapServiceAdapter(final LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @Cacheable("ousByLhmObjectId")
    public Optional<LdapOuDTO> getOu(final String lhmObjectId) {
        log.debug("Looking up ou with lhmObject '{}' via ldapService...", lhmObjectId);
        return this.ldapService.getOu(lhmObjectId);
    }

    @Cacheable("ousByOuShortcode")
    public Optional<LdapOuDTO> findOuByOuShortcode(final String ou) {
        log.debug("Looking up ou with lhmOUShortname '{}' via ldapService...", ou);
        return this.ldapService.findOuByShortcode(ou);
    }

    @Cacheable("usersByLhmObjectId")
    public Optional<LdapUserDTO> getPerson(final String lhmObjectId) {
        log.debug("Looking up person with lhmObject '{}' via ldapService...", lhmObjectId);
        return this.ldapService.getPerson(lhmObjectId);
    }

    @Cacheable("usersByUid")
    public Optional<LdapUserDTO> getPersonWithUID(final String uid) {
        log.debug("Looking up person with uid '{}' via ldapService...", uid);
        return this.ldapService.getPersonWithUID(uid);
    }

    @Cacheable("usersByUidSearch")
    public List<LdapBaseUserDTO> searchFor(final String uid, final Integer size) {
        log.debug("Searching for person '{}' (size: {}) via ldapService...", uid, size);
        return this.ldapService.searchFor(uid, size);
    }

    @Cacheable("usersByOuShortcode")
    public Optional<List<LdapBaseUserDTO>> findPersonsByOuShortcode(final String ou) {
        log.debug("Looking up persons in ou with shortcode '{}' ...", ou);
        return this.ldapService.findPersonsByOuShortcode(ou);
    }

    @Cacheable("ouTreeByOuShortcode")
    public Optional<List<String>> findOuTree(final String ouShortCode) {
        log.debug("Looking up ou tree for ou '{}' ...", ouShortCode);
        return this.ldapService.findOuTreeByOuShortCode(ouShortCode);
    }

    @Cacheable("ouTreeByUserId")
    public Optional<List<String>> findOuTreeForUser(final String userId) {
        log.debug("Looking up ou tree for user '{}' ...", userId);
        return this.ldapService.findOuTreeByUserId(userId);
    }

}
