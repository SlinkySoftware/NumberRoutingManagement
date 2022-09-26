/*
 *   NumberRoutingManagement - PrefixDTO.java
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
package com.slinkytoybox.numberroutingmanagement.dto;

import com.slinkytoybox.numberroutingmanagement.database.model.NumberPrefix;
import com.slinkytoybox.numberroutingmanagement.database.types.AUState;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Data
@ToString(exclude = {})
@Accessors(chain = true)
@Slf4j
public class PrefixDTO implements DTOResult {

    private Long prefixId;
    private String prefix;
    private AUState state;
    private Boolean allowAllocation = false;
    private Boolean preferredForRooms;
    private Boolean preferredForResources;

    private Integer allocationOrder;
    private Long defaultPlatformId;
    private Long siteId;

    private String defaultPlatformName;
    private Long extensionCount = 0L;
    private String siteName;

    public static PrefixDTO mapDataObject(NumberPrefix dbPrefix) {
        if (dbPrefix == null) {
            throw new IllegalArgumentException("Prefix DB Object cannot be null");
        }

        return new PrefixDTO()
                .setPrefixId(dbPrefix.getPrefixId())
                .setPrefix(dbPrefix.getPrefix())
                .setState(dbPrefix.getState())
                .setAllowAllocation(dbPrefix.getAllowAllocation())
                .setPreferredForRooms(dbPrefix.getPreferredForRooms())
                .setPreferredForResources(dbPrefix.getPreferredForRooms())
                .setAllocationOrder(dbPrefix.getAllocationOrder())
                .setDefaultPlatformId((dbPrefix.getDefaultPlatform() == null ? null : dbPrefix.getDefaultPlatform().getPlatformId()))
                .setDefaultPlatformName((dbPrefix.getDefaultPlatform() == null ? null : dbPrefix.getDefaultPlatform().getName()))
                .setSiteId((dbPrefix.getSite() == null ? null : dbPrefix.getSite().getSiteId()))
                .setSiteName((dbPrefix.getSite() == null ? null : dbPrefix.getSite().getName()))
                .setExtensionCount(dbPrefix.getE164numbers() == null ? 0L : dbPrefix.getE164numbers().size());
    }

    public PrefixDTO updateFromDataObject(NumberPrefix dbPrefix) {
        if (dbPrefix == null) {
            throw new IllegalArgumentException("Prefix DB Object cannot be null");
        }
        return this
                .setPrefixId(dbPrefix.getPrefixId())
                .setPrefix(dbPrefix.getPrefix())
                .setState(dbPrefix.getState())
                .setAllowAllocation(dbPrefix.getAllowAllocation())
                .setPreferredForRooms(dbPrefix.getPreferredForRooms())
                .setPreferredForResources(dbPrefix.getPreferredForRooms())
                .setAllocationOrder(dbPrefix.getAllocationOrder())
                .setDefaultPlatformId((dbPrefix.getDefaultPlatform() == null ? null : dbPrefix.getDefaultPlatform().getPlatformId()))
                .setDefaultPlatformName((dbPrefix.getDefaultPlatform() == null ? null : dbPrefix.getDefaultPlatform().getName()))
                .setSiteId((dbPrefix.getSite() == null ? null : dbPrefix.getSite().getSiteId()))
                .setSiteName((dbPrefix.getSite() == null ? null : dbPrefix.getSite().getName()))
                .setExtensionCount(dbPrefix.getE164numbers() == null ? 0L : dbPrefix.getE164numbers().size());
    }

}
