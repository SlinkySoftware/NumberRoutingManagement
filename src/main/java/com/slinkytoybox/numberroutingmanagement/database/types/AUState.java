/*
 *   NumberRoutingManagement - AUState.java
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
public enum AUState {
    
    UNKNOWN(0),
    NSW(1),
    VIC(2),
    QLD(3),
    TAS(4),
    SA(5),
    WA(6),
    NT(7),
    ACT(8),
    INTERNATIONAL(9);

    private final int id;

    private AUState(int id) {
        this.id = id;
    }

    public static AUState getType(Integer id) {
        if (id == null) {
            return null;
        }
        for (AUState enumType : AUState.values()) {
            if (id.equals(enumType.getId())) {
                return enumType;
            }
        }
        throw new IllegalArgumentException("No matching type for id " + id);
    }

    public int getId() {
        return id;
    }
}
