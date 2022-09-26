/*
 *   NumberRoutingManagement - NumberPrefix.java
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
import com.slinkytoybox.numberroutingmanagement.database.types.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.*;
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
@ToString(exclude = {"e164numbers"})
@Table(name = "prefix")
@DynamicUpdate
public class NumberPrefix implements Serializable, AuditableObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private @Setter(AccessLevel.PROTECTED)
    Long prefixId;

    private String prefix;
    private Integer stateid;
    private Integer allocationOrder;
    private Boolean allowAllocation;
    private Boolean preferredForRooms;
    private Boolean preferredForResources;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "siteId", nullable = false)
    @Fetch(FetchMode.JOIN)
    private Site site;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "defaultPlatformId", nullable = false)
    @Fetch(FetchMode.JOIN)
    private Platform defaultPlatform;

    @OneToMany(targetEntity = E164Number.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "prefix")
    private Set<E164Number> e164numbers;

    public AUState getState() {
        return AUState.getType(this.stateid);
    }

    public void setState(AUState auState) {

        if (auState == null) {
            this.stateid = null;
        }
        else {
            this.stateid = auState.getId();
        }
    }

    public void addE164Number(E164Number e164Number) {
        if (e164numbers == null) {
            e164numbers = new HashSet<>();
        }
        e164numbers.add(e164Number);
        e164Number.setPrefix(this);

    }

    public void removeE164Number(E164Number e164Number) {
        if (e164numbers == null) {
            return;
        }
        e164numbers.remove(e164Number);
        e164Number.setPrefix(null);
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
        NumberPrefix other = (NumberPrefix) obj;
        return prefixId != null && prefixId.equals(other.getPrefixId());
    }

    @Override
    public String getAuditString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=");
        sb.append(prefixId == null ? "null" : prefixId);
        sb.append("|prefix=");
        sb.append(prefix == null ? "null" : prefix);
        sb.append("|siteid=");
        sb.append((site == null || site.getSiteId() == null) ? "null" : site.getSiteId());
        sb.append("|defaultplatformid=");
        sb.append((defaultPlatform == null || defaultPlatform.getPlatformId() == null) ? "null" : defaultPlatform.getPlatformId());
        sb.append("|allocationorder=");
        sb.append(allocationOrder == null ? "null" : allocationOrder);
        sb.append("|allowAllocation=");
        sb.append(allowAllocation == null ? "null" : allowAllocation);
        sb.append("|state=");
        sb.append(stateid == null ? "null" : getState());
        sb.append("|preferredforrooms=");
        sb.append(preferredForRooms == null ? "null" : preferredForRooms);
        sb.append("|preferredforresources=");
        sb.append(preferredForResources == null ? "null" : preferredForResources);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return 5;
    }
}
