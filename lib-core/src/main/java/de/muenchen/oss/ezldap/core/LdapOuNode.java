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

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Node class to create ldap shade tree representation
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = false, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false)
public class LdapOuNode {

    private static final long serialVersionUID = 1L;

    private String distinguishedName;
    private LdapOuSearchResultDTO node;
    private Map<String, LdapOuNode> childNodes = new TreeMap<String, LdapOuNode>();
    private List<LdapUserDTO> users;

    public String logTree(String tab) {

        var tree = new StringBuilder();
        tree.append(tab + "***** New LDAP entry : " + getNode().getLhmOUShortname() + " " + getNode().getOu() + " *****" + System.lineSeparator());
        tree.append(tab + getDistinguishedName() + System.lineSeparator());
        tree.append(tab + getNode().toString() + System.lineSeparator());

        if (getUsers() != null)
            getUsers().forEach(u -> tree.append(tab + u.toString() + System.lineSeparator()));

        getChildNodes().forEach((k, v) -> {
            tree.append(v.logTree(tab + "     "));
        });

        return tree.toString();
    }

}
