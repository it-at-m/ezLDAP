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

import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.prankl
 *
 */
@Slf4j
public class LdapBaseUserAttributesMapper implements AttributesMapper<LdapBaseUserDTO> {

    private static final Pattern DOUBLE_PERCENTAGE_PATTERN = Pattern.compile("\\%\\%");

    @Override
    public LdapBaseUserDTO mapFromAttributes(Attributes attributes) throws NamingException {
        LdapBaseUserDTO u = new LdapBaseUserDTO();

        u.setLhmObjectId(safelyGet("lhmObjectId", attributes));
        u.setUid(safelyGet("uid", attributes));
        u.setAnrede(safelyGet("lhmTitle", attributes));
        u.setVorname(safelyGet("givenName", attributes));
        u.setNachname(safelyGet("sn", attributes));
        u.setCn(safelyGet("cn", attributes));
        u.setOu(safelyGet("ou", attributes));
        u.setModifyTimeStamp(safelyGet("modifyTimestamp", attributes));
        u.setLhmObjectReference(safelyGet("lhmObjectReference", attributes));
        log.debug("Mapped user {}.", u);
        return u;
    }

    /**
     * Liest den Value zu Attribut und ersetzt '%%' mit Line-Breaks.
     *
     * @param key the object key
     * @param attributes the {@link Attributes}
     * @return the value or <code>null</code>
     */
    public static String safelyGet(String key, Attributes attributes) {
        String value = null;
        Attribute a = attributes.get(key);
        if (a != null) {
            try {
                value = (String) a.get();
                if (value != null && value.contains("%%")) {
                    log.debug("Replacing '%%' in value {} with line-breaks", value);
                    value = DOUBLE_PERCENTAGE_PATTERN.matcher(value).replaceAll("\n");
                }
            } catch (NamingException e) {
                log.debug(String.format("Exception while accessing attribute '%s'"), key);
            }
        }
        log.debug("Resolved attribute['{}']: {}", key, value);
        return value;
    }

}
