/*
 *   number-routing-management - FrontEndController.java
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

import com.slinkytoybox.numberroutingmanagement.security.roles.RoleTelephonyMgmt;
import com.slinkytoybox.numberroutingmanagement.security.roles.RoleUserMgmt;
import com.slinkytoybox.numberroutingmanagement.businesslogic.FrontEndLogic;
import com.slinkytoybox.numberroutingmanagement.controller.defaults.ModelDefaults;
import com.slinkytoybox.numberroutingmanagement.database.types.NumberType;
import com.slinkytoybox.numberroutingmanagement.dto.ADUserDTO;
import com.slinkytoybox.numberroutingmanagement.dto.DTOHashMap;
import com.slinkytoybox.numberroutingmanagement.dto.DTOResult;
import com.slinkytoybox.numberroutingmanagement.dto.PlatformDTO;
import com.slinkytoybox.numberroutingmanagement.dto.SiteDTO;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
@Controller
@RequestMapping("/frontend")
public class FrontEndController {

    @Autowired
    private ModelDefaults modelDefaults;

    @Autowired
    private FrontEndLogic frontEndLogic;

    private static final String SECTION_NAME = "Home";
    private static final String NAVBAR_SEGMENT = "index";

    @RoleUserMgmt
    @GetMapping("/createuser")
    public String createUserGet(Model model) {
        final String logPrefix = "createUserGet() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing GET /frontend/createuser", logPrefix);
        final String pageTitle = "Create New User Extension";
        final String viewTemplate = "frontend/createuser";

        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }

    @RoleUserMgmt
    @GetMapping(path = "/aduser/{adUserName}", produces = "application/json")
    public ResponseEntity<DTOResult> frontEndAdUserGet(@PathVariable("adUserName") String adUserName) {
        final String logPrefix = "frontEndAdUserGet() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /frontend/aduser/{}", logPrefix, adUserName);

        ADUserDTO adUser = frontEndLogic.getAdUser(adUserName);
        if (adUser == null || adUser.getSAMAccountName() == null || adUser.getSAMAccountName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<SiteDTO> allSites = frontEndLogic.getAllSites();
        DTOHashMap<String, Object> map = new DTOHashMap<>();
        map.put("promptForSite", adUser.getCity() == null || adUser.getCity().isEmpty());
        map.put("adUser", adUser);
        if (adUser.getCity() == null || adUser.getCity().isEmpty()) map.put("sites", allSites);
        log.debug("{}Returning result to browser: {}", logPrefix, map);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @RoleUserMgmt
    @PostMapping("/createuser")
    public String createUserPost(Model model, Principal principal, @RequestParam("userName") String adUserName, @RequestParam("locationSelection") Optional<Long> requestedSiteId) {
        final String logPrefix = "createUserPost() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing POST /frontend/createuser for username {} at siteId {}", logPrefix, adUserName, requestedSiteId.orElse(-1L));
        final String pageTitle = "Create New User Extension";
        final String viewTemplate = "frontend/createuser";
        model.addAttribute("showAllocationResult", 1);

        Map<String, Object> result = frontEndLogic.allocateUserDID(adUserName, requestedSiteId, principal.getName());
        result.put("showNumber", result.containsKey("allocatedNumber"));
        model.addAttribute("allocationResult", result);
        log.trace("{}Added result to model: {}", logPrefix, result);
        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }
    
    @RoleTelephonyMgmt
    @GetMapping("/createroom")
    public String createRoomGet(Model model) {
        final String logPrefix = "createRoomGet() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing GET /frontend/createroom", logPrefix);
        final String pageTitle = "Create New Room Extension";
        final String viewTemplate = "frontend/createroom";

        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }

    @RoleTelephonyMgmt
    @GetMapping(path = "/rooms", produces = "application/json")
    public ResponseEntity<DTOResult> frontEndRoomsGet() {
        final String logPrefix = "frontEndRoomsGet() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /frontend/rooms", logPrefix);

        List<SiteDTO> allSites = frontEndLogic.getAllSites();
        List<PlatformDTO> allRoomPlatforms = frontEndLogic.getAllRoomEnabledPlatforms();
        DTOHashMap<String, Object> map = new DTOHashMap<>();
        map.put("sites", allSites);
        map.put("platforms", allRoomPlatforms);
        log.debug("{}Returning result to browser: {}", logPrefix, map);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
    
    @RoleTelephonyMgmt
    @PostMapping("/createroom")
    public String createRoomPost(Model model, Principal principal, 
            @RequestParam("deviceName") String deviceName, 
            @RequestParam("deviceDescription") String deviceDescription, 
            @RequestParam("locationSelection") Long siteId,
            @RequestParam("platformSelection") Long platformId
    ) {
        final String logPrefix = "createRoomPost() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing POST /frontend/createroom for device {}", logPrefix, deviceName);
        final String pageTitle = "Create New Room Extension";
        final String viewTemplate = "frontend/createroom";
        model.addAttribute("showAllocationResult", 1);

        Map<String, Object> result = frontEndLogic.allocateRoomDID(deviceName, deviceDescription, siteId, platformId, principal.getName());
        result.put("showNumber", result.containsKey("allocatedNumber"));
        result.put("showPowershell", result.containsKey("powershellCommands"));
        model.addAttribute("allocationResult", result);
        log.trace("{}Added result to model: {}", logPrefix, result);
        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }

    @RoleTelephonyMgmt
    @GetMapping("/createresx")
    public String createResxGet(Model model) {
        final String logPrefix = "createResxGet() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing GET /frontend/createresx", logPrefix);
        final String pageTitle = "Create New Resource Number";
        final String viewTemplate = "frontend/createresx";

        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }

    @RoleTelephonyMgmt
    @GetMapping(path = "/resources", produces = "application/json")
    public ResponseEntity<DTOResult> frontEndResourcesGet() {
        final String logPrefix = "frontEndResourcesGet() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing /frontend/resources", logPrefix);

        List<SiteDTO> allSites = frontEndLogic.getAllSites();
        List<PlatformDTO> allRoomPlatforms = frontEndLogic.getAllResourceEnabledPlatforms();
        DTOHashMap<String, Object> map = new DTOHashMap<>();
        map.put("sites", allSites);
        map.put("platforms", allRoomPlatforms);
        Map<Integer,String> types = frontEndLogic.getFriendlyNumberTypes();
        types.remove(1);
        map.put("numbertypes", types);
        log.debug("{}Returning result to browser: {}", logPrefix, map);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
    
    @RoleTelephonyMgmt
    @PostMapping("/createresx")
    public String createResxPost(Model model, Principal principal, 
            @RequestParam("deviceName") String deviceName, 
            @RequestParam("deviceDescription") String deviceDescription, 
            @RequestParam("locationSelection") Long siteId,
            @RequestParam("platformSelection") Long platformId,
            @RequestParam("typeSelection") String typeId
    ) {
        final String logPrefix = "createResxPost() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Processing POST /frontend/createresx for resource {}", logPrefix, deviceName);
        final String pageTitle = "Create New Resource Number";
        final String viewTemplate = "frontend/createresx";
        model.addAttribute("showAllocationResult", 1);
        Map<String, Object> result = frontEndLogic.allocateResourceDID(deviceName, deviceDescription, siteId, platformId, NumberType.getType(Integer.parseInt(typeId)), principal.getName());
        result.put("showNumber", result.containsKey("allocatedNumber"));
        result.put("showPowershell", result.containsKey("powershellCommands"));
        model.addAttribute("allocationResult", result);
        log.trace("{}Added result to model: {}", logPrefix, result);
        modelDefaults.updateModelDefaults(model, pageTitle, NAVBAR_SEGMENT, SECTION_NAME, viewTemplate);
        return viewTemplate;
    }
}
