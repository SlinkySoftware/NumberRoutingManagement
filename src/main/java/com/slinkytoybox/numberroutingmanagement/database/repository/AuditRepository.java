/*
 *   number-routing-management - AuditRepository.java
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

import com.slinkytoybox.numberroutingmanagement.database.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
public interface AuditRepository extends JpaRepository<Audit, Long> {
    
    
    
}
