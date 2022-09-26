/*
 *   number-routing-management - APILogic.java
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

import com.slinkytoybox.numberroutingmanagement.businesslogic.exceptions.AllocationException;
import com.slinkytoybox.numberroutingmanagement.database.types.NumberStatus;
import com.slinkytoybox.numberroutingmanagement.dto.E164NumberDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 *
 * @author Michael Junek (michaeljunek@nbnco.com.au)
 */
@Component
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {AllocationException.class})
public class APILogic {

    @Autowired
    private PowershellFunctions powershellFunctions;

    @Autowired
    private ADFunctions adFunctions;

    @Autowired
    private DatabaseFunctions dbFunctions;

    public Map<String, Object> deallocateObject(String objectName, String auditUserName) {
        final String logPrefix = "deallocateObject() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Searching for allocated numbers for object '{}' in database.", logPrefix, objectName);

        Map<String, Object> result = new HashMap<>();
        List<String> existingNumbers = dbFunctions.getAllocatedE164(objectName);

        if (existingNumbers.isEmpty()) {
            log.warn("{}Object does not have an existing allocation, nothing to do", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "Object does not have a number allocation");
            result.put("objectName", objectName);
            return result;
        }
        Integer successCount = 0;
        Integer failureCount = 0;
        log.debug("{}Found {} numbers: {}", logPrefix, existingNumbers.size(), existingNumbers);
        for (String number : existingNumbers) {
            log.trace("{}Processing {}", logPrefix, number);
            Map<String, Object> localResult = deallocateE164Number(number, auditUserName);
            result.put(number, localResult);
            if (localResult.getOrDefault("success", 0).equals(1)) {
                successCount++;
            }
            else {
                failureCount++;
            }
        }
        if (existingNumbers.size() == successCount) {
            // all succeeded
            result.put("status", "success");
        }
        else if (existingNumbers.size() == failureCount) {
            // all failed
            result.put("status", "failed");
        }
        else {
            // some failed
            result.put("status", "partial");
        }
        return result;
    }

    public Map<String, Object> deallocateNumber(String e164Number, String auditUserName) {
        final String logPrefix = "deallocateNumber() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Searching for allocated number '{}' in database.", logPrefix, e164Number);

        Map<String, Object> result = new HashMap<>();
        E164NumberDTO e164 = dbFunctions.getE64Number(e164Number);

        if (e164 == null) {
            log.warn("{}Object does not exist", logPrefix);
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("errorMessage", "E164 Number does not exist");
            result.put("e164Number", e164Number);
            return result;
        }
        log.trace("{}Processing {}", logPrefix, e164Number);
        Map<String, Object> localResult = deallocateE164Number(e164Number, auditUserName);
        result.put(e164Number, localResult);
        if (localResult.getOrDefault("success", 0).equals(1)) {
            result.put("status", "success");
        }
        else {
            result.put("status", "failed");
        }
        return result;
    }

    private Map<String, Object> deallocateE164Number(String e164Number, String auditUserName) {
        final String logPrefix = "deallocateE164Number() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Getting E164 from database.", logPrefix);
        E164NumberDTO e164 = dbFunctions.getE64Number(e164Number);
        Map<String, Object> result = new HashMap<>();
        if (e164 == null) {
            log.error("{}E164 number not found in database {}", logPrefix, e164Number);
            throw new IllegalArgumentException("E164 number not found in database");
        }
        result.put("objectBeforeDeletion", e164);

        if (e164.getStatus() == NumberStatus.UNALLOCATED) {
            log.warn("{}E164 {} is not allocated, nothing to do");
            result.put("success", 0);
            result.put("message", "Number is not allocated");
            return result;
        }

        try {
            dbFunctions.removeAllocation(e164, auditUserName);
            result.put("success", 1);
            log.info("{}Number deallocated successfully", logPrefix);
        }
        catch (AllocationException ex) {
            log.warn("{}Rolling back SQL transactions", logPrefix);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", 0);
            result.put("message", ex.getMessage());
        }

        return result;
    }

}
