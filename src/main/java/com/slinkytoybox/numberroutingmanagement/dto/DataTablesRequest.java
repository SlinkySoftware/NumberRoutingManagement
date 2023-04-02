/*
 *   NumberRoutingManagement - DataTablesRequest.java
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
package com.slinkytoybox.numberroutingmanagement.dto;

import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Data
@Slf4j
public class DataTablesRequest {

    private Integer start;
    private Integer length;
    private DataTablesSearch search;
    private List<DataTablesColumn> columns;
    private Integer draw;
    private List<DataTablesOrder> order;
    private Object filter;
    
    public Sort getSort() {
        final String logPrefix = "getSort() - ";
        log.trace("{}Entering method", logPrefix);
        log.debug("{}Determining sort order", logPrefix);
        Sort sortOrder = null;
        if (order != null && !order.isEmpty()) {
            for (DataTablesOrder dto : order) {
                DataTablesColumn dtc = columns.get(dto.getColumn());
                log.trace("{}Setting sort order {} on column {}", logPrefix, dto.getDir(), dtc.getData());
                if (sortOrder == null) {
                    if (dto.getDir().equalsIgnoreCase("desc")) {
                        sortOrder = Sort.by(dtc.getData()).descending();
                    }
                    else {
                        sortOrder = Sort.by(dtc.getData()).ascending();
                    }
                }
                else {
                    if (dto.getDir().equalsIgnoreCase("desc")) {
                        sortOrder.and(Sort.by(dtc.getData()).descending());
                    }
                    else {
                        sortOrder.and(Sort.by(dtc.getData()).ascending());
                    }
                }
            }
        }
        else {
            log.trace("{}Setting default sort order", logPrefix);
            sortOrder = Sort.by("name").ascending();
        }
        log.trace("{}Sort order: {}", logPrefix, sortOrder);
        return sortOrder;
    }
 
}
