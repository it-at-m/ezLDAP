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
@Schema(name = "LdapUserDTO", description = "Detaillierte Representation einer Person")
public class LdapUserDTO {

    @ToString.Include(rank = 0)
    @Schema(description = "Eindeutige ID einer Person in der LHM", example = "123456789")
    private String lhmObjectId;

    @Schema(description = "UID der Person, kann sich ändern (z.B. nach Heirat)", example = "erika.musterfrau")
    @ToString.Include(rank = 1)
    private String uid;

    @Schema(description = "Anrede der Person", example = "Frau")
    private String anrede;

    @Schema(description = "Vorname(n) der Person", example = "Erika")
    private String vorname;

    @Schema(description = "Familienname der Person", example = "Musterfrau")
    private String nachname;

    @ToString.Include(rank = 2)
    @Schema(description = "Vor- und Familienname der Person", example = "Erika Musterfrau")
    private String cn;

    @ToString.Include(rank = 3)
    @Schema(description = "Kurzbezeichnung der organisatorischen Einheit der Person", example = "REF-A1")
    private String ou;

    @Schema(description = "Langname der organisatorischen Einheit der Person", example = "Servicebereich Grundsatz A1")
    private String lhmOULongname;

    @Schema(description = "DN der organisatorischen Einheit der Person", example = "ou=Servicebereich Grundsatz A1,ou=Referat,o=Landeshauptstadt München,c=de")
    private String lhmObjectPath;

    @Schema(description = "Langname der organisatorischen übergeordneten Einheit der Person", example = "Geschäftsbereich A")
    private String lhmOberOrga;

    @Schema(description = "Kurzbezeichnung des Referats der Person", example = "REF")
    private String lhmReferatName;

    @Schema(description = "Funktionsbezeichnung der Person", example = "Sachbearbeitung A-Z")
    private String lhmFunctionalTitle;

    @Schema(description = "Amtskurzbezeichnung der Person", example = "Techn. Rat")
    private String amtsbezeichnung;

    @Schema(description = "Erreibarkeitszeiten der Person (z.B. bei Teilzeit)", example = "Mo-Mi")
    private String erreichbarkeit;

    @Schema(description = "Mailadresse der Person", example = "erika.musterfrau@muenchen.de")
    private String mail;

    @Schema(description = "Mailadresse der Organisationseinheit der Person", example = "ref.a1@muenchen.de")
    private String lhmOrgaMail;

    @Schema(description = "Festnetztelefonnummer", example = "99998 000")
    private String telephoneNumber;

    @Schema(description = "Faxnummer", example = "99998 001")
    private String facsimileTelephoneNumber;

    @Schema(description = "Mobilnummer", example = "0152-28-817386")
    private String mobile;

    @Schema(description = "Zimmernummer", example = "EG.001")
    private String zimmer;

    @Schema(description = "Postanschrift")
    private AnschriftDTO postanschrift;

    @Schema(description = "Büroanschrift")
    private AnschriftDTO bueroanschrift;

    @Schema(description = "Akademische Titel", example = "Dr.")
    private String personalTitle;

}
