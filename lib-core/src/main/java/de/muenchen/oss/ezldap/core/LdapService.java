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

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;

import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.prankl
 *
 */
@Slf4j
public class LdapService {

    public static final int MAX_SEARCH_RESULTS = 100;

    private static final String ATTRIBUTE_UID = "uid";
    private static final String ATTRIBUTE_OU = "ou";
    private static final String ATTRIBUTE_LHM_OBJECT_ID = "lhmObjectId";
    private static final String ATTRIBUTE_OBJECT_CLASS = "objectClass";
    private static final String LHM_ORGANIZATIONAL_UNIT = "lhmOrganizationalUnit";
    private static final String LHM_OU_SHORTNAME = "lhmOUShortname";
    private static final String LHM_PERSON = "lhmPerson";
    private static final String LHM_OBJECT_PATH = "lhmObjectPath";
    private static final String PERSON = "person";

    private final LdapTemplate ldapTemplate;
    private final LdapUserAttributesMapper ldapUserAttributesMapper;
    private final LdapBaseUserAttributesMapper ldapBaseUserAttributesMapper;
    private final LdapOuAttributesMapper ldapOuAttributesMapper;
    private final DtoMapper mapper;
    private final String userSearchBase;
    private final String ouSearchBase;

    /**
     * Erzeugt eine neue Instanz.
     *
     * @param ldapTemplate ein {@link LdapTemplate} für LDAP
     * @param ldapUserAttributesMapper ein {@link LdapUserAttributesMapper}
     * @param ldapBaseUserAttributesMapper ein {@link LdapBaseUserAttributesMapper}
     * @param ldapOuAttributesMapper ein {@link LdapOuAttributesMapper}
     * @param modelMapper ein {@link DtoMapper}
     * @param userSearchBase Search-Base für User (DN)
     * @param ouSearchBase Search-Base für OUs (DN)
     */
    public LdapService(final LdapTemplate ldapTemplate,
            final LdapUserAttributesMapper ldapUserAttributesMapper,
            final LdapBaseUserAttributesMapper ldapBaseUserAttributesMapper,
            final LdapOuAttributesMapper ldapOuAttributesMapper,
            final DtoMapper modelMapper,
            final String userSearchBase,
            final String ouSearchBase) {
        this.ldapTemplate = ldapTemplate;
        this.ldapUserAttributesMapper = ldapUserAttributesMapper;
        this.ldapBaseUserAttributesMapper = ldapBaseUserAttributesMapper;
        this.ldapOuAttributesMapper = ldapOuAttributesMapper;
        this.mapper = modelMapper;
        this.userSearchBase = userSearchBase;
        this.ouSearchBase = ouSearchBase;
    }

    /**
     * Erzeugt eine Instanz.
     *
     * @param ldapUrl die LDAP-URL (z.B. 'ldaps://ldap.example.org:636')
     * @param ldapUserDn LDAP-Zugangsuser (DN)
     * @param ldapPassword LDAP-Zugangsuser Passwort
     * @param userSearchBase die Search-Base für User (z.B. 'o=example,c=org')
     * @param ouSearchBase die Search-Base für OU's (z.B. 'o=example,c=org')
     */
    public LdapService(final String ldapUrl, final String ldapUserDn, final String ldapPassword, final String userSearchBase,
            final String ouSearchBase) {
        final LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(ldapUrl);
        ldapContextSource.setUserDn(ldapUserDn);
        ldapContextSource.setPassword(ldapPassword);
        ldapContextSource.afterPropertiesSet();
        this.ldapTemplate = new LdapTemplate(ldapContextSource);
        this.ldapBaseUserAttributesMapper = new LdapBaseUserAttributesMapper();
        this.ldapOuAttributesMapper = new LdapOuAttributesMapper();
        this.ldapUserAttributesMapper = new LdapUserAttributesMapper(this.ldapBaseUserAttributesMapper);
        this.mapper = new DtoMapperImpl();
        this.userSearchBase = userSearchBase;
        this.ouSearchBase = ouSearchBase;
    }

