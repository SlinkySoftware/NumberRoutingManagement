/*
 *   number-routing-management - MasterDBRepository.java
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
package com.slinkytoybox.numberroutingmanagement.database.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Component
public class MasterDBRepository {

    @Autowired
    public AllocatedObjectRepository allocateObjectRepository;

    @Autowired
    public E164NumberRepository e164NumberRepository;

    @Autowired
    public PlatformRepository platformRepository;

    @Autowired
    public PrefixRepository prefixRepository;

    @Autowired
    public SiteRepository siteRepository;

    @Autowired
    public AllocationAuditRepository allocationAuditRepository;

    @Autowired
    public AuditRepository auditRepository;

}
