/*
 *   number-routing-management - APIController.java
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
package com.slinkytoybox.numberroutingmanagement.controller;

import com.slinkytoybox.numberroutingmanagement.businesslogic.APILogic;
import java.security.Principal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Michael Junek (michaeljunek@nbnco.com.au)
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class APIController {

    @Autowired
    private APILogic apiLogic;

    @PostMapping(path = "/user", produces = "application/json")
    public ResponseEntity<Map<String, Object>> createUser(String username) {
        return null;
    }

    @DeleteMapping(path = "/user/{username}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deleteUser(Principal principal, @PathVariable("username") String username) {
        final String logPrefix = "deleteUser() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Dellocating all numbers for user {}", logPrefix, username);
        Map<String, Object> result = apiLogic.deallocateObject(username, principal.getName());
        if (result == null || !result.containsKey("status") || ((String) result.get("status")).equalsIgnoreCase("failed")) {
            log.error("{}Error encountered during deallocation", logPrefix);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        else {
            log.info("{}Dellocation result was {} for {}", logPrefix, result.get("status"), username);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @DeleteMapping(path = "/e164/{e164Number}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deleteE164(Principal principal, @PathVariable("e164Number") String e164Number) {
        final String logPrefix = "deleteE164() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Dellocating E164 Number {}", logPrefix, e164Number);
        Map<String, Object> result = apiLogic.deallocateNumber(e164Number, principal.getName());
        if (result == null || !result.containsKey("status") || ((String) result.get("status")).equalsIgnoreCase("failed")) {
            log.error("{}Error encountered during deallocation", logPrefix);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        else {
            log.info("{}Dellocation result was {} for {}", logPrefix, result.get("status"), e164Number);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }
}
