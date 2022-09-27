/*
 *   number-routing-management - ADFunctions.java
 *
 *   Copyright (c) 2022-2022, Slinky Software
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
package com.slinkytoybox.numberroutingmanagement.businesslogic;

import com.slinkytoybox.numberroutingmanagement.activedirectory.ADConnection;
import com.slinkytoybox.numberroutingmanagement.activedirectory.ADTypes;
import com.slinkytoybox.numberroutingmanagement.activedirectory.ADUser;
import com.slinkytoybox.numberroutingmanagement.activedirectory.exceptions.ActiveDirectoryException;
import com.slinkytoybox.numberroutingmanagement.dto.ADUserDTO;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Component
@Slf4j
public class ADFunctions {

    private ADConnection adConn;
    
    @Autowired
    private Environment env;

    @PostConstruct
    public void createADConnection() {
        final String logPrefix = "createADConnection() - ";
        log.trace("{}Entering method", logPrefix);
        log.debug("{}Retrieving AD connection parameters", logPrefix);

        String ldapServer = env.getProperty("active-directory.server", "").trim();
        int ldapPort = Integer.parseUnsignedInt(env.getProperty("active-directory.port", "0").trim());
        String connectionMode = env.getProperty("active-directory.security", "none").toUpperCase().trim();
        ADTypes.ConnectionSecurityMode csm;

        switch (connectionMode) {
            case "NONE":
            case "TLS":
            case "SSL":
                csm = ADTypes.ConnectionSecurityMode.valueOf(connectionMode);
                break;

            default:
                throw new IllegalArgumentException("active-directory.security=" + connectionMode + " is unsupported!");
        }

        String bindUser = env.getProperty("active-directory.bind-user", "").trim();
        String bindPass = env.getProperty("active-directory.bind-pass", "").trim();
        log.info("{}Creating Active Directory server connection for {}:{}", logPrefix, ldapServer, ldapPort);
        adConn = new ADConnection(ldapServer, ldapPort, csm, bindUser, bindPass);
        log.trace("{}Leaving method", logPrefix);
    }
    
    public ADConnection getConnection() {
        final String logPrefix = "getConnection() - ";
        log.trace("{}Entering method", logPrefix);
        return adConn;
    }

    private ADUser lookupUser(String username) throws ActiveDirectoryException {
        final String logPrefix = "lookupUser() - ";
        log.trace("{}Entering method", logPrefix);
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username must be supplied");

        }
        String lookupMode = env.getProperty("active-directory.search-mode", "UPN").toUpperCase().trim();
        ADTypes.AccountType at;

        switch (lookupMode) {
            case "UPN":
            case "DN":
            case "CN":
            case "SAMACCOUNTNAME":
                at = ADTypes.AccountType.valueOf(lookupMode);
                break;

            default:
                throw new IllegalArgumentException("active-directory.search-mode=" + lookupMode + " is unsupported!");
        }
        String searchBase = env.getProperty("active-directory.search-base", "").trim();

        log.info("{}Looking up AD user {}", logPrefix, username);
        ADUser adUser;
        try {
            adUser = ADUser.findADUser(adConn, searchBase, username, at);
        }
        catch (ActiveDirectoryException ex) {
            log.error("{}AD Exception occurred whilst looking up user", logPrefix, ex);
            throw ex;
        }
        return adUser;
    }

    public ADUserDTO getADUser(String username) {
        final String logPrefix = "getADUser() - ";
        log.trace("{}Entering method", logPrefix);
        ADUser adUser;
        ADUserDTO ad = new ADUserDTO();

        try {
            adUser = lookupUser(username);
        }
        catch (ActiveDirectoryException ex) {
            return ad;
        }

        ad
                .setAccountDisabled(adUser.isAccountDisabled())
                .setAccountLocked(adUser.isAccountLocked())
                .setAddress(adUser.getAddress())
                .setCN(adUser.getCN())
                .setCity(adUser.getCity())
                .setCompany(adUser.getCompany())
                .setCountry(adUser.getCountry())
                .setDN(adUser.getDN())
                .setDepartment(adUser.getDepartment())
                .setEmail(adUser.getEmail())
                .setFirstName(adUser.getFirstName())
                .setManager(adUser.getManager())
                .setOffice(adUser.getOffice())
                .setPosition(adUser.getPosition())
                .setPostcode(adUser.getPostcode())
                .setSAMAccountName(adUser.getSAMAccountName())
                .setState(adUser.getState())
                .setSurname(adUser.getSurname())
                .setUserPrincipalName(adUser.getUserPrincipalName());
        return ad;
    }

}
