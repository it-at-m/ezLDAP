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
package de.muenchen.oss.ezldap.client.v1;

import java.io.Serializable;

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
@ToString(callSuper = false, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false)
public class LdapBaseUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ToString.Include(rank = 0)
    private String lhmObjectId;

    @ToString.Include(rank = 1)
    private String uid;

    /**
     * Anrede
     */
    private String anrede;

    /**
     * Vorname
     */
    private String vorname;

    /**
     * Nachname
     */
    private String nachname;

    /**
     * Vor- und Nachname.
     */
    @ToString.Include(rank = 2)
    private String cn;

    /**
     * OU Kurzbezeichnung (z.B. ITM-KM23)
     */
    @ToString.Include(rank = 3)
    private String ou;

}
