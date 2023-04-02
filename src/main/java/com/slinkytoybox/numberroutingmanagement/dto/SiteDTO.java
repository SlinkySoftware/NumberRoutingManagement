/*
 *   NumberRoutingManagement - SiteDTO.java
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

import com.slinkytoybox.numberroutingmanagement.database.model.Site;
import com.slinkytoybox.numberroutingmanagement.database.types.AUState;
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
public class SiteDTO implements DTOResult {

    private Long siteId;
    private String name;
    private String adName;
    private String dialplan;
    private AUState state;

    public static SiteDTO mapDataObject(Site site) {
        if (site == null) {
            throw new IllegalArgumentException("Site DB Object cannot be null");
        }

        return new SiteDTO()
                .setSiteId(site.getSiteId())
                .setName(site.getName())
                .setAdName(site.getAdname())
                .setState(site.getState())
                .setDialplan(site.getDialplan());
    }

    public SiteDTO updateFromDataObject(Site site) {
        if (site == null) {
            throw new IllegalArgumentException("Site DB Object cannot be null");
        }
        this
                .setSiteId(site.getSiteId())
                .setName(site.getName())
                .setAdName(site.getAdname())
                .setState(site.getState())
                .setDialplan(site.getDialplan());
        return this;
    }

}
