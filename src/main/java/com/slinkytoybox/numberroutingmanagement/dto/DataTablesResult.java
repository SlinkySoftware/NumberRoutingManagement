/*
 *   NumberRoutingManagement - DataRablesResult.java
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
package com.slinkytoybox.numberroutingmanagement.dto;

import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Data
@Accessors(chain = true)
public class DataTablesResult implements DTOResult {

    private Integer draw;
    private Long recordsTotal;
    private Long recordsFiltered;
    private String error;
    private Object data;

    
    public Map<String, Object> fetchResponse() {
         Map<String, Object> response = new DTOHashMap<>();
         response.put("draw", draw);
         response.put("recordsTotal", recordsTotal);
         response.put("recordsFiltered", recordsFiltered);
         response.put("data", data);
         if (error != null && !error.isEmpty()) {
             response.put("error", error);
         }
         return response;
    }
}
