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
package de.muenchen.oss.ezldap.spring.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(name = "LdapOuDTO", description = "Representation einer Organisationseinheit")
public class LdapOuDTO {

    @ToString.Include(rank = 0)
    @Schema(description = "Eindeutige ID der Organisationseinheit in der LHM", example = "123456789")
    private String lhmObjectId;

    @ToString.Include(rank = 1)
    @Schema(description = "Kürzel der Organisationseinheit", example = "REF-A1")
    private String ou;

    private String lhmOUKey;
    @Schema(description = "Langname der Organisationseinheit", example = "Servicebereich Grundsatz A1")
    private String lhmOULongname;
    @Schema(description = "Kurzbezeichnung Organisationseinheit", example = "REF-A1")
    private String lhmOUShortname;

    @Schema(description = "Postleitzahl", example = "80331")
    private String postalCode;
    @Schema(description = "Straße und Hausnummer ink. Hausnummernzusatz", example = "Marienplatz 8")
    private String street;

    @Schema(description = "Mailadresse der Organisationseinheit", example = "ref.a1@muenchen.de")
    private String mail;

    @Schema(description = "Festnetztelefonnummer", example = "99998 000")
    private String telephoneNumber;

    @Schema(description = "Faxnummer", example = "99998 001")
    private String facsimileTelephoneNumber;

    @Schema(description = "Leitung der Organisationseinheit")
    private LdapUserDTO leitung;
    @Schema(description = "Stellvertretende Leitung der Organisationseinheit")
    private LdapUserDTO stellvertretung;

}
