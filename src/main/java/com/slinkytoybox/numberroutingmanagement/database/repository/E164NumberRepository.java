/*
 *   NumberRoutingManagement - E164NumberRepository.Java
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

import org.springframework.data.jpa.repository.JpaRepository;
import com.slinkytoybox.numberroutingmanagement.database.model.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
public interface E164NumberRepository extends JpaRepository<E164Number, Long>  {

        public Set<E164Number> findByPrefix(NumberPrefix prf);
       
        @Query("SELECT E.status, COUNT(E.status) FROM E164Number E WHERE E.prefix = prf GROUP BY E.status")
        public Map<Long, Long> countByPrefixAndStatus(NumberPrefix prf);
       
        @Query("SELECT E FROM E164Number E JOIN NumberPrefix P ON E.prefix = P WHERE E.status = :statusId AND P.allowAllocation = :allowAlloc AND P.site = :site AND E.platform = :plat AND P.defaultPlatform = :plat AND E.lastAllocationTime <= :beforeDate ORDER by P.allocationOrder, E.lastAllocationTime")
        public List<E164Number> getNumbersByPrefixPlatform(Pageable pageable, Site site, Platform plat, Integer statusId, Boolean allowAlloc, OffsetDateTime beforeDate);

        @Query("SELECT E FROM E164Number E JOIN NumberPrefix P ON E.prefix = P WHERE E.status = :statusId AND P.allowAllocation = :allowAlloc AND P.site = :site AND E.platform = :plat AND E.lastAllocationTime <= :beforeDate ORDER by P.allocationOrder, E.lastAllocationTime")
        public List<E164Number> getNumbersByE164Platform(Pageable pageable, Site site, Platform plat, Integer statusId, Boolean allowAlloc, OffsetDateTime beforeDate);

        @Query("SELECT E FROM E164Number E WHERE E.status = :statusId AND E.prefix = :prefix AND E.platform = :plat ORDER by E.lastAllocationTime")
        public List<E164Number> getNumbersByPrefixAndE164Platform(Pageable pageable, NumberPrefix prefix, Platform plat, Integer statusId);
        
        public Optional<E164Number> findByE164 (String e164);
}
