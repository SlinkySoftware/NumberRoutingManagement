/*
 *   NumberRoutingManagement - AllocatedObject.java
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

import java.io.Serializable;
import java.util.Set;
import javax.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "allocatedobject")
@DynamicUpdate
@ToString(exclude = {"e164numbers"})
public class AllocatedObject implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private @Setter(AccessLevel.PROTECTED)
    Long allocatedObjectId;

    private String name;

    @OneToMany(targetEntity = E164Number.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "allocatedObject")
    private Set<E164Number> e164numbers;

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
        AllocatedObject other = (AllocatedObject) obj;
        return allocatedObjectId != null && allocatedObjectId.equals(other.getAllocatedObjectId());
    }

    public String getAuditString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=");
        sb.append(allocatedObjectId == null ? "null" : allocatedObjectId);
        sb.append("|name=");
        sb.append(name == null ? "null" : name);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
