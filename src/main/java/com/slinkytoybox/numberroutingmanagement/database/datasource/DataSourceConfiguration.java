/*
 *   NumberRoutingManagement - DataSourceConfiguration.java
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
package com.slinkytoybox.numberroutingmanagement.database.datasource;

import com.slinkytoybox.numberroutingmanagement.utils.AESEncryption;
import com.slinkytoybox.numberroutingmanagement.database.model.*;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.slinkytoybox.numberroutingmanagement.database.repository", entityManagerFactoryRef = "numberEntityManagerFactory", transactionManagerRef = "numberTransactionManager")
@Slf4j
public class DataSourceConfiguration {
   
    @Autowired
    private Environment env;
    
    @Bean
    @ConfigurationProperties("number.datasource")
    @Primary
    public DataSourceProperties numberDataSourceProperties() {
        final String logPrefix = "numberDataSourceProperties() - ";
        log.info("{}Setting up new Data Source Properties", logPrefix);

        DataSourceProperties prop = new DataSourceProperties();
        String encryptedPassword = env.getProperty("number.datasource.encryptedPassword");
        String encryptionKey = env.getProperty("system.secure.hash");
        String initValue = env.getProperty("system.secure.initial");
        log.info("{}Set JDBC Url: {}", logPrefix, prop.getUrl());
        if (encryptedPassword != null && !encryptedPassword.isEmpty()) {
            log.debug("{}Using encrypted password: {}", logPrefix, encryptedPassword);
            String password = AESEncryption.decrypt(encryptedPassword, encryptionKey, initValue);
            prop.setPassword(password);
        }
        return prop;
    }

    @Bean
    @ConfigurationProperties("number.datasource")
    public DataSource numberDataSource() {
        return numberDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "numberEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean numberEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(numberDataSource())
                .packages(AllocatedObject.class)
                .packages(E164Number.class)
                .packages(Platform.class)
                .packages(NumberPrefix.class)
                .packages(Site.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager numberTransactionManager(final @Qualifier("numberEntityManagerFactory") @Nullable LocalContainerEntityManagerFactoryBean numberEntityManagerFactory) {
        if (numberEntityManagerFactory == null) {
            return null;
        }
        else {
            return new JpaTransactionManager(numberEntityManagerFactory.getObject());
        }
    }
}
