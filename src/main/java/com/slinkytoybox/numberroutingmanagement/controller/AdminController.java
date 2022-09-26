/*
 *   NumberRoutingManagement - AdminController.java
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

import com.slinkytoybox.numberroutingmanagement.security.roles.RolePlatformAdmin;
import com.slinkytoybox.numberroutingmanagement.businesslogic.AdministrationLogic;
import com.slinkytoybox.numberroutingmanagement.controller.defaults.ModelDefaults;
import com.slinkytoybox.numberroutingmanagement.dto.*;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
@Controller
@RolePlatformAdmin
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdministrationLogic adminLogic;

    @Autowired
    private ModelDefaults modelDefaults;

    private static final String SECTION_NAME = "Administration";
    private static final String NAVBAR_SEGMENT = "admin";

    @GetMapping("/")
    public String admin(Model model) {
        final String logPrefix = "admin() - ";
        log.trace("{}Entering method", logPrefix);
        final String viewTemplate = "admin/index";
        final String pageTitle = "Administration";
        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }

    @GetMapping("/platform")
    public String adminPlatform(Model model) {
        final String logPrefix = "adminPlatform() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/platform", logPrefix);
        final String pageTitle = "Administration - Platforms";
        final String viewTemplate = "admin/platform";
        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }

    @PostMapping(path = "/platform/refresh", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> adminPlatformRefresh(@RequestBody DataTablesRequest dtRequest) {
        final String logPrefix = "adminPlatformRefresh() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/platform/refresh", logPrefix);
        log.trace("{}Deserialised DT Request: {}", logPrefix, dtRequest);
        log.info("{}Performing Platforms search", logPrefix);
        ResultMap map = adminLogic.getAllPlatforms(dtRequest);
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(map.getHttpStatus()).body(((DataTablesResult) map.getResult()).fetchResponse());
    }

    @GetMapping(path = "/platform/config", produces = "application/json")
    public ResponseEntity<DTOResult> adminPlatformConfig() {
        final String logPrefix = "adminPlatformConfig() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/platform/config", logPrefix);
        ResultMap result = adminLogic.getPlatformConfig();
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(result.getHttpStatus()).body(result.getResult());
    }

    @GetMapping(path = "/platform/item/{platformId}", produces = "application/json")
    public ResponseEntity<DTOResult> adminPlatformItemGet(@PathVariable("platformId") Long platformId) {
        final String logPrefix = "adminPlatformItemGet() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/platform/item/{}", logPrefix, platformId);
        log.info("{}Performing specific Platform search: {}", logPrefix, platformId);
        ResultMap map = adminLogic.getPlatform(platformId);
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(map.getHttpStatus()).body(map.getResult());
    }

    @PutMapping(path = "/platform/item/{platformId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DTOResult> adminPlatformItemPut(@RequestBody PlatformDTO modRequest, @PathVariable("platformId") Long platformId) {
        final String logPrefix = "adminPlatformItemPut() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/platform/item/{}", logPrefix, platformId);
        modRequest.setPlatformId(platformId);
        ResultMap map = adminLogic.updatePlatform(modRequest, true);
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(map.getHttpStatus()).body(map.getResult());
    }

    @PostMapping(path = "/platform/item", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DTOResult> adminPlatformItemPost(@RequestBody PlatformDTO modRequest) {
        final String logPrefix = "adminBusinessUnitsBUPost() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/platform/item", logPrefix);
        ResultMap map = adminLogic.updatePlatform(modRequest, false);
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(map.getHttpStatus()).body(map.getResult());
    }

    @GetMapping("/site")
    public String adminSite(Model model) {
        final String logPrefix = "adminSite() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/site", logPrefix);
        final String pageTitle = "Administration -  Sites";
        final String viewTemplate = "admin/site";
        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }

    @PostMapping(path = "/site/refresh", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> adminSiteRefresh(@RequestBody DataTablesRequest dtRequest) {
        final String logPrefix = "adminSiteRefresh() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/site/refresh", logPrefix);
        log.trace("{}Deserialised DT Request: {}", logPrefix, dtRequest);
        log.info("{}Performing Sites search", logPrefix);
        ResultMap map = adminLogic.getAllSites(dtRequest);
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(map.getHttpStatus()).body(((DataTablesResult) map.getResult()).fetchResponse());
    }

    @GetMapping(path = "/site/config", produces = "application/json")
    public ResponseEntity<DTOResult> adminSiteConfig() {
        final String logPrefix = "adminSiteConfig() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/site/config", logPrefix);
        ResultMap result = adminLogic.getSiteConfig();
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(result.getHttpStatus()).body(result.getResult());
    }

    @GetMapping(path = "/site/item/{siteId}", produces = "application/json")
    public ResponseEntity<DTOResult> adminSiteItemGet(@PathVariable("siteId") Long siteId) {
        final String logPrefix = "adminSiteItemGet() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/site/item/{}", logPrefix, siteId);
        log.info("{}Performing specific Site search: {}", logPrefix, siteId);
        ResultMap map = adminLogic.getSite(siteId);
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(map.getHttpStatus()).body(map.getResult());
    }

    @PutMapping(path = "/site/item/{siteId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DTOResult> adminSiteItemPut(@RequestBody SiteDTO modRequest, @PathVariable("siteId") Long siteId) {
        final String logPrefix = "adminSiteItemPut() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/site/item/{}", logPrefix, siteId);
        modRequest.setSiteId(siteId);
        ResultMap map = adminLogic.updateSite(modRequest, true);
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(map.getHttpStatus()).body(map.getResult());
    }

    @PostMapping(path = "/site/item", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DTOResult> adminSiteItemPost(@RequestBody SiteDTO modRequest) {
        final String logPrefix = "adminBusinessUnitsBUPost() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /admin/site/item", logPrefix);
        ResultMap map = adminLogic.updateSite(modRequest, false);
        log.debug("{}Returning result to browser", logPrefix);
        return ResponseEntity.status(map.getHttpStatus()).body(map.getResult());
    }

}
