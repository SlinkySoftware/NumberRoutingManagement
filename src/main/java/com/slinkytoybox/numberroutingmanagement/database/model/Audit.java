/*
 *   number-routing-management - Audit.java
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
package com.slinkytoybox.numberroutingmanagement.database.model;

import com.slinkytoybox.numberroutingmanagement.database.AuditableObject;
import java.io.Serializable;
import java.time.OffsetDateTime;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.*;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "audit")
@DynamicUpdate
@ToString
public class Audit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private @Setter(AccessLevel.PROTECTED)
    Long auditId;

    private String username;
    private OffsetDateTime actionDate;
    private String tableName;
    private String oldData;
    private String newData;

    public static Audit createAuditRecord(String username, AuditableObject oldObj, AuditableObject newObj) {
        return new Audit()
                .setActionDate(OffsetDateTime.now())
                .setUsername(username)
                .setTableName(oldObj.getTableName())
                .setOldData(oldObj.getAuditString())
                .setNewData(newObj.getAuditString())
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Audit other = (Audit) obj;
        return auditId != null && auditId.equals(other.getAuditId());
    }

    @Override
    public int hashCode() {
        return 6;
    }
}
