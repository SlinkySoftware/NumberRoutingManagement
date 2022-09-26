/*
 *   number-routing-management - SAMLLoginSettings.java
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.saml2.Saml2LoginConfigurer;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
@Component
public class SAMLLoginSettings implements Customizer<Saml2LoginConfigurer<HttpSecurity>> {
    
    private final Map<String, String> roleMap = new HashMap<>();
    
    @Autowired
    private SAMLConfigurationProperties samlProps;
    
    @Autowired
    private Environment env;
    
    @Override
    public void customize(Saml2LoginConfigurer<HttpSecurity> t) {
        final String logPrefix = "customize() - ";
        log.trace("{}Entering method", logPrefix);
        
        t.successHandler(new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                authentication = assignAuthorities(authentication);
                super.onAuthenticationSuccess(request, response, authentication);
            }
        });

        t.loginPage("/auth/login");
        log.trace("{}Leaving method", logPrefix);
        
    }
    
    private Authentication assignAuthorities(Authentication authentication) {
        final String logPrefix = "assignAuthorities() - ";
        log.trace("{}Entering method", logPrefix);
        String groupAttribute = env.getProperty("auth.saml.group-attribute", "http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
        
        DefaultSaml2AuthenticatedPrincipal princ = (DefaultSaml2AuthenticatedPrincipal) authentication.getPrincipal();
        log.trace("{}Principal: {}", logPrefix, princ);
        
        if (princ.getAttribute(groupAttribute) != null) {
            log.trace("{}Found Attribute '{}'", logPrefix, groupAttribute);
            List<String> groups = princ.getAttribute(groupAttribute);
            log.info("{}External groups: {}", logPrefix, groups);
            
            List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
            
            log.trace("{}Adding new roles", logPrefix);
            if (groups != null) {
                for (String group : groups) {
                    log.trace("{}>> Processing group {}", logPrefix, group);
                    if (roleMap.containsKey(group)) {
                        String role = roleMap.get(group);
                        log.trace("{}>>++ Found role {}", logPrefix, role);
                        updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }
            
            Saml2Authentication sAuth = (Saml2Authentication) authentication;
            sAuth = new Saml2Authentication(
                    (AuthenticatedPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                    sAuth.getSaml2Response(),
                    updatedAuthorities
            );
            SecurityContextHolder.getContext().setAuthentication(sAuth);
            log.trace("{}Returning updated authentication {},", logPrefix, sAuth);
            return sAuth;
        }
        else {
            log.trace("{}Returning original authentication {},", logPrefix, authentication);
            return authentication;
        }
    }
    
    @PostConstruct
    private void setRoles() {
        final String logPrefix = "setRoles() - ";
        log.trace("{}Entering method", logPrefix);
        roleMap.clear();
        log.debug("{}Setting up group to role mapping", logPrefix);
        
        samlProps.getGroupMapping().forEach((role, group) -> {
            log.info("{}Group {} maps to role {}", logPrefix, group, role);
            roleMap.put(group, role);
        });
        log.debug("{}RoleMap: {}", logPrefix, roleMap);
        log.trace("{}Leaving method", logPrefix);
        
    }
    
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = "";
        hierarchy += "ROLE_PLATFORM_ADMIN > ROLE_TELEPHONY_MGMT \n";
        hierarchy += "ROLE_TELEPHONY_MGMT > ROLE_USER_MGMT \n";
        hierarchy += "ROLE_USER_MGMT > ROLE_READ_ONLY \n";
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }
    
    @Bean
    public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }
}
