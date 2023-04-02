/*
 *   NumberRoutingManagement - ModelDefaults.java
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
package com.slinkytoybox.numberroutingmanagement.controller.defaults;

import javax.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
@Component
public class ModelDefaults {
    
    @Autowired
    ServletContext context;
    
    @Value("${company.name}")
    String companyName;

    @Value("${app.name}")
    String appName;

    @Value("${app.copyright}")
    String appCopyright;

    @Value("${info.build.name}")
    String buildName;
    
    @Value("${info.build.version}")
    String buildVersion;

    @Value("${company.logo}")
    String companyLogo;
    
    @Value("${company.favicon}")
    String favIcon;
    
    
    public void updateModelDefaults(Model model, String pageTitle, String navBarFragment, String moduleName, String activeLink) {
        final String logPrefix = "updateModelDefaults() - ";
        log.trace("{}Entering method", logPrefix);
        
        String appVer = buildName + " v" + buildVersion;
        model.addAttribute("appver", appVer);
        model.addAttribute("pagetitle", appName + " / " + pageTitle + " (" + companyName + ")");
        model.addAttribute("companylogo", companyLogo);
        model.addAttribute("favicon", favIcon);
        model.addAttribute("appname", appName);
        model.addAttribute("copyright", appCopyright);

        model.addAttribute("pageheader", pageTitle);
        model.addAttribute("navBarFragment", navBarFragment);
        model.addAttribute("moduleName", moduleName);
        model.addAttribute("activeLink", activeLink);

        log.trace("{}Leaving method", logPrefix);
    }
    
}

