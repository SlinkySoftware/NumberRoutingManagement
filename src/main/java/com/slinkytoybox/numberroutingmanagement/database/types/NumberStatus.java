/*
 *   NumberRoutingManagement - NumberStatus.Java
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
package com.slinkytoybox.numberroutingmanagement.database.types;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
public enum NumberStatus {

    UNALLOCATED(0, "Unallocated / Free"),
    ALLOCATED(1, "Allocated"),
    RESERVED(2, "Reserved for future use"),
    UNAVAILABLE(3, "Unavailable for use");

    private final int id;
    private final String friendlyName;

    private NumberStatus(int id, String friendlyName) {
        this.id = id;
        this.friendlyName = friendlyName;
    }

    public static NumberStatus getType(Integer id) {

        if (id == null) {
            return null;
        }

        for (NumberStatus enumType : NumberStatus.values()) {
            if (id.equals(enumType.getId())) {
                return enumType;
            }
        }
        throw new IllegalArgumentException("No matching type for id " + id);
    }

    public int getId() {
        return id;
    }
    
        public String getFriendlyName() {
        return friendlyName;
    }
}
