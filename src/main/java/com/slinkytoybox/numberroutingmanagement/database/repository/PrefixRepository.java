/*
 *   NumberRoutingManagement - PrefixRepository.Java
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

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import com.slinkytoybox.numberroutingmanagement.database.model.*;
import com.slinkytoybox.numberroutingmanagement.database.types.AUState;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
public interface PrefixRepository extends JpaRepository<NumberPrefix, Long>, QuerydslPredicateExecutor<NumberPrefix>, QuerydslBinderCustomizer<QNumberPrefix> {

    @Query("SELECT PRF FROM NumberPrefix PRF WHERE PRF.site=:site AND PRF.allowAllocation = TRUE AND PRF.preferredForRooms = TRUE ORDER BY PRF.allocationOrder")
    Set<NumberPrefix> getPreferredRoomPrefixesForSite(Site site);

    @Query("SELECT PRF FROM NumberPrefix PRF WHERE PRF.site=site AND PRF.allowAllocation = TRUE AND PRF.preferredForResources = TRUE ORDER BY PRF.allocationOrder")
    Set<NumberPrefix> getPreferredResourcePrefixesForSite(Site site);
    
    @Query("SELECT PRF FROM NumberPrefix PRF WHERE PRF.site=site AND PRF.allowAllocation = TRUE ORDER BY PRF.allocationOrder")
    Set<NumberPrefix> getAvailablePrefixesForSite(Site site);

    @Query("SELECT PRF FROM NumberPrefix PRF WHERE PRF.stateid=state AND PRF.allowAllocation = TRUE ORDER BY PRF.allocationOrder")
    Set<NumberPrefix> getAvailablePrefixesForState(AUState state);


    @Override
    public default void customize(QuerydslBindings bindings, QNumberPrefix t) {
        bindings.bind(String.class).first(new SingleValueBinding<StringPath, String>() {
            @Override
            public Predicate bind(StringPath path, String s) {
                return path.equalsIgnoreCase(s);
            }
        });
    }

}
