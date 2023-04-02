/*
 *   NumberRoutingManagement - ADUser.java
 *
 *   Copyright (c) 2022-2023, Slinky Software
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   A copy of the GNU Affero General Public License is located in the 
 *   AGPL-3.0.md supplied with the source code.
 *
 */
package com.slinkytoybox.numberroutingmanagement.activedirectory;

import com.slinkytoybox.numberroutingmanagement.activedirectory.ADTypes.*;
import com.slinkytoybox.numberroutingmanagement.activedirectory.exceptions.*;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
@Data

public class ADUser {

    private String DN = "";                 // distinguishsedName
    private String CN = "";                 // cn
    private String userPrincipalName = "";  // userPrincipalName
    private String firstName = "";          // givenName
    private String surname = "";            // sn
    private String sAMAccountName = "";     // sAMAccountName
    private String email = "";              // mail
    private String company = "";            // company
    private String position = "";           // title
    private String department = "";         // department
    private String address = "";            // streetAddress
    private String city = "";               // l
    private String country = "";            // co
    private String state = "";              // st
    private String postcode = "";           // postalCode
    private String office = "";             // physicalDeliveryOfficeName
    private String manager = "";            // manager
    private boolean accountDisabled = false;
    private boolean accountLocked = false;

    public static ADUser findADUser(ADConnection ldapConn, String searchBase, String searchData, AccountType inputType) throws ActiveDirectoryException {
        final String logPrefix = "ctor() - ";
        log.trace("{}Entering method", logPrefix);
        if (ldapConn == null) {
            log.error("{}No valid LDAP connection specified", logPrefix);
            throw new IllegalArgumentException("No valid LDAP connection specified");
        }
        if (searchBase == null || searchBase.isEmpty()) {
            log.error("{}No valid search base specified", logPrefix);
            throw new IllegalArgumentException("No valid search base specified");
        }
        ADUser newUser = new ADUser();

        log.debug("{}LDAP URL: {}", logPrefix, ldapConn.getLdapURL());
        log.debug("{}Search Base: {}", logPrefix, searchBase);

        if (searchData == null || searchData.isEmpty() || inputType == null) {
            log.error("{}Search data not specified", logPrefix);
            throw new IllegalArgumentException("Search data not specified");
        }
        log.debug("{}Searching type: {} - search data: {}", logPrefix, inputType.name(), searchData);

        DirContext context = ldapConn.getContext();
        SearchControls params = new SearchControls();
        params.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String searchString = "";
        switch (inputType) {
            case SAMACCOUNTNAME:
                searchString = "(&(sAMAccountName=" + searchData + ")(objectClass=person))";
                break;
            case DN:
                searchString = "(&(distinguishedName=" + searchData + ")(objectClass=person))";
                break;
            case CN:
                searchString = "(&(CN=" + searchData + ")(objectClass=person))";
                break;
            case UPN:
                searchString = "(&(userPrincipalName=" + searchData + ")(objectClass=person))";
                break;
            default:
                log.error("{}Search type not implemented", logPrefix);
                throw new java.lang.UnsupportedOperationException("Search type not implemented");
        }
        try {
            log.info("{}Issuing Search: {}", logPrefix, searchString);
            NamingEnumeration<SearchResult> ne;

            ne = context.search(searchBase, searchString, params);

            log.debug("{}Search Complete", logPrefix);
            while (ne.hasMoreElements()) {
                SearchResult sr = ne.nextElement();
                log.debug("{}Found result: {}", logPrefix, sr.getName());
                log.debug("{}Getting all attributes", logPrefix);
                Attributes a = sr.getAttributes();
                if (a.get("userPrincipalName") != null) {
                    newUser.userPrincipalName = (String) a.get("userPrincipalName").get();
                }
                if (a.get("CN") != null) {
                    newUser.CN = (String) a.get("CN").get();
                }
                if (a.get("distinguishedName") != null) {
                    newUser.DN = (String) a.get("distinguishedName").get();
                }
                if (a.get("givenName") != null) {
                    newUser.firstName = (String) a.get("givenName").get();
                }
                if (a.get("mail") != null) {
                    newUser.email = (String) a.get("mail").get();
                }
                if (a.get("sn") != null) {
                    newUser.surname = (String) a.get("sn").get();
                }
                if (a.get("sAMAccountName") != null) {
                    newUser.sAMAccountName = (String) a.get("sAMAccountName").get();
                }
                if (a.get("company") != null) {
                    newUser.company = (String) a.get("company").get();
                }
                if (a.get("title") != null) {
                    newUser.position = (String) a.get("title").get();
                }
                if (a.get("department") != null) {
                    newUser.department = (String) a.get("department").get();
                }
                if (a.get("streetAddress") != null) {
                    newUser.address = (String) a.get("streetAddress").get();
                }
                if (a.get("l") != null) {
                    newUser.city = (String) a.get("l").get();
                }
                if (a.get("st") != null) {
                    newUser.state = (String) a.get("st").get();
                }
                if (a.get("postalCode") != null) {
                    newUser.postcode = (String) a.get("postalCode").get();
                }
                if (a.get("physicalDeliveryOfficeName") != null) {
                    newUser.office = (String) a.get("physicalDeliveryOfficeName").get();
                }
                if (a.get("manager") != null) {
                    newUser.manager = (String) a.get("manager").get();
                }
                if (a.get("co") != null) {
                    newUser.country = (String) a.get("co").get();
                }
                
                if (a.get("userAccountControl") != null) {
                    int uac = Integer.parseInt((String) a.get("userAccountControl").get());
                    newUser.accountDisabled = (uac & 0x2) != 0;
                    newUser.accountLocked = (uac & 0x10) != 0;
                }
                log.info("{}{}", logPrefix, newUser);
                return newUser;
            }
            log.warn("{}No results found", logPrefix);
            throw new NoSuchUserException("Unable to find user in active directory");
        }

        catch (NamingException ex) {
            log.error("{}Search failed", logPrefix, ex);
            throw new LookupException("Unable to find user in active directory", ex);
        }
        finally {
            log.debug("{}Returning AD context to pool", logPrefix);
            ldapConn.returnContext(context);

        }

    }

}
