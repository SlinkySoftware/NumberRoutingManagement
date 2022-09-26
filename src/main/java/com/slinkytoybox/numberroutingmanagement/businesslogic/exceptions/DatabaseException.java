/*
 *   number-routing-management - DatabaseException.java
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
package com.slinkytoybox.numberroutingmanagement.businesslogic.exceptions;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
public class DatabaseException extends AllocationException{
    
    public DatabaseException(String message, Throwable t) {
        super(message, t);
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException() {
        super();
    }    
}
