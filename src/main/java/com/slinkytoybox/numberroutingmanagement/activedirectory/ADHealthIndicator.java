/*
 *   number-routing-management - ADHealthIndicator.java
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
package com.slinkytoybox.numberroutingmanagement.activedirectory;

import com.slinkytoybox.numberroutingmanagement.activedirectory.exceptions.ActiveDirectoryException;
import com.slinkytoybox.numberroutingmanagement.businesslogic.ADFunctions;
import java.time.Clock;
import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Component
@Slf4j
public class ADHealthIndicator implements HealthIndicator {

    @Autowired
    private ADFunctions adFunctions;

    private ADConnection adConn;

    @PostConstruct
    private void setADConnection() {
        final String logPrefix = "setADConnection() - ";
        log.trace("{}Entering method", logPrefix);
        this.adConn = adFunctions.getConnection();
    }

    @Override
    public Health health() {
        final String logPrefix = "health() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Checking AD health", logPrefix);
        String name;
        DirContext ctx = null;
        Clock clock = Clock.systemDefaultZone();
        long startMillis =clock.millis();
        try {
            ctx = adConn.getContext();
            name = ctx.getNameInNamespace();
        }
        catch (ActiveDirectoryException ex) {
            log.error("{}ActiveDirectory Exception:", logPrefix, ex);
            return Health.down().withDetail("ActiveDirectoryException", ex.getMessage()).build();
        }
        catch (NamingException ex) {
            log.error("{}Naming Exception:", logPrefix, ex);
            return Health.down().withDetail("NamingException", ex.getMessage()).build();
        }
        finally {
            adConn.returnContext(ctx);
        }
        long finishMillis =clock.millis();
        long timeTaken = finishMillis - startMillis;
        
        return Health.up().withDetail("Namespace", name).withDetail("TimeTaken", timeTaken).build();

    }

}
