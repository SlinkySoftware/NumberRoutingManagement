/*
 *   NumberRoutingManagement - NumberAllocatorApplication.java
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
package com.slinkytoybox.numberroutingmanagement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
@Slf4j
public class NumberAllocatorApplication {

    private static String consoleLogo;
    private final Object logoLock = new Object();

    public static void main(String[] args) {
        final String logPrefix = "main() - ";
        log.trace("{}Entering method", logPrefix);
        log.info("{}Booting SpringBoot application", logPrefix);
        SpringApplication.run(NumberAllocatorApplication.class, args);
        log.info("{}Running on JVM: {} {} version {}", logPrefix, System.getProperty("java.vm.vendor"), System.getProperty("java.vm.name"), System.getProperty("java.version"));
        log.info(consoleLogo);
        log.trace("{}Leaving method", logPrefix);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        log.info("Reading GIT Properties: {}", propsConfig);
        return propsConfig;
    }

    @Value("${console.logo}")
    public void setLogo(String consoleLogo) {
        synchronized (logoLock) {
            NumberAllocatorApplication.consoleLogo = consoleLogo;
        }

    }
}
