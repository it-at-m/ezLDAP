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
 * @author m.zollbrecht
 */
@Slf4j
public class LdapOuAttributesMapper implements AttributesMapper<LdapOuSearchResultDTO> {

    @Override
    public LdapOuSearchResultDTO mapFromAttributes(Attributes attributes) throws NamingException {
        LdapOuSearchResultDTO ou = new LdapOuSearchResultDTO();

        ou.setLhmObjectId(safelyGet("lhmObjectId", attributes));
        ou.setOu(safelyGet("ou", attributes));

        ou.setLhmOUKey(safelyGet("lhmOUKey", attributes));
        ou.setLhmOULongname(safelyGet("lhmOULongname", attributes));
        ou.setLhmOUShortname(safelyGet("lhmOUShortname", attributes));

        ou.setPostalCode(safelyGet("postalCode", attributes));
        ou.setStreet(safelyGet("street", attributes));

        ou.setMail(safelyGet("mail", attributes));
        ou.setTelephoneNumber(safelyGet("telephoneNumber", attributes));
        ou.setFacsimileTelephoneNumber(safelyGet("facsimileTelephoneNumber", attributes));

        ou.setLhmOUManager(safelyGet("lhmOUManager", attributes));
        ou.setLhmOU2ndManager(safelyGet("lhmOU2ndManager", attributes));

        log.debug("Mapped ou {}.", ou);
        return ou;
    }

}
