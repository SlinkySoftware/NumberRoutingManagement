/*
 *   NumberRoutingManagement - E164NumberDTO.java
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

import com.slinkytoybox.numberroutingmanagement.database.model.E164Number;
import com.slinkytoybox.numberroutingmanagement.database.types.*;
import java.time.OffsetDateTime;
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
public class E164NumberDTO implements DTOResult {

    private Long e164NumberId;
    private String e164;
    private Long prefixId;
    private Long platformId;
    private Long allocatedObjectId;
    private NumberStatus status;
    private NumberType allocationType;
    private OffsetDateTime lastAllocationTime;
    private String description;

    private String platformName;
    private String prefixName;
    private String allocatedObjectName;

    public Integer getNumberStatusId() {
        if (status == null) {
            return null;
        }
        return status.getId();
    }

    public Integer getNumberTypeId() {
        if (allocationType == null) {
            return null;
        }
        return allocationType.getId();
    }

    public static E164NumberDTO mapDataObject(E164Number e164) {
        if (e164 == null) {
            throw new IllegalArgumentException("E164 DB Object cannot be null");
        }
        
        return new E164NumberDTO()
                .setE164NumberId(e164.getE164NumberId())
                .setE164(e164.getE164())
                .setPrefixId((e164.getPrefix() == null ? null : e164.getPrefix().getPrefixId()))
                .setPrefixName((e164.getPrefix() == null ? null : e164.getPrefix().getPrefix()))
                .setPlatformId((e164.getPlatform() == null ? null : e164.getPlatform().getPlatformId()))
                .setPlatformName((e164.getPlatform() == null ? null : e164.getPlatform().getName()))
                .setAllocatedObjectId((e164.getAllocatedObject()== null ? null : e164.getAllocatedObject().getAllocatedObjectId()))
                .setAllocatedObjectName((e164.getAllocatedObject() == null ? null : e164.getAllocatedObject().getName()))
                .setStatus(e164.getStatus())
                .setLastAllocationTime(e164.getLastAllocationTime())
                .setAllocationType(e164.getAllocationType())
                .setDescription(e164.getDescription())
                ;
    }

    public E164NumberDTO updateFromDataObject(E164Number e164) {
        if (e164 == null) {
            throw new IllegalArgumentException("E164 DB Object cannot be null");
        }
        return this
                .setE164NumberId(e164.getE164NumberId())
                .setE164(e164.getE164())
                .setPrefixId((e164.getPrefix() == null ? null : e164.getPrefix().getPrefixId()))
                .setPrefixName((e164.getPrefix() == null ? null : e164.getPrefix().getPrefix()))
                .setPlatformId((e164.getPlatform() == null ? null : e164.getPlatform().getPlatformId()))
                .setPlatformName((e164.getPlatform() == null ? null : e164.getPlatform().getName()))
                .setAllocatedObjectId((e164.getAllocatedObject()== null ? null : e164.getAllocatedObject().getAllocatedObjectId()))
                .setAllocatedObjectName((e164.getAllocatedObject() == null ? null : e164.getAllocatedObject().getName()))
                .setStatus(e164.getStatus())
                .setLastAllocationTime(e164.getLastAllocationTime())
                .setAllocationType(e164.getAllocationType())
                .setDescription(e164.getDescription())
                ;
    }

}
