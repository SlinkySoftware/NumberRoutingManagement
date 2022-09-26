/*
 *   number-routing-management - AdministrationLogic.java
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

import com.slinkytoybox.numberroutingmanagement.database.model.Platform;
import com.slinkytoybox.numberroutingmanagement.database.model.Site;
import com.slinkytoybox.numberroutingmanagement.database.repository.MasterDBRepository;
import com.slinkytoybox.numberroutingmanagement.dto.DTOHashMap;
import com.slinkytoybox.numberroutingmanagement.dto.DataTablesRequest;
import com.slinkytoybox.numberroutingmanagement.dto.DataTablesResult;
import com.slinkytoybox.numberroutingmanagement.dto.PlatformDTO;
import com.slinkytoybox.numberroutingmanagement.dto.ResultMap;
import com.slinkytoybox.numberroutingmanagement.dto.SiteDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Component
@Slf4j
public class AdministrationLogic {

    @Autowired
    private MasterDBRepository dbRepo;

    public ResultMap getAllPlatforms(DataTablesRequest dtRequest) {
        final String logPrefix = "getAllPlatforms() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up all Platforms in database.", logPrefix);

        Page<Platform> platforms;
        DataTablesResult dtResult = new DataTablesResult();

        Integer page = dtRequest.getStart() / dtRequest.getLength();
        log.debug("{}Requesting page {} of length {}", logPrefix, page, dtRequest.getLength());

        Sort sortOrder = dtRequest.getSort();
        dtResult.setDraw(dtRequest.getDraw());

        log.debug("{}Running DB query for all Platforms", logPrefix);
        PageRequest pageRequest = PageRequest.of(page, dtRequest.getLength(), sortOrder);
        log.trace("{}PageRequest: {}", logPrefix, pageRequest);
        Long rowCount = dbRepo.platformRepository.count();
        dtResult.setRecordsTotal(rowCount);

        platforms = dbRepo.platformRepository.findAll(pageRequest);
        dtResult.setRecordsFiltered(rowCount);

        List<PlatformDTO> platformResultList = new ArrayList<>();
        log.debug("{}Creating result list", logPrefix);
        for (Platform platform : platforms) {
            PlatformDTO platformResult = PlatformDTO.mapDataObject(platform);
            platformResultList.add(platformResult);
            log.trace("{}Added : {}", logPrefix, platformResult);
        }
        log.debug("{}Returning result list of size {} members", logPrefix, platformResultList.size());
        dtResult.setData(platformResultList);

        return new ResultMap(HttpStatus.OK, dtResult);
    }

    public ResultMap getPlatformConfig() {
        final String logPrefix = "getPlatformConfig() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up all Platform config from database.", logPrefix);
        List<String> platformList = new ArrayList<>();
        List<String> ipGroupList = new ArrayList<>();
        log.debug("{}Running DB query for all Platforms", logPrefix);

        List<Platform> platforms = dbRepo.platformRepository.findAll();
        Long defaultPlatformId = -1L;
        for (Platform platform : platforms) {
            if (platform.getDefaultUserPlatform()) {
                defaultPlatformId = platform.getPlatformId();
            }
            platformList.add(platform.getName());
            ipGroupList.add(platform.getIpGroup());
        }
        log.debug("{}Returning config list", logPrefix);

        DTOHashMap<String, Object> resultMap = new DTOHashMap<>();
        resultMap.put("inUseNames", platformList);
        resultMap.put("inUseIpGroups", ipGroupList);
        resultMap.put("defaultPlatformId", defaultPlatformId.toString());
        return new ResultMap(HttpStatus.OK, resultMap);

    }

    public ResultMap getPlatform(Long platformId) {
        final String logPrefix = "getPlatform() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up Platforms ID {} in database.", logPrefix, platformId);

        PlatformDTO platformResult = new PlatformDTO();
        Optional<Platform> optPlatform = dbRepo.platformRepository.findById(platformId);
        HttpStatus responseStatus;
        if (optPlatform.isPresent()) {
            Platform platform = optPlatform.get();
            platformResult.updateFromDataObject(platform);
            responseStatus = HttpStatus.OK;
        }
        else {
            responseStatus = HttpStatus.NOT_FOUND;
        }
        return new ResultMap(responseStatus, platformResult);
    }

    public ResultMap updatePlatform(PlatformDTO platformUpdate, boolean update) {
        final String logPrefix = "updatePlatform() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Modification Request: {}", logPrefix, platformUpdate);
        Platform platform;
        if (update) {
            log.debug("{}Looking up Platform in database", logPrefix);
            Optional<Platform> optPlatform = dbRepo.platformRepository.findById(platformUpdate.getPlatformId());
            if (optPlatform.isPresent()) {
                platform = optPlatform.get();
            }
            else {
                log.error("{}Platform {} not found in database", logPrefix, platformUpdate.getPlatformId());
                DTOHashMap<String, Boolean> dtResult = new DTOHashMap<>();
                dtResult.put("success", Boolean.FALSE);
                return new ResultMap(HttpStatus.NOT_FOUND, dtResult);
            }
        }
        else {
            log.debug("{}Creating new Platform", logPrefix);
            platform = new Platform();
        }

        log.debug("{}Setting new/updated Platform paramaters", logPrefix);
        platform.setName(platformUpdate.getName());
        platform.setIpGroup(platformUpdate.getIpGroup());
        platform.setDefaultUserPlatform(platformUpdate.getDefaultUserPlatform());
        log.trace("{}Saving Platform to database", logPrefix);
        platform = dbRepo.platformRepository.save(platform);
        log.info("{}Updated Platform: {}", logPrefix, platform);
        DTOHashMap<String, Boolean> dtResult = new DTOHashMap<>();
        dtResult.put("success", Boolean.TRUE);
        return new ResultMap(HttpStatus.OK, dtResult);
    }

    public ResultMap getAllSites(DataTablesRequest dtRequest) {
        final String logPrefix = "getAllSites() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up all sites in database.", logPrefix);

        Page<Site> sites;
        DataTablesResult dtResult = new DataTablesResult();

        Integer page = dtRequest.getStart() / dtRequest.getLength();
        log.debug("{}Requesting page {} of length {}", logPrefix, page, dtRequest.getLength());

        Sort sortOrder = dtRequest.getSort();
        dtResult.setDraw(dtRequest.getDraw());

        log.debug("{}Running DB query for all  Sites", logPrefix);
        PageRequest pageRequest = PageRequest.of(page, dtRequest.getLength(), sortOrder);
        log.trace("{}PageRequest: {}", logPrefix, pageRequest);

        Long rowCount = dbRepo.siteRepository.count();
        dtResult.setRecordsTotal(rowCount);

        sites = dbRepo.siteRepository.findAll(pageRequest);
        dtResult.setRecordsFiltered(rowCount);

        List<SiteDTO> siteResultList = new ArrayList<>();
        log.debug("{}Creating result list", logPrefix);
        for (Site site : sites) {
            SiteDTO siteResult = SiteDTO.mapDataObject(site);
            siteResultList.add(siteResult);
            log.trace("{}Added : {}", logPrefix, siteResult);
        }
        log.debug("{}Returning result list of size {} members", logPrefix, siteResultList.size());
        dtResult.setData(siteResultList);

        return new ResultMap(HttpStatus.OK, dtResult);
    }

    public ResultMap getSiteConfig() {
        final String logPrefix = "getSiteConfig() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up all site config from database.", logPrefix);
        List<String> siteList = new ArrayList<>();
        List<String> adSiteList = new ArrayList<>();
        log.debug("{}Running DB query for all sites", logPrefix);

        List<Site> sites = dbRepo.siteRepository.findAll();
        for (Site site : sites) {
            siteList.add(site.getName());
            adSiteList.add(site.getAdname());
        }

        log.debug("{}Returning config list", logPrefix);

        DTOHashMap<String, Object> resultMap = new DTOHashMap<>();
        resultMap.put("inUseNames", siteList);
        resultMap.put("inUseAdNames", adSiteList);
        return new ResultMap(HttpStatus.OK, resultMap);

    }

    public ResultMap getSite(Long siteId) {
        final String logPrefix = "getSite() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Looking up siteId {} in database.", logPrefix, siteId);

        SiteDTO siteResult = new SiteDTO();
        Optional<Site> optSite = dbRepo.siteRepository.findById(siteId);
        HttpStatus responseStatus;
        if (optSite.isPresent()) {
            Site site = optSite.get();
            siteResult.updateFromDataObject(site);
            responseStatus = HttpStatus.OK;
        }
        else {
            responseStatus = HttpStatus.NOT_FOUND;
        }
        return new ResultMap(responseStatus, siteResult);
    }

    public ResultMap updateSite(SiteDTO siteUpdate, boolean update) {
        final String logPrefix = "updateSite() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Modification Request: {}", logPrefix, siteUpdate);
        Site site;
        if (update) {
            log.debug("{}Looking up site in database", logPrefix);
            Optional<Site> optSite = dbRepo.siteRepository.findById(siteUpdate.getSiteId());
            if (optSite.isPresent()) {
                site = optSite.get();
            }
            else {
                log.error("{}Site {} not found in database", logPrefix, siteUpdate.getSiteId());
                DTOHashMap<String, Boolean> dtResult = new DTOHashMap<>();
                dtResult.put("success", Boolean.FALSE);
                return new ResultMap(HttpStatus.NOT_FOUND, dtResult);
            }
        }
        else {
            log.debug("{}Creating new Site", logPrefix);
            site = new Site();
        }

        log.debug("{}Setting new/updated site paramaters", logPrefix);
        site.setName(siteUpdate.getName());
        site.setAdname(siteUpdate.getAdName());
        site.setDialplan(siteUpdate.getDialplan());
        site.setState(siteUpdate.getState());
        
        log.trace("{}Saving Site to database", logPrefix);
        site = dbRepo.siteRepository.save(site);
        log.info("{}Updated Site: {}", logPrefix, site);
        DTOHashMap<String, Boolean> dtResult = new DTOHashMap<>();
        dtResult.put("success", Boolean.TRUE);
        return new ResultMap(HttpStatus.OK, dtResult);
    }

}
