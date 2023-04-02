/*
 *   NumberRoutingManagement - PlatformDTO.java
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
package com.slinkytoybox.numberroutingmanagement.dto;

import com.slinkytoybox.numberroutingmanagement.database.model.Platform;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Data
@ToString(exclude = {})
@Accessors(chain = true)
public class PlatformDTO implements DTOResult {

    private Long platformId;
    private String name;
    private String ipGroup;
    private Boolean defaultUserPlatform;
    private Boolean allowRooms;
    private Boolean allowResources;
    private Boolean powershellEnabled;

    public static PlatformDTO mapDataObject(Platform platform) {
        if (platform == null) {
            throw new IllegalArgumentException("Platform DB Object cannot be null");
        }

        return new PlatformDTO()
                .setPlatformId(platform.getPlatformId())
                .setName(platform.getName())
                .setIpGroup(platform.getIpGroup())
                .setDefaultUserPlatform(platform.getDefaultUserPlatform())
                .setAllowResources(platform.getAllowResources())
                .setAllowRooms(platform.getAllowRooms())
                .setPowershellEnabled(platform.getPowershellEnabled())
                ;
    }

    public PlatformDTO updateFromDataObject(Platform platform) {
        if (platform == null) {
            throw new IllegalArgumentException("Platform DB Object cannot be null");
        }
        this
                .setPlatformId(platform.getPlatformId())
                .setName(platform.getName())
                .setIpGroup(platform.getIpGroup())
                .setDefaultUserPlatform(platform.getDefaultUserPlatform())
                .setAllowResources(platform.getAllowResources())
                .setAllowRooms(platform.getAllowRooms())
                .setPowershellEnabled(platform.getPowershellEnabled())
                ;        
        return this;
    }
}
