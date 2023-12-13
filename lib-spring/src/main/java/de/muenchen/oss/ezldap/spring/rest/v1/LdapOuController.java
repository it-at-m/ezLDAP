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

import de.muenchen.oss.ezldap.core.LdapOuDTO;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.prankl
 * @author m.zollbrecht
 */
@Controller
@Slf4j
@RequestMapping(value = "${ezldap.api-path:}/v1/ldap", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "OU", description = "Lese/Suchzugriffe für Organisationseinheiten (OU)")
public class LdapOuController {

    private final LdapServiceAdapter ldapService;

    /**
     * Erzeugt eine Instanz.
     *
     * @param ldapServiceAdapter eine {@link LdapServiceAdapter}
     */
    public LdapOuController(final LdapServiceAdapter ldapServiceAdapter) {
        this.ldapService = ldapServiceAdapter;
    }

    /**
     * GET /ldap/ou/{lhmObjectId}
     *
     * @param lhmObjectId eine lhmObjectId
     * @return {@link LdapUserDTO}
     */
    @GetMapping("/ou/{lhmObjectId}")
    @Operation(
            summary = "Lookup einer OU via lhmObjectId", operationId = "getOuByLhmObjectId", method = "GET", parameters = {
                    @Parameter(name = "lhmObjectId", required = true, description = "lhmObjectId der OU", example = "112043571")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK")
            }
    )
    public ResponseEntity<LdapOuDTO> getOuByLhmObjectId(
            @PathVariable(name = "lhmObjectId") final String lhmObjectId) {
        log.info("Incoming LDAP OU request for lhmObjectId: {}", lhmObjectId);
        final Optional<LdapOuDTO> ou = this.ldapService.getOu(lhmObjectId);
        if (ou.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                    .body(ou.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /ldap/outree/{ouShortCode}
     *
     * @param ouShortCode eine OU Kurzbezeichnung (z.B. "ITM-KM21")
     * @return Liste der OU-Shortcodes des OU-Baums
     */
    @GetMapping("/outree/{ouShortCode}")
    @Operation(
            summary = "Lookup des OU Baums via OU Kurzbezeichnung", operationId = "ouTree", method = "GET", parameters = {
                    @Parameter(name = "ouShortCode", required = true, description = "OU Kurzbezeichnung (z.B. ITM-KM21)", example = "ITM-KM21")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK")
            }
    )
    public ResponseEntity<List<String>> getOUTreeByOUShortcode(
            @PathVariable(name = "ouShortCode") final String ouShortCode) {
        log.info("Incoming LDAP OU request for ou: {}", ouShortCode);
        final Optional<List<String>> result = this.ldapService.findOuTree(ouShortCode);
        if (result.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                    .body(result.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /ldap/ou/search/findByOu?ou={ou}
     *
     * @param ou ein OU-Shortcode (z.B. "ITM-KM21")
     * @return {@link LdapUserDTO}
     */
    @GetMapping("/ou/search/findByOu")
    @Operation(
            summary = "Lookup einer OU via OU Kurzbezeichnung", operationId = "findOuByOuShortcode", method = "GET", parameters = {
                    @Parameter(name = "ou", required = true, description = "OU Kurzbezeichnung (z.B. ITM-KM21)", example = "ITM-KM21")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK")
            }
    )
    public ResponseEntity<LdapOuDTO> findOuByOuShortcode(
            @RequestParam(name = "ou") final String ou) {
        log.info("Incoming LDAP OU request for ou shortcode: {}", ou);
        final Optional<LdapOuDTO> result = this.ldapService.findOuByOuShortcode(ou);
        if (result.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                    .body(result.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
