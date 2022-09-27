/*
 *   NumberRoutingManagement - ADConnection.java
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
package com.slinkytoybox.numberroutingmanagement.activedirectory;

import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import com.slinkytoybox.numberroutingmanagement.activedirectory.ADTypes.*;
import com.slinkytoybox.numberroutingmanagement.activedirectory.exceptions.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Active Directory Connection Object
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
public class ADConnection {

    private String ldapServer;
    private int ldapPort;
    private ConnectionSecurityMode secMode;

    private Hashtable<String, String> connectionParameters;
    private String ldapURL;

    /**
     * Builds a new Active Directory Connection
     *
     * @param ldapServer Active Directory LDAP Server host name
     * @param port LDAP Port, usually 389 or 636
     * @param securityMode Encryption Required, SSL, TLS or NONE
     * @param username Binding username for queries
     * @param password Binding password
     */
    public ADConnection(String ldapServer, int port, ConnectionSecurityMode securityMode, String username, String password) {
        final String logPrefix = "ctor() - ";
        log.trace("{}Entering method", logPrefix);
        log.debug("{}Creating new ADConnection", logPrefix);

        if (port < 1 || port > 65535) {
            log.error("{}Invalid port number: {}", logPrefix, port);
            throw new IllegalArgumentException("Invalid port number");
        }
        if (ldapServer == null || ldapServer.equals("")) {
            log.error("{}No LDAP Server specified", logPrefix);
            throw new IllegalArgumentException("No LDAP Server specified");
        }
        String protocol = "";
        String secType = "";

        switch (securityMode) {
            case NONE:
                protocol = "ldap://";
                secType = "none";
                break;
            case TLS:
                protocol = "ldap://";
                secType = "ssl";
                break;
            case SSL:
                protocol = "ldaps://";
                secType = "ssl";
                break;
            default:
                log.error("{}Invalid security mode: {}", logPrefix, securityMode.name());
                throw new IllegalArgumentException("Invalid Security Mode");
        }
        this.ldapServer = ldapServer;
        this.ldapPort = port;
        this.secMode = securityMode;

        ldapURL = protocol + ldapServer + ":" + port + "";
        log.info("{}Using LDAP URL: {}", logPrefix, ldapURL);
        connectionParameters = new Hashtable<>();
        connectionParameters.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        connectionParameters.put(Context.PROVIDER_URL, ldapURL);
        connectionParameters.put(Context.SECURITY_PROTOCOL, secType);
        connectionParameters.put(Context.REFERRAL, "follow");
        connectionParameters.put("com.sun.jndi.ldap.connect.pool", "true");

        if (username != null && !username.isEmpty() && (password == null || password.isEmpty())) {
            log.error("{}Username provided but no password specified", logPrefix);
            throw new IllegalArgumentException("No password specified");

        }
        else if (username != null && !username.isEmpty()) {
            log.debug("{}Connecting with credentials: {}", logPrefix, username);
            connectionParameters.put(Context.SECURITY_PRINCIPAL, username);
            connectionParameters.put(Context.SECURITY_CREDENTIALS, password);
            connectionParameters.put(Context.SECURITY_AUTHENTICATION, "simple");

        }
        else {
            log.debug("{}Connecting without authentication", logPrefix);
            connectionParameters.put(Context.SECURITY_AUTHENTICATION, "none");

        }

        log.trace("{}Leaving method", logPrefix);

    }

    DirContext getContext() throws ActiveDirectoryException {
        final String logPrefix = "getContext() - ";
        log.trace("{}Entering method", logPrefix);

        log.info("{}Connecting to Active Directory server", logPrefix);
        DirContext context;
        try {
            context = new InitialDirContext(connectionParameters);
            log.info("{}Sucessfully connected", logPrefix);
            return context;
        }
        catch (AuthenticationNotSupportedException ex) {
            log.error("{}The authentication method is not supported by the server", logPrefix, ex);
            throw new ConnectionException("The authentication method is not supported by the server", ex);
        }
        catch (AuthenticationException ex) {
            log.error("{}Incorrect password or username", logPrefix, ex);
            throw new IncorrectCredentialsException("Incorrect password or username", ex);
        }
        catch (NamingException ex) {
            log.error("{}NamingError when trying to create the context", logPrefix, ex);
            throw new ConnectionException("NamingError when trying to create the context", ex);
        }
    }

    void returnContext(DirContext context) {
        final String logPrefix = "returnContext() - ";
        log.trace("{}Entering method", logPrefix);
        if (context == null) {
            return;
        }
        try {
            log.info("{}Closing AD Connection Context", logPrefix);
            context.close();
        }
        catch (NamingException ex) {
            log.warn("{}Exception closing context (ignored) {}", logPrefix, ex.getMessage());
        }
        log.trace("{}LEaving method", logPrefix);
    }

    String getLdapURL() {
        return ldapURL;
    }

    /**
     *
     * Builds a new Active Directory Connection. Assumes no authentication
     * required.
     *
     * @param ldapServer Active Directory LDAP Server host name
     * @param port LDAP Port, usually 389 or 636
     * @param securityMode Encryption Required, SSL, TLS or NONE
     */
    public ADConnection(String ldapServer, int port, ConnectionSecurityMode securityMode) {
        this(ldapServer, port, securityMode, null, null);
    }

    /**
     * Builds a new Active Directory Connection. Assumes no encryption or
     * authentication
     *
     * @param ldapServer Active Directory LDAP Server host name
     * @param port LDAP Port, usually 389 or 636
     */
    public ADConnection(String ldapServer, int port) {
        this(ldapServer, port, ConnectionSecurityMode.NONE, null, null);

    }

    /**
     * Builds a new Active Directory Connection. Assumes no encryption or
     * authentication, and 389 for port number
     *
     * @param ldapServer Active Directory LDAP Server host name
     */
    public ADConnection(String ldapServer) {
        this(ldapServer, 389, ConnectionSecurityMode.NONE, null, null);

    }

    String getLdapServer() {
        return this.ldapServer;
    }

    int getLdapPort() {
        return this.ldapPort;
    }

    ConnectionSecurityMode getSecMode() {
        return this.secMode;
    }

}
