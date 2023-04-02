/*
 *   number-routing-management - DatabaseFunctions.java
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
package com.slinkytoybox.numberroutingmanagement.businesslogic;

import com.slinkytoybox.numberroutingmanagement.businesslogic.exceptions.*;
import com.slinkytoybox.numberroutingmanagement.database.model.*;
import com.slinkytoybox.numberroutingmanagement.database.repository.MasterDBRepository;
import com.slinkytoybox.numberroutingmanagement.database.types.*;
import com.slinkytoybox.numberroutingmanagement.dto.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Component
@Slf4j
public class DatabaseFunctions {

    @Autowired
    private MasterDBRepository dbRepo;

    @Autowired
    private Environment env;

    List<SiteDTO> getAllSites() {
        final String logPrefix = "getAllSites() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up all sites in database.", logPrefix);

        List<Site> sites = dbRepo.siteRepository.findAll();

        List<SiteDTO> siteResultList = new ArrayList<>();
        log.debug("{}Creating result list", logPrefix);
        for (Site site : sites) {
            SiteDTO siteResult = SiteDTO.mapDataObject(site);
            siteResultList.add(siteResult);
            log.trace("{}Added : {}", logPrefix, siteResult);
        }
        log.debug("{}Returning result list of size {} members", logPrefix, siteResultList.size());

        return siteResultList;
    }

    SiteDTO getSite(Long siteId) {
        final String logPrefix = "getSite() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up site {} in database.", logPrefix, siteId);

        Optional<Site> siteOpt = dbRepo.siteRepository.findById(siteId);
        if (!siteOpt.isPresent()) {
            log.warn("{}Warning: Site not found in database", logPrefix);
            return null;
        }
        Site site = siteOpt.get();
        SiteDTO siteResult = SiteDTO.mapDataObject(site);
        log.debug("{}Returning site {} ", logPrefix, siteResult);
        return siteResult;
    }

    SiteDTO getSiteByAd(String adSiteName) {
        final String logPrefix = "getSiteByAd() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up AD site {} in database.", logPrefix, adSiteName);

        Optional<Site> siteOpt = dbRepo.siteRepository.findByAdname(adSiteName);
        if (!siteOpt.isPresent()) {
            log.warn("{}Warning: Site not found in database", logPrefix);
            return null;
        }
        Site site = siteOpt.get();
        SiteDTO siteResult = SiteDTO.mapDataObject(site);

        log.debug("{}Returning site {} ", logPrefix, siteResult);
        return siteResult;
    }

    PlatformDTO getDefaultUserPlatform() {
        final String logPrefix = "getDefaultUserPlatform() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up default user platform", logPrefix);
        Set<Platform> platforms = dbRepo.platformRepository.findByDefaultUserPlatform(true);
        if (platforms.size() != 1) {
            log.error("{}Error: Only one platform can be default!", logPrefix);
            return null;
        }
        Platform p = (Platform) platforms.toArray()[0];
        log.debug("{}Found platform: {}", logPrefix, p);

        PlatformDTO plat = PlatformDTO.mapDataObject(p);
        plat.setDefaultUserPlatform(true);
        return plat;

    }

    PlatformDTO getPlatform(Long platformId, Boolean isRoomCapable, Boolean isResourceCapable) {
        final String logPrefix = "getPlatform() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up platform >> ID:{} | Room Capable? {} | Resource Capable? {}", logPrefix, platformId, isRoomCapable, isResourceCapable);
        Optional<Platform> platform;
        if ((isRoomCapable == null || !isRoomCapable) && (isResourceCapable == null || !isResourceCapable)) {
            // find all platforms
            platform = dbRepo.platformRepository.findById(platformId);
        }
        else if (Boolean.TRUE.equals(isResourceCapable) && (isRoomCapable == null || !isRoomCapable)) {
            // Resource Only
            platform = dbRepo.platformRepository.findOneByPlatformIdAndAllowResources(platformId, true);
        }
        else if (Boolean.TRUE.equals(isRoomCapable) && (isResourceCapable == null || !isResourceCapable)) {
            // Room Only
            platform = dbRepo.platformRepository.findOneByPlatformIdAndAllowRooms(platformId, true);
        }
        else {
            // Room and Resource
            platform = dbRepo.platformRepository.findOneByPlatformIdAndAllowRoomsAndAllowResources(platformId, true, true);
        }

        if (platform.isEmpty()) {
            log.error("{}Error: Suitable platform not found", logPrefix);
            return null;
        }
        Platform p = (Platform) platform.get();
        log.debug("{}Found platform: {}", logPrefix, p);
        PlatformDTO plat = PlatformDTO.mapDataObject(p);
        return plat;

    }

    void writeAllocation(E164NumberDTO e164, String auditUsername) throws AllocationException {
        final String logPrefix = "writeAllocation() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Saving record: {}", logPrefix, e164);

        log.debug("{}Getting E164 to update from DB", logPrefix);
        E164Number dbE164 = dbRepo.e164NumberRepository.findById(e164.getE164NumberId()).orElseThrow(() -> new DatabaseException("Could not find E164 number in database"));
        E164Number oldE164 = dbE164.clone();

        AllocatedObject dbAo = null;
        if (e164.getStatus() == NumberStatus.ALLOCATED) {
            log.debug("{}Getting allocated object from DB", logPrefix);
            dbAo = dbRepo.allocateObjectRepository.findById(e164.getAllocatedObjectId()).orElseThrow(() -> new DatabaseException("Could not find user in database"));
        }

        log.debug("{}Setting up E164 allocation parameters", logPrefix);
        dbE164.setAllocatedObject(dbAo);
        dbE164.setAllocationType(e164.getAllocationType());
        dbE164.setDescription(e164.getDescription());
        dbE164.setLastAllocationTime(e164.getLastAllocationTime());
        dbE164.setStatus(e164.getStatus());

        log.info("{}Writing to database: {}", logPrefix, dbE164);
        try {
            E164Number newE164 = dbRepo.e164NumberRepository.save(dbE164);
            Audit auditRow = Audit.createAuditRecord(auditUsername, oldE164, newE164);
            log.debug("{}Writing audit record: {}", logPrefix, auditRow);
            dbRepo.auditRepository.saveAndFlush(auditRow);
        }
        catch (Exception ex) {
            log.error("{}Exception encountered {}", logPrefix, ex.getMessage());
            log.warn("{}Throwing exception", logPrefix);
            throw new DatabaseException("Could not save allocation to database", ex);
        }
    }

    AllocatedObjectDTO getOrCreateObject(String objectName) {
        final String logPrefix = "getOrCreateObject() - ";
        log.trace("{}Entering method", logPrefix);
        log.debug("{}Checking if object {} exists", logPrefix, objectName);
        AllocatedObjectDTO aoDTO = new AllocatedObjectDTO();

        AllocatedObject ao = dbRepo.allocateObjectRepository.findByName(objectName);
        if (ao != null) {
            log.info("{}Found existing object: {}", logPrefix, ao);
            aoDTO.setAllocatedObjectId(ao.getAllocatedObjectId())
                    .setName(ao.getName());
            return aoDTO;
        }

        log.info("{}{} was not found, creating new object", logPrefix, objectName);
        ao = new AllocatedObject();
        ao.setName(objectName);
        ao = dbRepo.allocateObjectRepository.saveAndFlush(ao);
        aoDTO.setAllocatedObjectId(ao.getAllocatedObjectId())
                .setName(objectName);

        return aoDTO;
    }

    List<String> getAllocatedE164(String username) {
        final String logPrefix = "getAllocatedE164() - ";
        log.trace("{}Entering method", logPrefix);

        log.debug("{}Checking if {} has an existing DID allocated", logPrefix, username);
        List<String> allocNum = new ArrayList<>();

        try {
            AllocatedObject allocUser = dbRepo.allocateObjectRepository.findByName(username);
            if (allocUser == null) {
                log.debug("{}User does not have an allocation", logPrefix);
            }
            else if (allocUser.getE164numbers() != null) {
                log.debug("{}User has an allocation entity, getting associated numbers", logPrefix);
                for (E164Number e164 : allocUser.getE164numbers()) {
                    String e164Number = e164.getE164();
                    log.trace("{} Found number -> {}", logPrefix, e164);
                    allocNum.add(e164Number);
                }
            }
            else {
                log.warn("{}Allocation entity does not have any numbers associated", logPrefix);
            }
        }
        catch (Exception ex) {
            log.error("{}Exception whilst looking up allocated numbers", logPrefix, ex);
        }
        log.info("{}Returning '{}'", logPrefix, allocNum);
        return allocNum;
    }

    E164NumberDTO allocateNumberForSite(SiteDTO site, PlatformDTO platform, List<PrefixDTO> preferredPrefixes) throws AllocationException {
        final String logPrefix = "allocateNumberForSite() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Finding a spare E164 allocation", logPrefix);

        E164NumberDTO allocatedE164;
        List<E164Number> numbers = new ArrayList<>();

        log.debug("{}Getting platform from database", logPrefix);
        Platform dbPlat = null;
        try {
            dbPlat = dbRepo.platformRepository.getReferenceById(platform.getPlatformId());
            if (dbPlat == null || dbPlat.getPlatformId() == null || dbPlat.getPlatformId() == 0) {
                throw new Exception();
            }
        }
        catch (Exception ex) {
            log.error("{}Platform {} not found in DB", logPrefix, platform);
            throw new IllegalArgumentException("Platform " + platform.toString() + " does not exist");
        }
        log.trace("{}Received platform {}", logPrefix, dbPlat);

        log.debug("{}Getting site from Database", logPrefix);
        Site dbSite = null;
        try {
            dbSite = dbRepo.siteRepository.getReferenceById(site.getSiteId());
            if (dbSite == null || dbSite.getSiteId() == null || dbSite.getSiteId() == 0) {
                throw new Exception();
            }
        }
        catch (Exception ex) {
            log.error("{}Site {} not found in DB", logPrefix, site);
            throw new IllegalArgumentException("Site " + site.toString() + " does not exist");

        }
        log.trace("{}Received site {}", logPrefix, dbSite);

        // Is preferred prefix defined?
        if (preferredPrefixes != null) {
            for (PrefixDTO preferredPrefix : preferredPrefixes) {
                log.trace("{}Checking Preferred Prefix: {}", logPrefix, preferredPrefix);
                if (preferredPrefix != null && preferredPrefix.getAllowAllocation()) {
                    NumberPrefix dbPrefix;
                    try {
                        dbPrefix = dbRepo.prefixRepository.getReferenceById(preferredPrefix.getPrefixId());
                        if (dbPrefix != null && dbPrefix.getPrefixId() != null && dbPrefix.getPrefixId() != 0) {
                            // Check to see if there's an available number for that platform in the preferred prefix.
                            if (dbPrefix.getAllowAllocation()) {
                                log.debug("{}Prefix allows allocation, finding number", logPrefix);
                                numbers = dbRepo.e164NumberRepository.getNumbersByPrefixAndE164Platform(PageRequest.of(0, 3), dbPrefix, dbPlat, NumberStatus.UNALLOCATED.getId());
                                if (!numbers.isEmpty()) {
                                    log.trace("{}Found numbers, exiting loop", logPrefix);
                                    break;
                                }
                                else {
                                    log.trace("{}No number free in this prefix, continuing loop", logPrefix);
                                }
                            }
                            else {
                                log.warn("{}Requested prefix does not allow allocation, falling back to normal allocation", logPrefix);
                            }

                        }
                        else {
                            log.warn("{}Requested prefix does not not exist in database, falling back to normal allocation", logPrefix);
                        }
                    }
                    catch (Exception ex) {
                        log.warn("{}Requested prefix does not not exist in database, falling back to normal allocation", logPrefix);
                    }
                }
            }
        }

        Long reuseInterval = Long.valueOf(env.getProperty("allocation.reuse-delay-days", "30"));
        OffsetDateTime searchDate = OffsetDateTime.now();
        searchDate = searchDate.minusDays(reuseInterval);
        log.trace("{} -- Reuse datetime: {}", logPrefix, searchDate.format(DateTimeFormatter.ISO_DATE_TIME));

        if (numbers.isEmpty()) {
            log.trace("{}-- Checking for an available number at site in platform default prefixes", logPrefix);
            numbers = dbRepo.e164NumberRepository.getNumbersByPrefixPlatform(PageRequest.of(0, 3), dbSite, dbPlat, NumberStatus.UNALLOCATED.getId(), true, searchDate);
            log.trace("{} >> By site Default Prefix: {}", logPrefix, numbers);
            log.info("{}## Found {} free numbers", logPrefix, numbers.size());
        }

        if (numbers.isEmpty()) {
            log.warn("{}No number found in platform default prefixes, checking remaining numbers", logPrefix);
            // no number found
            // Look at all remaining e164 numbers on the right platform, in the prefix list
            // Find a free number in that range, ordered by lastAllocationDate
            log.trace("{}-- Checking for an available number at site", logPrefix);
            numbers = dbRepo.e164NumberRepository.getNumbersByE164Platform(PageRequest.of(0, 3), dbSite, dbPlat, NumberStatus.UNALLOCATED.getId(), true, searchDate);
            log.trace("{} >> By E164 prefix: {}", logPrefix, numbers);
            log.info("{}## Found {} free numbers", logPrefix, numbers.size());
        }

        if (numbers.isEmpty()) {
            // if still no number found, error out.
            log.error("{}No free/available numbers found for the site", logPrefix);
            throw new NoAvailableE164Exception("No free/available numbers found for the site");
        }

        // number found = good
        // get the first one found
        E164Number allocDBNum = numbers.get(0);
        allocatedE164 = new E164NumberDTO()
                .setE164NumberId(allocDBNum.getE164NumberId())
                .setE164(allocDBNum.getE164())
                .setLastAllocationTime(OffsetDateTime.now())
                .setPlatformId(allocDBNum.getPlatform().getPlatformId())
                .setPrefixId(allocDBNum.getPrefix().getPrefixId())
                .setPlatformName(allocDBNum.getPlatform().getName())
                .setPrefixName(allocDBNum.getPrefix().getPrefix())
                .setStatus(NumberStatus.ALLOCATED);

        log.info("{}Successfully allocated number: {}", logPrefix, allocatedE164);
        return allocatedE164;
    }

    List<PrefixDTO> getPreferredRoomPrefix(SiteDTO site) {
        final String logPrefix = "getRoomPrefix() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Finding a preferred room prefix for site {}", logPrefix, site.getName());
        log.debug("{}Getting site from Database", logPrefix);
        Site dbSite = null;
        try {
            dbSite = dbRepo.siteRepository.getReferenceById(site.getSiteId());
            if (dbSite == null || dbSite.getSiteId() == null || dbSite.getSiteId() == 0) {
                throw new Exception();
            }
        }
        catch (Exception ex) {
            log.error("{}Site {} not found in DB", logPrefix, site);
            throw new IllegalArgumentException("Site " + site.toString() + " does not exist");
        }
        log.trace("{}Received site {}", logPrefix, dbSite);

        Set<NumberPrefix> prefixes = dbRepo.prefixRepository.getPreferredRoomPrefixesForSite(dbSite);
        if (prefixes == null || prefixes.isEmpty()) {
            log.info("{}No preferred prefix found", logPrefix);
            return null;
        }
        log.debug("{}Found {} prefixes", logPrefix, prefixes.size());
        List<PrefixDTO> preferred = new ArrayList<>();
        for (NumberPrefix prf : prefixes) {
            preferred.add(PrefixDTO.mapDataObject(prf));
        }
        log.trace("{}Returning list: {}", logPrefix, preferred);
        return preferred;
    }

    List<PlatformDTO> getAllPlatforms() {
        final String logPrefix = "getAllPlatforms() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up all platforms", logPrefix);
        List<Platform> platforms = dbRepo.platformRepository.findAll();
        List<PlatformDTO> outputList = new ArrayList<>();
        for (Platform p : platforms) {
            log.debug("{}Found platform: {}", logPrefix, p);
            outputList.add(PlatformDTO.mapDataObject(p));
        }
        return outputList;
    }

    List<PlatformDTO> getAllRoomEnabledPlatforms() {
        final String logPrefix = "getAllRoomEnabledPlatforms() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up all room-enabled platforms", logPrefix);
        List<Platform> platforms = dbRepo.platformRepository.findAllByAllowRooms(Boolean.TRUE);
        List<PlatformDTO> outputList = new ArrayList<>();
        for (Platform p : platforms) {
            log.debug("{}Found platform: {}", logPrefix, p);
            outputList.add(PlatformDTO.mapDataObject(p));
        }
        return outputList;
    }

    List<PlatformDTO> getAllResourceEnabledPlatforms() {
        final String logPrefix = "getAllResourceEnabledPlatforms() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up all resource-enabled platforms", logPrefix);
        List<Platform> platforms = dbRepo.platformRepository.findAllByAllowResources(Boolean.TRUE);
        List<PlatformDTO> outputList = new ArrayList<>();
        for (Platform p : platforms) {
            log.debug("{}Found platform: {}", logPrefix, p);
            outputList.add(PlatformDTO.mapDataObject(p));
        }
        return outputList;
    }

    E164NumberDTO getE64Number(String e164) {
        final String logPrefix = "getE64Number() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up E164 from Database: {}", logPrefix, e164);
        Optional<E164Number> dbE164 = dbRepo.e164NumberRepository.findByE164(e164);
        if (dbE164.isPresent()) {
            E164Number num = dbE164.get();
            log.debug("{}Found number: {}", logPrefix, num);
            return E164NumberDTO.mapDataObject(num);
        }
        else {
            log.warn("{}Did not find number in DB", logPrefix);
            return null;
        }

    }

    void removeAllocation(E164NumberDTO e164, String auditUsername) throws AllocationException {
        final String logPrefix = "removeAllocation() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Deallocating record: {}", logPrefix, e164);

        log.debug("{}Getting E164 to deallocate from DB", logPrefix);
        E164Number dbE164 = dbRepo.e164NumberRepository.findById(e164.getE164NumberId()).orElseThrow(() -> new DatabaseException("Could not find E164 number in database"));
        E164Number oldE164 = dbE164.clone();

        log.debug("{}Setting up E164 de-allocation parameters", logPrefix);
        dbE164.setAllocatedObject(null);
        dbE164.setAllocationType(NumberType.UNKNOWN);
        dbE164.setDescription(null);
        dbE164.setLastAllocationTime(OffsetDateTime.now());
        dbE164.setStatus(NumberStatus.UNALLOCATED);

        log.debug("{}Creating allocation audit record", logPrefix);
        AllocationAudit alAudit = new AllocationAudit()
                .setAllocatedObject(oldE164.getAllocatedObject())
                .setAllocationDate(oldE164.getLastAllocationTime())
                .setDeAllocationDate(dbE164.getLastAllocationTime())
                .setE164Number(dbE164)
                .setAllocationType(oldE164.getAllocationType());

        try {
            log.info("{}Writing to database: {}", logPrefix, dbE164);
            E164Number newE164 = dbRepo.e164NumberRepository.save(dbE164);
            log.info("{}Writing allocation audit record: {}", logPrefix, alAudit);
            dbRepo.allocationAuditRepository.save(alAudit);
            Audit auditRow = Audit.createAuditRecord(auditUsername, oldE164, newE164);
            log.debug("{}Writing audit record: {}", logPrefix, auditRow);
            dbRepo.auditRepository.saveAndFlush(auditRow);
        }
        catch (Exception ex) {
            log.error("{}Exception encountered {}", logPrefix, ex.getMessage());
            log.warn("{}Throwing exception", logPrefix);
            throw new DatabaseException("Could not save allocation to database", ex);
        }
    }
}