    /**
     * Ruft die Person zur angegebenen lhmObjectID ab.
     *
     * @param lhmObjectId eine LHM Object ID
     * @return den Mitarbeiter als {@link LdapUserDTO}
     */
    public Optional<LdapUserDTO> getPerson(final String lhmObjectId) {
        log.info("Searching LDAP for Person[lhmObjectId={}]...", lhmObjectId);
        final LdapQuery query = LdapQueryBuilder.query().base(this.userSearchBase).countLimit(1).searchScope(SearchScope.SUBTREE)
                .where(ATTRIBUTE_OBJECT_CLASS).is(PERSON)
                .and(ATTRIBUTE_OBJECT_CLASS).is(LHM_PERSON)
                .and(ATTRIBUTE_LHM_OBJECT_ID).is(lhmObjectId);
        final List<LdapUserDTO> searchResults = this.ldapTemplate.search(query, this.ldapUserAttributesMapper);
        if (searchResults.size() == 1) {
            return Optional.of(searchResults.get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Ruft die Person zur angegebenen lhmObjectID ab.
     *
     * @param uid eine UID ("vorname.nachname")
     * @return den Mitarbeiter als {@link LdapUserDTO}
     */
    public Optional<LdapUserDTO> getPersonWithUID(final String uid) {
        log.info("Searching LDAP for Person[uid={}]...", uid);
        final LdapQuery query = LdapQueryBuilder.query().base(this.userSearchBase).countLimit(1).searchScope(SearchScope.SUBTREE)
                .where(ATTRIBUTE_OBJECT_CLASS).is(PERSON)
                .and(ATTRIBUTE_OBJECT_CLASS).is(LHM_PERSON)
                .and(ATTRIBUTE_LHM_OBJECT_ID).isPresent() // es gibt Personen ohne lhmObjectId ¯\_(ツ)_/¯
                .and(ATTRIBUTE_UID).is(uid);
        final List<LdapUserDTO> searchResults = this.ldapTemplate.search(query, this.ldapUserAttributesMapper);
        if (searchResults.size() == 1) {
            return Optional.of(searchResults.get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Sucht alle Personen, die der angegeben Organisationseinheit zugeordnet sind.
     *
     * @param ou die Kurzbezeichnung der OU (z.B. "ITM-KM21")
     * @return die Ergebnisliste oder {@link Optional#empty()}, wenn keine OU zur Kurzbezeichnung
     *         existiert
     */
    public Optional<List<LdapBaseUserDTO>> findPersonsByOuShortcode(final String ou) {
        log.info("Performing LDAP lookup for persons in ou='{}' ...", ou);
        // check if OU exists before searching for users
        final LdapQuery queryForOu = LdapQueryBuilder.query().base(this.ouSearchBase)
                .searchScope(SearchScope.SUBTREE)
                .where(ATTRIBUTE_OBJECT_CLASS).is(LHM_ORGANIZATIONAL_UNIT)
                .and(LHM_OU_SHORTNAME).is(ou);
        final List<String> foundOus = this.ldapTemplate.search(queryForOu,
                (AttributesMapper<String>) attributes -> (String) attributes.get(LHM_OU_SHORTNAME).get());
        log.debug("Found OUs for shortcode '{}': {}", ou, foundOus);
        if (foundOus.isEmpty()) {
            return Optional.empty();
        } else {
            final LdapQuery query = LdapQueryBuilder.query().base(this.userSearchBase)
                    .searchScope(SearchScope.SUBTREE)
                    .where(ATTRIBUTE_OBJECT_CLASS).is(PERSON)
                    .and(ATTRIBUTE_OBJECT_CLASS).is(LHM_PERSON)
                    .and(ATTRIBUTE_LHM_OBJECT_ID).isPresent() // es gibt Personen ohne lhmObjectId ¯\_(ツ)_/¯
                    .and(ATTRIBUTE_OU).is(ou);
            return Optional.of(this.ldapTemplate.search(query, this.ldapBaseUserAttributesMapper));
        }

    }

    /**
     * Sucht nach Personen, deren UID mit der angegeben Phrase beginnt.
     *
     * @param searchPhrase die Suchphrase (mindestens 3 Zeichen, * Zählen nicht)
     * @param resultLimit maximale Treffernzahl
     * @return die Ergebnisliste
     * @throws IllegalArgumentException wenn die Suchphrase zu kurz ist
     */
    public List<LdapBaseUserDTO> searchFor(final String searchPhrase, int resultLimit) {
        if (searchPhrase == null || searchPhrase.replace("*", "").trim().length() < 3) {
            throw new IllegalArgumentException("Keine Suchphrase angegeben bzw. die Suchphrase ist zu kurz");
        }
        log.info("Performing LDAP lookup for uid like {}...", searchPhrase);
        if (resultLimit > MAX_SEARCH_RESULTS) {
            log.warn("Angegebenes Result-Limit ist größer als das erlaubte Maximallimit.");
            resultLimit = MAX_SEARCH_RESULTS;
        }
        final LdapQuery query = LdapQueryBuilder.query().base(this.userSearchBase).countLimit(resultLimit)
                .searchScope(SearchScope.SUBTREE)
                .where(ATTRIBUTE_OBJECT_CLASS).is(PERSON)
                .and(ATTRIBUTE_OBJECT_CLASS).is(LHM_PERSON)
                .and(ATTRIBUTE_LHM_OBJECT_ID).isPresent() // es gibt Personen ohne lhmObjectId ¯\_(ツ)_/¯
                .and(ATTRIBUTE_UID).like(searchPhrase);
        return this.ldapTemplate.search(query, this.ldapBaseUserAttributesMapper);
    }

    /**
     * Holt die Daten zu einer Ou.
     *
     * @param lhmObjectId der gewünschten Ou
     * @return Daten der Ou
     */
    public Optional<LdapOuDTO> getOu(final String lhmObjectId) {
        log.info("Searching LDAP for Ou[lhmObjectId={}]...", lhmObjectId);
        final LdapQuery query = LdapQueryBuilder.query().base(this.ouSearchBase).countLimit(1).searchScope(SearchScope.SUBTREE)
                .where(ATTRIBUTE_OBJECT_CLASS).is(LHM_ORGANIZATIONAL_UNIT)
                .and(ATTRIBUTE_LHM_OBJECT_ID).is(lhmObjectId);
        final List<LdapOuSearchResultDTO> searchResults = this.ldapTemplate.search(query, this.ldapOuAttributesMapper);
        if (searchResults.size() == 1) {
            final LdapOuSearchResultDTO searchResultDTO = searchResults.get(0);
            log.debug("Found OU with [lhmObjectId={}]: {}", lhmObjectId, searchResultDTO);
            return this.resolveManagersForOu(searchResultDTO);
        } else {
            log.debug("Found no OU with [lhmObjectId={}].", lhmObjectId);
            return Optional.empty();
        }
    }

    /**
     * Holt die Daten zu einer OU.
     *
     * @param ou OU-Shortcode der Organisationseinheit (z.B. "ITM-KM21")
     * @return Daten der Ou
     */
    public Optional<LdapOuDTO> findOuByShortcode(final String ou) {
        log.info("Searching LDAP for ou[lhmOUShortname={}]...", ou);
        final LdapQuery query = LdapQueryBuilder.query().base(this.ouSearchBase).countLimit(1).searchScope(SearchScope.SUBTREE)
                .where(ATTRIBUTE_OBJECT_CLASS).is(LHM_ORGANIZATIONAL_UNIT)
                .and(LHM_OU_SHORTNAME).is(ou);
        final List<LdapOuSearchResultDTO> searchResults = this.ldapTemplate.search(query, this.ldapOuAttributesMapper);
        if (searchResults.size() == 1) {
            final LdapOuSearchResultDTO searchResultDTO = searchResults.get(0);
            log.debug("Found OU with [lhmOUShortname={}]: {}", ou, searchResultDTO);
            return this.resolveManagersForOu(searchResultDTO);
        } else {
            log.debug("Found no OU with [lhmOUShortname={}].", ou);
            return Optional.empty();
        }
    }

    /**
     * Liest den OU Tree (Abteilungsbaum) eines user ein
     *
     * @param lhmObjectId des Users
     * @return OU Tree
     */
    public Optional<List<String>> findOuTreeByUserId(final String lhmObjectId) {
        log.debug("Get LDAP ou tree for user {}.", lhmObjectId);
        final LdapQuery query = query()
                .searchScope(SearchScope.SUBTREE)
                .base(this.userSearchBase)
                .where(ATTRIBUTE_LHM_OBJECT_ID).is(lhmObjectId);
        return this.findOuTree(query);
    }

    /**
     * Liest den OU Tree (Abteilungsbaum) einer OU ein
     *
     * @param ouShortCode Shortcode der OU (z.B. ITM-KM21)
     * @return OU Tree
     */
    public Optional<List<String>> findOuTreeByOuShortCode(final String ouShortCode) {
        log.debug("Get LDAP ou tree for ou {}.", ouShortCode);
        final LdapQuery ouQuery = query()
                .searchScope(SearchScope.SUBTREE)
                .base(this.userSearchBase)
                .countLimit(1)
                .where("cn").is(ouShortCode);
        return this.findOuTree(ouQuery);
    }

    /**
     * Helper method to find the ou tree for a given query. The query can be a user or ou query.
     *
     * @see #findOuTreeByUserId
     * @see #findOuTreeByOuShortCode
     *
     * @param query
     * @return OU Tree
     */
    private Optional<List<String>> findOuTree(final LdapQuery query) {
        List<LdapName> ldapNames = this.ldapTemplate.search(query, (AttributesMapper<LdapName>) attrs -> {
            if (null != attrs.get(LHM_OBJECT_PATH)) {
                return new LdapName((String) attrs.get(LHM_OBJECT_PATH).get());
            }
            return null;
        });
        // clean ldapNames from null values
        ldapNames = ldapNames.stream().filter(Objects::nonNull).toList();
        if (ldapNames.isEmpty()) {
            log.debug("Found no ou tree");
            return Optional.empty();
        }

        final LdapName ldapName = ldapNames.get(0);

        final List<String> ouTree = new ArrayList<>();

        for (int i = 1; i <= ldapName.getRdns().size(); i++) {
            final Name partialDN = ldapName.getPrefix(i);

            try {
                log.debug("Searching for dn='{} & objectClass='{}' ...", partialDN, LHM_ORGANIZATIONAL_UNIT);
                final LdapQuery ouObjectReferenceQuery = query()
                        .searchScope(SearchScope.OBJECT)
                        .base(partialDN)
                        .countLimit(1)
                        .where(ATTRIBUTE_OBJECT_CLASS).is(LHM_ORGANIZATIONAL_UNIT);
                List<String> ouShortnames = this.ldapTemplate.search(ouObjectReferenceQuery, (AttributesMapper<String>) attrs -> {
                    if (null != attrs.get(LHM_OU_SHORTNAME)) {
                        return (String) attrs.get(LHM_OU_SHORTNAME).get();
                    }
                    return null;
                });
                ouTree.addAll(ouShortnames.stream().filter(Objects::nonNull).toList());
            } catch (final NameNotFoundException ex) {
                log.warn("No shortCode found for dn {}. Query failed with {} exception", partialDN, ex.getClass().getName());
            }
        }
        ouTree.replaceAll(String::toUpperCase);
        return Optional.of(ouTree);
    }

    private Optional<LdapOuDTO> resolveManagersForOu(final LdapOuSearchResultDTO searchResultDTO) {
        if (searchResultDTO.getLhmOUManager() != null || searchResultDTO.getLhmOU2ndManager() != null) {
            // Manager Attribute sind gesetzt, ermittle Leitung/Stvt. darüber
            return Optional.of(this.ermittleLeitungByManagerAttributes(searchResultDTO));
        } else {
            // suche Personen in der OU mit lhmRankInOu = 01 / 03 (Leitung / Stellvertretung)
            return Optional.of(this.ermittleLeitungByRankInOu(searchResultDTO));
        }
    }

    /**
     * Ermittelt die Leitung/Stellvertretung zur OU, in dem Personen in der OU gesucht werden, die
     * mit dem Attribut lhmRankInOU und Wert 01 (Leitung) bzw. 03 (Stellvertretung) versehen sind.
     *
     * @param searchResultDTO das {@link LdapOuSearchResultDTO}
     * @return das gemappte {@link LdapOuDTO}
     */
    private LdapOuDTO ermittleLeitungByRankInOu(final LdapOuSearchResultDTO searchResultDTO) {
        final LdapOuDTO ouDTO = this.mapper.toLdapOuDTO(searchResultDTO);
        final Optional<LdapUserDTO> leitung = this.lookupPersonInOuWithRank(searchResultDTO.getLhmObjectId(), "01");
        if (leitung.isPresent()) {
            log.debug("Found Leitung (lhmRankInOU=01): {}", leitung.get().getUid());
            ouDTO.setLeitung(leitung.get());
        } else {
            log.debug("No Leitung found (no person in OU with lhmRankInOu=01).");
        }
        final Optional<LdapUserDTO> stellvertretung = this.lookupPersonInOuWithRank(searchResultDTO.getLhmObjectId(), "03");
        if (stellvertretung.isPresent()) {
            log.debug("Found Stellvertretung (lhmRankInOu=03): {}", stellvertretung.get().getUid());
            ouDTO.setStellvertretung(stellvertretung.get());
        } else {
            log.debug("No Stellvertretung found (no person in OU with lhmRankInOu=03).");
        }
        return ouDTO;
    }

    private Optional<LdapUserDTO> lookupPersonInOuWithRank(final String lhmObjectIdOfOu, final String rankMarker) {
        final LdapQuery query = LdapQueryBuilder.query().base(this.userSearchBase).countLimit(1)
                .searchScope(SearchScope.SUBTREE)
                .where(ATTRIBUTE_OBJECT_CLASS).is(PERSON)
                .and(ATTRIBUTE_OBJECT_CLASS).is(LHM_PERSON)
                .and(ATTRIBUTE_LHM_OBJECT_ID).isPresent() // es gibt Personen ohne lhmObjectId ¯\_(ツ)_/¯
                .and("lhmObjectReference").is(lhmObjectIdOfOu)
                .and("lhmRankInOu").is(rankMarker);
        final List<LdapUserDTO> searchResult = this.ldapTemplate.search(query, this.ldapUserAttributesMapper);
        if (searchResult.size() == 1) {
            return Optional.of(searchResult.get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Ermittelt die Leitung/Stellvertrung zur OU mittels dem im SearchResult vorhandenen Attributen
     * lhmOUManager und lhmOU2ndManager.
     *
     * @param searchResultDTO das {@link LdapOuSearchResultDTO}
     * @return das gemappte {@link LdapOuDTO}
     */
    private LdapOuDTO ermittleLeitungByManagerAttributes(final LdapOuSearchResultDTO searchResultDTO) {
        final LdapOuDTO ouDTO = this.mapper.toLdapOuDTO(searchResultDTO);
        if (searchResultDTO.getLhmOUManager() != null) {
            log.debug("Looking up lhmOUManager = {} ...", searchResultDTO.getLhmOUManager());
            final Optional<LdapUserDTO> manager = this.getPersonWithUID(searchResultDTO.getLhmOUManager());
            if (manager.isPresent()) {
                log.debug("Found lhmOUManager person: {}", manager.get());
                ouDTO.setLeitung(manager.get());
            } else {
                log.debug("lhmOUManager - No person found for uid: {}", searchResultDTO.getLhmOUManager());
            }
        }
        if (searchResultDTO.getLhmOU2ndManager() != null) {
            log.debug("Looking up lhmOU2ndManager = {} ...", searchResultDTO.getLhmOU2ndManager());
            final Optional<LdapUserDTO> manager = this.getPersonWithUID(searchResultDTO.getLhmOU2ndManager());
            if (manager.isPresent()) {
                log.debug("Found lhmOU2ndManager person: {}", manager.get());
                ouDTO.setStellvertretung(manager.get());
            } else {
                log.debug("lhmOU2ndManager - No person found for uid: {}", searchResultDTO.getLhmOU2ndManager());
            }
        }
        return ouDTO;
    }

}
