/*
 *   NumberRoutingManagement - SAMLAuthenticationConfiguration.java
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
package com.slinkytoybox.numberroutingmanagement.security;

import java.io.File;
import java.security.cert.X509Certificate;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.security.x509.X509Support;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j

public class SAMLAuthenticationConfiguration {

    @Autowired
    private Environment env;

    @Autowired
    private SAMLLoginSettings samlSettings;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        final String logPrefix = "securityFilterChain(HttpSecurity) - ";
        log.trace("{}Entering method", logPrefix);
        http
                .saml2Login(samlSettings)
                .authorizeRequests()
                .antMatchers("/assets/**").permitAll()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/saml2/**").permitAll()
                .antMatchers("/content/**").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/version").permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers("/login*").permitAll()
                .antMatchers("/logout*").permitAll()
                .antMatchers("/source/**").permitAll()
                .antMatchers("/metrics/health**/**").permitAll()
                .antMatchers("/**").authenticated()
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .sessionManagement()
                .sessionFixation()
                .migrateSession()
                .and()
                .logout()
                .invalidateHttpSession(false);
        RelyingPartyRegistrationResolver relyingPartyRegistrationResolver = new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrations());
        Saml2MetadataFilter filter = new Saml2MetadataFilter(relyingPartyRegistrationResolver, new OpenSamlMetadataResolver());
        http.addFilterBefore(filter, Saml2WebSsoAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    protected RelyingPartyRegistrationRepository relyingPartyRegistrations() throws Exception {
        final String logPrefix = "relyingPartyRegistrations() - ";
        log.trace("{}Entering method", logPrefix);
        String ssoUrl = env.getProperty("auth.saml.sso.url");
        String entityId = env.getProperty("auth.saml.entity-id");
        String registrationId = env.getProperty("auth.saml.registration-id");
        String certificateFile = env.getProperty("auth.saml.certificate", "none");
        Boolean wantSignedRequests = Boolean.getBoolean(env.getProperty("auth.saml.signing", "false"));
        RelyingPartyRegistration registration;
        if (certificateFile.isEmpty() || certificateFile.equalsIgnoreCase("none")) {
            log.trace("{}Building configuration without certificate", logPrefix);
            registration = RelyingPartyRegistration
                    .withRegistrationId(registrationId)
                    .assertingPartyDetails(party -> party
                    .entityId(entityId)
                    .singleSignOnServiceLocation(ssoUrl)
                    .wantAuthnRequestsSigned(wantSignedRequests)
                    )
                    .build();

        }
        else {
            File verificationKey = new File(certificateFile);
            log.trace("{}Building configuration with certificate : {}", logPrefix, verificationKey);
            X509Certificate certificate = X509Support.decodeCertificate(verificationKey);
            Saml2X509Credential credential = Saml2X509Credential.verification(certificate);
            registration = RelyingPartyRegistration
                    .withRegistrationId(registrationId)
                    .assertingPartyDetails(party -> party
                    .entityId(entityId)
                    .singleSignOnServiceLocation(ssoUrl)
                    .wantAuthnRequestsSigned(wantSignedRequests)
                    .verificationX509Credentials(c -> c.add(credential))
                    )
                    .build();
        }
        log.trace("{}Build registration: {}", logPrefix, registration);
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

}
