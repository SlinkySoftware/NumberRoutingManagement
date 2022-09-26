/*
 *   number-routing-management - FrontEndLogic.java
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

import com.slinkytoybox.numberroutingmanagement.businesslogic.exceptions.*;
import com.slinkytoybox.numberroutingmanagement.database.types.*;
import com.slinkytoybox.numberroutingmanagement.dto.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Component
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {AllocationException.class})
public class FrontEndLogic {

    @Autowired
    private PowershellFunctions powershellFunctions;

    @Autowired
    private ADFunctions adFunctions;

    @Autowired
    private DatabaseFunctions dbFunctions;

    public Map<String, Object> allocateUserDID(String username, Optional<Long> requestedSiteId, String auditUsername) {
        final String logPrefix = "allocateUserDID() - ";
        log.trace("{}Entering method", logPrefix);
        Map<String, Object> result = new HashMap<>();

        ADUserDTO ad = adFunctions.getADUser(username);

        if (ad == null) {
            log.error("{}Could not locate AD user", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "Could not find user in Active Directory");
            return result;
        }

        List<String> existingNumbers = dbFunctions.getAllocatedE164(ad.getUserPrincipalName());

        if (!existingNumbers.isEmpty()) {
            log.warn("{}User already has an allocation ({}), not allocating a new number", logPrefix, existingNumbers);
            result.put("allocatedNumber", existingNumbers);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "User already has a number allocation");
            result.put("user", ad);
            return result;
        }

        SiteDTO dbSite = null;

        if ((ad.getCity() == null || ad.getCity().isEmpty()) && !requestedSiteId.isPresent()) {
            log.error("{}No active directory site, and no requested site in request. Process failed", logPrefix);
        }
        else if ((ad.getCity() == null || ad.getCity().isEmpty()) && requestedSiteId.isPresent()) {
            log.debug("{}Processing site ID {}", logPrefix, requestedSiteId.get());
            dbSite = dbFunctions.getSite(requestedSiteId.get());
            ad.setCity(dbSite.getAdName());
            ad.setState(dbSite.getState().name());
        }
        else {
            log.debug("{}Processing AD City to Site Map: {}", logPrefix, ad.getCity());
            dbSite = dbFunctions.getSiteByAd(ad.getCity());
        }

        if (dbSite == null) {
            log.warn("{}Could not find an appropriate site for this user.", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "Could not find an appropriate site for this user.");
            result.put("user", ad);
            return result;
        }
        log.info("{}Using DB site: {}", logPrefix, dbSite);

        PlatformDTO platform = dbFunctions.getDefaultUserPlatform();
        if (platform == null) {
            log.warn("{}No default platform defined.", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "No default user phone platform defined. Cannot allocate a number.");
            result.put("user", ad);
            return result;
        }

        log.debug("{}Getting user object from DB", logPrefix);
        AllocatedObjectDTO userObject = dbFunctions.getOrCreateObject(ad.getUserPrincipalName());
        log.info("{}Using DB user object {}", logPrefix, userObject);

        // No preferred prefix for users.
        List<PrefixDTO> preferredPrefixes = null;

        log.debug("{}Finding appropriate E164 number for user site {} on platform {}", logPrefix, dbSite.getName(), platform.getName());
        E164NumberDTO allocatedNumber;
        try {
            allocatedNumber = dbFunctions.allocateNumberForSite(dbSite, platform, preferredPrefixes);
            allocatedNumber
                    .setAllocatedObjectId(userObject.getAllocatedObjectId())
                    .setAllocatedObjectName(userObject.getName())
                    .setDescription(ad.getFirstName() + " " + ad.getSurname())
                    .setAllocationType(NumberType.EXTENSION);
            dbFunctions.writeAllocation(allocatedNumber, auditUsername);
        }
        catch (AllocationException ex) {
            log.error("{}Allocation exception encountered.", logPrefix, ex);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", ex.getMessage());
            result.put("user", ad);
            return result;
        }

        if (platform.getPowershellEnabled()) {
            String powershellCommands = powershellFunctions.getCreateUserCommand(ad, allocatedNumber, dbSite.getDialplan());
            result.put("powershellCommands", powershellCommands);
        }

        result.put("success", 1);
        result.put("allocatedNumber", allocatedNumber.getE164());
        result.put("user", ad);
        return result;

    }

    public Map<String, Object> allocateRoomDID(String roomAccountName, String roomDescription, Long requestedSiteId, Long requestedPlatformId, String auditUsername) {
        final String logPrefix = "allocateRoomDID() - ";
        log.trace("{}Entering method", logPrefix);
        Map<String, Object> result = new HashMap<>();

        List<String> existingNumbers = dbFunctions.getAllocatedE164(roomAccountName);

        if (!existingNumbers.isEmpty()) {
            log.warn("{}Room already has an allocation ({}), not allocating a new number", logPrefix, existingNumbers);
            result.put("allocatedNumber", existingNumbers);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "This room already has a number allocation");
            result.put("roomAccountName", roomAccountName);
            result.put("roomDescription", roomDescription);
            return result;
        }

        SiteDTO dbSite = null;

        if (requestedSiteId == null) {
            log.error("{}No requested site in request. Process failed", logPrefix);
        }
        else {
            log.debug("{}Processing site ID {}", logPrefix, requestedSiteId);
            dbSite = dbFunctions.getSite(requestedSiteId);
        }

        if (dbSite == null) {
            log.warn("{}Could not find an appropriate site for this room.", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "Could not find an appropriate site for this room.");
            result.put("roomAccountName", roomAccountName);
            result.put("roomDescription", roomDescription);
            return result;
        }
        log.info("{}Using DB site: {}", logPrefix, dbSite);

        PlatformDTO platform = dbFunctions.getPlatform(requestedPlatformId, Boolean.TRUE, null);
        if (platform == null) {
            log.warn("{}No platform found for rooms.", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "Cannot allocate a number - platform does not support rooms");
            result.put("roomAccountName", roomAccountName);
            result.put("roomDescription", roomDescription);
            return result;
        }

        log.debug("{}Getting allocated object from DB", logPrefix);
        AllocatedObjectDTO allocatedObject = dbFunctions.getOrCreateObject(roomAccountName);
        log.info("{}Using DB allocated object {}", logPrefix, allocatedObject);

        // Find the preferred room prefix if there is one
        log.debug("{}Looking to see if we have a preferred room prefix for this site", logPrefix);
        List<PrefixDTO> preferredPrefixes = dbFunctions.getPreferredRoomPrefix(dbSite);

        log.debug("{}Finding appropriate E164 number for room site {} on platform {}", logPrefix, dbSite.getName(), platform.getName());
        E164NumberDTO allocatedNumber;
        try {
            allocatedNumber = dbFunctions.allocateNumberForSite(dbSite, platform, preferredPrefixes);
            allocatedNumber
                    .setAllocatedObjectId(allocatedObject.getAllocatedObjectId())
                    .setAllocatedObjectName(allocatedObject.getName())
                    .setDescription(roomDescription)
                    .setAllocationType(NumberType.EXTENSION);
            dbFunctions.writeAllocation(allocatedNumber, auditUsername);
        }
        catch (AllocationException ex) {
            log.error("{}Allocation exception encountered.", logPrefix, ex);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", ex.getMessage());
            result.put("roomAccountName", roomAccountName);
            result.put("roomDescription", roomDescription);
            return result;
        }

        if (platform.getPowershellEnabled()) {
            String powershellCommands = powershellFunctions.getCreateRoomCommand(allocatedObject.getName(), allocatedNumber, dbSite.getDialplan());
            result.put("powershellCommands", powershellCommands);
        }

        result.put("success", 1);
        result.put("allocatedNumber", allocatedNumber.getE164());
        result.put("roomAccountName", roomAccountName);
        result.put("roomDescription", roomDescription);
        return result;

    }

    public Map<String, Object> allocateResourceDID(String resourceAccountName, String resourceDescription, Long requestedSiteId, Long requestedPlatformId, NumberType numberType, String auditUsername) {
        final String logPrefix = "allocateResourceDID() - ";
        log.trace("{}Entering method", logPrefix);
        Map<String, Object> result = new HashMap<>();

        List<String> existingNumbers = dbFunctions.getAllocatedE164(resourceAccountName);

        if (!existingNumbers.isEmpty()) {
            log.warn("{}Resource already has an allocation ({}), not allocating a new number", logPrefix, existingNumbers);
            result.put("allocatedNumber", existingNumbers);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "This resource already has a number allocation");
            result.put("resourceAccountName", resourceAccountName);
            result.put("resourceDescription", resourceDescription);
            result.put("resourceType", numberType.getFriendlyName());
            return result;
        }

        SiteDTO dbSite = null;

        if (requestedSiteId == null) {
            log.error("{}No requested site in request. Process failed", logPrefix);
        }
        else {
            log.debug("{}Processing site ID {}", logPrefix, requestedSiteId);
            dbSite = dbFunctions.getSite(requestedSiteId);
        }

        if (dbSite == null) {
            log.warn("{}Could not find an appropriate site for this room.", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "Could not find an appropriate site for this resource.");
            result.put("resourceAccountName", resourceAccountName);
            result.put("resourceDescription", resourceDescription);
            result.put("resourceType", numberType.getFriendlyName());

            return result;
        }
        log.info("{}Using DB site: {}", logPrefix, dbSite);

        PlatformDTO platform = dbFunctions.getPlatform(requestedPlatformId, Boolean.TRUE, null);
        if (platform == null) {
            log.warn("{}No platform found for rooms.", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "Cannot allocate a number - platform does not support resources");
            result.put("resourceAccountName", resourceAccountName);
            result.put("resourceDescription", resourceDescription);
            result.put("resourceType", numberType.getFriendlyName());

            return result;
        }

        log.debug("{}Getting allocated object from DB", logPrefix);
        AllocatedObjectDTO allocatedObject = dbFunctions.getOrCreateObject(resourceAccountName);
        log.info("{}Using DB allocated object {}", logPrefix, allocatedObject);

        // Find the preferred room prefix if there is one
        log.debug("{}Looking to see if we have a preferred room prefix for this site", logPrefix);
        List<PrefixDTO> preferredPrefixes = dbFunctions.getPreferredRoomPrefix(dbSite);

        log.debug("{}Finding appropriate E164 number for resource site {} on platform {}", logPrefix, dbSite.getName(), platform.getName());
        E164NumberDTO allocatedNumber;
        try {
            allocatedNumber = dbFunctions.allocateNumberForSite(dbSite, platform, preferredPrefixes);
            allocatedNumber
                    .setAllocatedObjectId(allocatedObject.getAllocatedObjectId())
                    .setAllocatedObjectName(allocatedObject.getName())
                    .setDescription(resourceDescription)
                    .setAllocationType(numberType);
            dbFunctions.writeAllocation(allocatedNumber,auditUsername);
        }
        catch (AllocationException ex) {
            log.error("{}Allocation exception encountered.", logPrefix, ex);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", ex.getMessage());
            result.put("resourceAccountName", resourceAccountName);
            result.put("resourceDescription", resourceDescription);
            result.put("resourceType", numberType.getFriendlyName());
            return result;
        }

        if (platform.getPowershellEnabled()) {
            String powershellCommands = powershellFunctions.getCreateRoomCommand(allocatedObject.getName(), allocatedNumber, dbSite.getDialplan());
            result.put("powershellCommands", powershellCommands);
        }

        result.put("success", 1);
        result.put("allocatedNumber", allocatedNumber.getE164());
        result.put("resourceAccountName", resourceAccountName);
        result.put("resourceDescription", resourceDescription);
        result.put("resourceType", numberType.getFriendlyName());

        return result;

    }
    
    public List<PlatformDTO> getAllPlatforms() {
        final String logPrefix = "getAllPlatforms() - ";
        log.trace("{}Entering method", logPrefix);
        return dbFunctions.getAllPlatforms();
    }

    public List<PlatformDTO> getAllRoomEnabledPlatforms() {
        final String logPrefix = "getAllRoomEnabledPlatforms() - ";
        log.trace("{}Entering method", logPrefix);
        return dbFunctions.getAllRoomEnabledPlatforms();
    }

    public List<PlatformDTO> getAllResourceEnabledPlatforms() {
        final String logPrefix = "getAllResourceEnabledPlatforms() - ";
        log.trace("{}Entering method", logPrefix);
        return dbFunctions.getAllResourceEnabledPlatforms();
    }

    public Map<Integer, String> getFriendlyNumberTypes() {
        final String logPrefix = "getFriendlyNumberTypes() - ";
        log.trace("{}Entering method", logPrefix);
        Map<Integer, String> numTypes = new HashMap<>();
        for (NumberType nt : NumberType.values()) {
            numTypes.put(nt.getId(), nt.getFriendlyName());
        }
        return numTypes;
    }

    public ADUserDTO getAdUser(String username) {
        final String logPrefix = "getAdUser() - ";
        log.trace("{}Entering method", logPrefix);
        return adFunctions.getADUser(username);
    }

    public List<SiteDTO> getAllSites() {
        final String logPrefix = "getAllSites() - ";
        log.trace("{}Entering method", logPrefix);
        return dbFunctions.getAllSites();
    }

}
