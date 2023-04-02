/*
 *   NumberRoutingManagement - NumberType.java
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
package com.slinkytoybox.numberroutingmanagement.database.types;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
public enum NumberType {

    UNKNOWN(0, "Unknown"),
    EXTENSION(1, "Extension"),
    HUNTGROUP(2, "Hunt Group"),
    AUTOATTENDANT(3, "Auto Attendant"),
    CALLERID(4, "Caller ID Usage"),
    BARRED(5, "Barred");

    private final int id;
    private final String friendlyName;

    private NumberType(int id, String friendlyName) {
        this.id = id;
        this.friendlyName = friendlyName;
    }

    public static NumberType getType(Integer id) {

        if (id == null) {
            return null;
        }

        for (NumberType enumType : NumberType.values()) {
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
