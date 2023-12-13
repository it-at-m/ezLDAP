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
@Schema(name = "LdapBaseUserDTO", description = "Basic Representation einer Person")
public class LdapBaseUserDTO {

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

    /**
     * OU Kurzbezeichnung (z.B. ITM-KM23)
     */
    @ToString.Include(rank = 3)
    @Schema(description = "Kürzel der organisatorischen Einheit der Person", example = "REF-A1")
    private String ou;

}
