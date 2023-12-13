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
package de.muenchen.oss.ezldap.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Repräsentiert ein LHM-Person im LDAP.
 *
 * @author michael.prankl
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LdapUserDTO extends LdapBaseUserDTO {

    private static final long serialVersionUID = 1L;

    /**
     * Erzeugt eine neue Instanz auf Basis des übergebenen {@link LdapBaseUserDTO}.
     *
     * @param baseUser ein {@link LdapBaseUserDTO}
     */
    public LdapUserDTO(LdapBaseUserDTO baseUser) {
        super(baseUser.getLhmObjectId(), baseUser.getUid(), baseUser.getAnrede(), baseUser.getVorname(),
                baseUser.getNachname(), baseUser.getCn(), baseUser.getOu());
    }

    /**
     * OU Langname (z.B. Servicebereich RBS)
     */
    private String lhmOULongname;

    private String lhmObjectPath;
    private String lhmOberOrga;

    /**
     * lhmReferatName
     */
    private String lhmReferatName;

    /**
     * Funktion
     */
    private String lhmFunctionalTitle;

    /**
     * Amtsbezeichnung
     */
    private String amtsbezeichnung;
    /**
     * Erreichbarkeit
     */
    private String erreichbarkeit;

    private String mail;
    private String lhmOrgaMail;
    private String telephoneNumber;
    private String facsimileTelephoneNumber;
    /**
     * Mobiltelefon
     */
    private String mobile;

    /**
     * Zimmernummer
     */
    private String zimmer;

    private AnschriftDTO postanschrift;
    private AnschriftDTO bueroanschrift;

    private String personalTitle;

}
