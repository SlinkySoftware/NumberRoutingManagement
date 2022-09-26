/*
 *   NumberRoutingManagement - RootController.java
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
package com.slinkytoybox.numberroutingmanagement.controller;

import lombok.extern.slf4j.Slf4j;
import com.slinkytoybox.numberroutingmanagement.controller.defaults.ModelDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
@Controller
@Lazy
public class RootController {

    @Autowired
    private Environment env;

    @Autowired
    private ModelDefaults modelDefaults;
    
    @Value("${git.commit.time}")
    private String gitCommitTime;

    @Value("${git.commit.message.short}")
    private String gitCommitMessageShort;
    
    @Value("${git.commit.id.abbrev}")
    private String gitCommitIdAbbrev;

    @Value("${git.commit.id.describe-short}")
    private String gitCommitIdDescribeShort;

    @Value("${git.build.host}")
    private String gitBuildHost;

    @Value("${git.build.user.name}")
    private String gitBuildUser;
    
    @Value("${git.branch}")
    private String gitBranch;

    @Value("${git.dirty}")
    private String gitDirty;

    private static final String SECTION_NAME = "Home";
    private static final String NAVBAR_SEGMENT = "index";
   
    @GetMapping("/")
    public String index(Model model) {
        final String logPrefix = "index() - ";
        log.trace("{}Entering method", logPrefix);

        final String pageTitle = "Main Menu";
        final String viewTemplate = "index";

        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }


    @GetMapping(value="/version", produces="text/plain;charset=UTF-8")
    public @ResponseBody String version(WebRequest request) {
        final String logPrefix = "version() - ";
        log.trace("{}Entering method", logPrefix);
        log.debug("{}Processing request for /version", logPrefix);
        String response = "";
        response += "   Application Name: " + env.getProperty("app.name", "(unknown)");
        response += "\n      Class Version: " + env.getProperty("info.build.name", "(unknown)") + " v" + env.getProperty("info.build.version", "(unknown)");
        response += "\n";
        response += "\nBuild Information";
        response += "\n      Build Date: " + env.getProperty("info.build.date", "(unknown)");
        response += "\n        Hostname: " + gitBuildHost;
        response += "\n            User: " + gitBuildUser;
        response += "\n";
        response += "\nGIT Branch";
        response += "\n    Branch Name: " + gitBranch;
        response += "\n   Uncommitted?: " + gitDirty;
        response += "\n";
        response += "\nGIT Commit";
        response += "\n              ID: " + gitCommitIdAbbrev;
        response += "\n            Date: " + gitCommitTime;
        response += "\n         Message: " + gitCommitMessageShort;
        response += "\n     Description: " + gitCommitIdDescribeShort;
        response += "\n";
        response += "\n";

        return response;
    }


}