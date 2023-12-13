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

import static de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper.safelyGet;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.prankl
 */
@Slf4j
public class LdapUserAttributesMapper implements AttributesMapper<LdapUserDTO> {

    private final LdapBaseUserAttributesMapper baseAttributeMapper;

    /**
     * Erzeugt eine Instanze
     *
     * @param baseAttributeMapper ein {@link LdapBaseUserAttributesMapper}
     */
    public LdapUserAttributesMapper(LdapBaseUserAttributesMapper baseAttributeMapper) {
        this.baseAttributeMapper = baseAttributeMapper;
    }

    @Override
    public LdapUserDTO mapFromAttributes(Attributes attributes) throws NamingException {
        LdapBaseUserDTO baseUser = this.baseAttributeMapper.mapFromAttributes(attributes);
        LdapUserDTO u = new LdapUserDTO(baseUser);
        u.setBueroanschrift(new AnschriftDTO());
        u.setPostanschrift(new AnschriftDTO());

        u.setLhmOULongname(safelyGet("lhmOULongname", attributes));
        u.setLhmObjectPath(safelyGet("lhmObjectPath", attributes));
        u.setLhmOberOrga(safelyGet("lhmOberOrga", attributes));
        u.setLhmReferatName(safelyGet("lhmReferatName", attributes));
        u.setLhmFunctionalTitle(safelyGet("lhmFunctionalTitle", attributes));
        u.setAmtsbezeichnung(safelyGet("title", attributes));
        u.setErreichbarkeit(safelyGet("lhmWorkHours", attributes));
        u.setMail(safelyGet("mail", attributes));
        u.setLhmOrgaMail(safelyGet("lhmOrgaMail", attributes));
        u.setTelephoneNumber(safelyGet("telephoneNumber", attributes));
        u.setFacsimileTelephoneNumber(safelyGet("facsimileTelephoneNumber", attributes));
        u.setMobile(safelyGet("mobile", attributes));
        u.setZimmer(safelyGet("roomNumber", attributes));
        u.setPersonalTitle(safelyGet("personalTitle", attributes));

        // Anschriften
        u.getPostanschrift().setOrt(safelyGet("l", attributes));
        u.getPostanschrift().setPlz(safelyGet("postalCode", attributes));
        u.getPostanschrift().setStrasse(safelyGet("street", attributes));
        u.getBueroanschrift().setOrt(safelyGet("lhmOfficeLocalityName", attributes));
        u.getBueroanschrift().setPlz(safelyGet("lhmOfficePostalCode", attributes));
        u.getBueroanschrift().setStrasse(safelyGet("lhmOfficeStreetAddress", attributes));
        log.debug("Mapped user {}.", u);
        return u;
    }

}
