/*
 *   number-routing-management - E164Number.java
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

import com.slinkytoybox.numberroutingmanagement.database.AuditableObject;
import com.slinkytoybox.numberroutingmanagement.database.types.NumberStatus;
import com.slinkytoybox.numberroutingmanagement.database.types.NumberType;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import javax.persistence.*;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "e164number")
@DynamicUpdate
@ToString(exclude = {""})

public class E164Number implements Serializable, AuditableObject, Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private @Setter(AccessLevel.PROTECTED)
    Long e164NumberId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "prefixId", nullable = false)
    @Fetch(FetchMode.JOIN)
    private NumberPrefix prefix;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "platformId", nullable = false)
    @Fetch(FetchMode.JOIN)
    private Platform platform;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "allocatedObjectId", nullable = true)
    @Fetch(FetchMode.JOIN)
    private AllocatedObject allocatedObject;

    private Integer status;
    private Integer allocationType;
    private OffsetDateTime lastAllocationTime;
    private String e164;
    private String description;

    @Override
    public E164Number clone() {
        E164Number newobj = new E164Number();
        newobj.e164NumberId = this.e164NumberId;
        newobj.prefix = this.prefix;        
        newobj.platform = this.platform;
        newobj.allocatedObject = this.allocatedObject;        
        newobj.status = this.status;
        newobj.allocationType = this.allocationType;
        newobj.lastAllocationTime = this.lastAllocationTime;
        newobj.e164 = this.e164;        
        newobj.description = this.description;
        return newobj;
    }
    
    
    public NumberType getAllocationType() {
        return NumberType.getType(this.allocationType);
    }

    public void setAllocationType(NumberType allocationType) {

        if (allocationType == null) {
            this.allocationType = null;
        }
        else {
            this.allocationType = allocationType.getId();
        }
    }

    public NumberStatus getStatus() {
        return NumberStatus.getType(this.status);
    }

    public void setStatus(NumberStatus status) {

        if (status == null) {
            this.status = null;
        }
        else {
            this.status = status.getId();
        }
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
        E164Number other = (E164Number) obj;
        return e164NumberId != null && e164NumberId.equals(other.getE164NumberId());
    }

    @Override
    public String getAuditString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=");
        sb.append(e164NumberId == null ? "null" : e164NumberId);
        sb.append("|e164=");
        sb.append(e164 == null ? "null" : e164);
        sb.append("|prefixid=");
        sb.append((prefix == null || prefix.getPrefixId() == null) ? "null" : prefix.getPrefixId());
        sb.append("|platformid=");
        sb.append((platform == null || platform.getPlatformId() == null) ? "null" : platform.getPlatformId());
        sb.append("|lastallocationtime=");
        sb.append(lastAllocationTime == null ? "null" : lastAllocationTime.format(DateTimeFormatter.ISO_DATE_TIME));
        sb.append("|allocatedobjectid=");
        sb.append((allocatedObject == null || allocatedObject.getAllocatedObjectId() == null) ? "null" : allocatedObject.getAllocatedObjectId());
        sb.append("|allocationtype=");
        sb.append(allocationType == null ? "null" : getAllocationType());
        sb.append("|status=");
        sb.append(status == null ? "null" : getStatus());
        sb.append("|description=");
        sb.append(description == null ? "null" : description);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return 4;
    }

}
