/*
 *   number-routing-management - ADUserDTO.java
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
package com.slinkytoybox.numberroutingmanagement.dto;

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
public class ADUserDTO implements DTOResult {
    private String DN = "";                 // distinguishsedName
    private String CN = "";                 // cn
    private String userPrincipalName = "";  // userPrincipalName
    private String firstName = "";          // givenName
    private String surname = "";            // sn
    private String sAMAccountName = "";     // sAMAccountName
    private String email = "";              // mail
    private String company = "";            // company
    private String position = "";           // title
    private String department = "";         // department
    private String address = "";            // streetAddress
    private String city = "";               // l
    private String country = "";            // co
    private String state = "";              // st
    private String postcode = "";           // postalCode
    private String office = "";             // physicalDeliveryOfficeName
    private String manager = "";            // manager
    private boolean accountDisabled = false;
    private boolean accountLocked = false;
    private String e164 = "";
    
}
