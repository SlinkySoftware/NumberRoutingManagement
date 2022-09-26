/*
 *   number-routing-management - AllocationAudit.java
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
package com.slinkytoybox.numberroutingmanagement.database.model;

import com.slinkytoybox.numberroutingmanagement.database.types.NumberType;
import java.io.Serializable;
import java.time.OffsetDateTime;
import javax.persistence.*;
import javax.persistence.CascadeType;
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
@Table(name = "allocationaudit")
@DynamicUpdate
@ToString
public class AllocationAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private @Setter(AccessLevel.PROTECTED)
    Long allocationAuditId;

    private Integer allocationType;
    private OffsetDateTime allocationDate;
    private OffsetDateTime deAllocationDate;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "e164numberid", nullable = false)
    @Fetch(FetchMode.JOIN)
    private E164Number e164Number;
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "allocatedobjectid", nullable = false)
    @Fetch(FetchMode.JOIN)
    private AllocatedObject allocatedObject;

    public NumberType getAllocationType() {
        return NumberType.getType(this.allocationType);
    }

    public AllocationAudit setAllocationType(NumberType allocationType) {

        if (allocationType == null) {
            this.allocationType = null;
        }
        else {
            this.allocationType = allocationType.getId();
        }
        return this;
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
        AllocationAudit other = (AllocationAudit) obj;
        return allocationAuditId != null && allocationAuditId.equals(other.getAllocationAuditId());
    }

    @Override
    public int hashCode() {
        return 7;
    }
}
