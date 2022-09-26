/*
 *   number-routing-management - PowershellFunctions.java
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

import com.slinkytoybox.numberroutingmanagement.dto.ADUserDTO;
import com.slinkytoybox.numberroutingmanagement.dto.E164NumberDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
@Component
public class PowershellFunctions {
    
    @Autowired
    private Environment env;
    
    public String getCreateUserCommand(ADUserDTO user, E164NumberDTO e164Number, String dialPlan) {
        final String logPrefix = "getCreateUserCommands() - ";
        log.trace("{}Entering method", logPrefix);
        String commandList = getIndexedProperties("powershell.new-teams-acct");
        
        log.debug("{}Replacing variables with values", logPrefix);
        commandList = commandList.replace("~UPN~", user.getUserPrincipalName());
        commandList = commandList.replace("~USER~", user.getSAMAccountName());
        commandList = commandList.replace("~E164~", e164Number.getE164());
        commandList = commandList.replace("~DP~", dialPlan);
        log.debug("{}Final String: {}", logPrefix, commandList);

        return commandList;
    }
    
    
    public String getCreateRoomCommand(String roomName, E164NumberDTO e164Number, String dialPlan) {
        final String logPrefix = "getCreateRoomCommand() - ";
        log.trace("{}Entering method", logPrefix);
        String commandList = getIndexedProperties("powershell.new-teams-acct");
        
        log.debug("{}Replacing variables with values", logPrefix);
        commandList = commandList.replace("~UPN~", roomName);
        commandList = commandList.replace("~E164~", e164Number.getE164());
        commandList = commandList.replace("~DP~", dialPlan);
        log.debug("{}Final String: {}", logPrefix, commandList);

        return commandList;
    }
    
    private String getIndexedProperties (String propertyPrefix) {
        final String logPrefix = "getIndexedProperties() - ";
        log.trace("{}Entering method", logPrefix);
        String commandList = "";
        Integer i=0;
        log.debug("{}Scanning for commands from configuration with prefix {}", logPrefix, propertyPrefix);

        while (true) {
            i++;
            String prefixToGet = propertyPrefix + "." + i.toString();
            log.trace("{}-- Scanning {}", logPrefix, prefixToGet);
            if (env.containsProperty(prefixToGet)) {
                log.trace("{}---> Found, getting data", logPrefix);
                String data = env.getProperty(prefixToGet);
                commandList += (commandList.isEmpty() ? data : "\n" + data );
            }
            else {
                log.trace("{}---> Non-existent, exiting loop", logPrefix);
                break;
            }
        }
        log.trace("{}Command list found: {}", logPrefix, commandList);
        return commandList;
    }
    
}
