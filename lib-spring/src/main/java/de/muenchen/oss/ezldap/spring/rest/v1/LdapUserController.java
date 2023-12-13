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

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.muenchen.oss.ezldap.core.LdapService;
import de.muenchen.oss.ezldap.spring.rest.v1.dto.LdapBaseUserDTO;
import de.muenchen.oss.ezldap.spring.rest.v1.dto.LdapUserDTO;
import de.muenchen.oss.ezldap.spring.rest.v1.dto.WebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.prankl
 *
 */
@Controller
@Slf4j
@RequestMapping(value = "${ezldap.api-path:}/v1/ldap", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User", description = "Lese/Suchzugriffe für Personen")
public class LdapUserController {

    private final LdapServiceAdapter ldapService;
    private final WebMapper webMapper;

    /**
     * Erzeugt eine Instanz.
     *
     * @param ldapServiceAdapter eine {@link LdapServiceAdapter}
     * @param webMapper ein {@link WebMapper}
     */
    public LdapUserController(final LdapServiceAdapter ldapServiceAdapter, final WebMapper webMapper) {
        this.ldapService = ldapServiceAdapter;
        this.webMapper = webMapper;
    }

    /**
     * GET /ldap/user/{lhmObjectId}
     *
     * @param lhmObjectId eine lhmObjectId
     * @return {@link LdapUserDTO}
     */
    @GetMapping("/user/{lhmObjectId}")
    @Operation(
            summary = "Lookup eines Users via lhmObjectId", operationId = "getUserByLhmObjectId", method = "GET", parameters = {
                    @Parameter(name = "lhmObjectId", required = true, description = "lhmObjectId des Users", example = "111140670")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK")
            }
    )
    public ResponseEntity<LdapUserDTO> getUserByLhmObjectId(
            @PathVariable(name = "lhmObjectId") final String lhmObjectId) {
        log.info("Incoming LDAP User request for lhmObjectId: {}", lhmObjectId);
        Optional<de.muenchen.oss.ezldap.core.LdapUserDTO> person = this.ldapService.getPerson(lhmObjectId);
        if (person.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                    .body(webMapper.toWebDto(person.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /ldap/search/findByUid?uid={uid}
     *
     * @param uid eine UID
     * @return {@link LdapUserDTO}
     */
    @GetMapping("/search/findByUid")
    @Operation(
            summary = "Lookup eines Users via uid", operationId = "getUserByUid", method = "GET", parameters = {
                    @Parameter(name = "uid", required = true, description = "uid des Users", example = "erika.musterfrau")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK")
            }
    )
    public ResponseEntity<LdapUserDTO> getUserByUid(
            @RequestParam(name = "uid", required = true) final String uid) {
        log.info("Incoming LDAP User request for uid: {}", uid);
        Optional<de.muenchen.oss.ezldap.core.LdapUserDTO> personWithUID = this.ldapService.getPersonWithUID(uid);
        if (personWithUID.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                    .body(webMapper.toWebDto(personWithUID.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /ldap/search/findByUidWildcard?uid=[suchphrase](&size=20)
     *
     * @param uid eine UID, unterstützt * als Platzhalter. Muss mindestens 3 Zeichen lang sein (*
     *            zählen nicht als Zeichen).
     * @return {@link LdapUserDTO}
     */
    @GetMapping("/search/findByUidWildcard")
    @Operation(
            summary = "Wildcard-unterstüztende Suche nach Usern via uid", operationId = "searchUser", method = "GET", parameters = {
                    @Parameter(name = "uid", required = true, description = "uid des Users (* als Wildcards erlaubt)", example = "erika.*"),
                    @Parameter(name = "size", description = "Trefferanzahl, default: 10, Maximum: 100"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK")
            }
    )
    public ResponseEntity<List<LdapBaseUserDTO>> searchUser(
    // @formatter:off
                @RequestParam(name = "uid", required = true)
                @Size(min = 3, message = "UID muss mindestens 3 Zeichen haben") final
                String uid,

                @RequestParam(name = "size", defaultValue = "10")
                @Max(value = LdapService.MAX_SEARCH_RESULTS, message = "Parameter 'size' darf nicht größer wie 100 sein") final
    Integer size
            ) {
        // @formatter:on
        log.info("Incoming LDAP User search request for uid like '{}'", uid);
        if (uid.replace("*", "").length() < 3) {
            return ResponseEntity.badRequest().build();
        }
        List<de.muenchen.oss.ezldap.core.LdapBaseUserDTO> results = this.ldapService.searchFor(uid, size);
        return ResponseEntity.ok(webMapper.toWebDtoList(results));
    }

    /**
     * GET /ldap/search/findByOu?ou={ou}
     *
     * @param ou Kurzbezeichnung der OU (z.B. "ITM-KM21")
     * @return {@link List<LdapBaseUserDTO>}
     */
    @GetMapping("/search/findByOu")
    @Operation(
            summary = "Lookup von Usern via OU Kurzbezeichnung", operationId = "findByOu", method = "GET", parameters = {
                    @Parameter(name = "ou", required = true, description = "OU Kurzbezeichnung (z.B. ITM-KM21)", example = "ITM-KM21")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "OU existiert nicht"),
            }
    )
    public ResponseEntity<List<LdapBaseUserDTO>> findByOu(@RequestParam(name = "ou") final String ou) {
        Optional<List<de.muenchen.oss.ezldap.core.LdapBaseUserDTO>> result = this.ldapService.findPersonsByOuShortcode(ou);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                    .body(webMapper.toWebDtoList(result.get()));
        }
    }

    /**
     * GET /ldap/outree/{lhmObjectId}
     *
     * @param lhmObjectId des Users
     * @return Liste der OU-Shortcodes des OU-Baums des Users
     */
    @GetMapping("/user/outree/{lhmObjectId}")
    @Operation(
            summary = "Lookup des OU Baums des Users", operationId = "ouTree", method = "GET", parameters = {
                    @Parameter(name = "lhmObjectId", required = true, description = "lhmObjectId des Users", example = "111140670")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK")
            }
    )
    public ResponseEntity<List<String>> getOUTreeByOUShortcode(
            @PathVariable(name = "lhmObjectId") final String lhmObjectId) {
        log.info("Incoming LDAP OU request for user: {}", lhmObjectId);
        final Optional<List<String>> result = this.ldapService.findOuTreeForUser(lhmObjectId);
        if (result.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                    .body(result.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
