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
package de.muenchen.oss.ezldap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.test.web.servlet.MockMvc;

import de.muenchen.oss.ezldap.core.LdapBaseUserDTO;
import de.muenchen.oss.ezldap.core.LdapService;
import de.muenchen.oss.ezldap.core.LdapUserDTO;

/**
 * @author michael.prankl
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigurationTest {

    @MockBean
    LdapService ldapService;

    @MockBean
    LdapTemplate ldapTemplate;

    @MockBean
    LdapContextSource ldapContextSource;

    @Autowired
    MockMvc mockMvc;

    @Test
    void context_loads_and_cors_correct() throws Exception {
        Mockito.when(ldapService.getPerson(Mockito.anyString()))
                .thenReturn(Optional.of(new LdapUserDTO(new LdapBaseUserDTO())));
        mockMvc.perform(get("/v1/ldap/user/{lhmObjectId}", "111140670").header("Origin", "http://localhost:8081"))
                .andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/ldap/user/{lhmObjectId}", "111140670").header("Origin", "https://app.example.org"))
                .andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/ldap/user/{lhmObjectId}", "111140670").header("Origin", "https://app.example.org:8443"))
                .andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/ldap/user/{lhmObjectId}", "111140670").header("Origin", "https://example.com"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

}
