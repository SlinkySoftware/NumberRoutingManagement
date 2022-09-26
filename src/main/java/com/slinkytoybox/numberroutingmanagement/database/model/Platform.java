/*
 *   NumberRoutingManagement - Platform.java
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
import java.io.Serializable;
import java.util.HashSet;
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
@Table(name = "platform")
@ToString(exclude = {"prefixes", "e164numbers"})
@DynamicUpdate
public class Platform implements Serializable, AuditableObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private @Setter(AccessLevel.PROTECTED)
    Long platformId;

    private String name;
    private String ipGroup;
    private Boolean defaultUserPlatform;
    private Boolean allowRooms;
    private Boolean allowResources;
    private Boolean powershellEnabled;

    @OneToMany(targetEntity = NumberPrefix.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "defaultPlatform")
    private Set<NumberPrefix> prefixes;

    @OneToMany(targetEntity = E164Number.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "platform")
    private Set<E164Number> e164numbers;

    public void addE164Number(E164Number e164Number) {
        if (e164numbers == null) {
            e164numbers = new HashSet<>();
        }
        e164numbers.add(e164Number);
        e164Number.setPlatform(this);

    }

    public void removeE164Number(E164Number e164Number) {
        if (e164numbers == null) {
            return;
        }
        e164numbers.remove(e164Number);
        e164Number.setPlatform(null);
    }

    public void addPrefix(NumberPrefix prefix) {
        if (prefixes == null) {
            prefixes = new HashSet<>();
        }
        prefixes.add(prefix);
        prefix.setDefaultPlatform(this);

    }

    public void removePrefix(NumberPrefix prefix) {
        if (prefixes == null) {
            return;
        }
        prefixes.remove(prefix);
        prefix.setDefaultPlatform(null);
    }

    @Override
    public String getAuditString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=");
        sb.append(platformId == null ? "null" : platformId);
        sb.append("|name=");
        sb.append(name == null ? "null" : name);
        sb.append("|ipgroup=");
        sb.append(ipGroup == null ? "null" : ipGroup);
        sb.append("|defaultuserplatform=");
        sb.append(defaultUserPlatform == null ? "null" : defaultUserPlatform);
        sb.append("|allowrooms=");
        sb.append(allowRooms == null ? "null" : allowRooms);
        sb.append("|allowresources=");
        sb.append(allowResources == null ? "null" : allowResources);
        sb.append("|powershellenabled=");
        sb.append(powershellEnabled == null ? "null" : powershellEnabled);

        return sb.toString();
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
        Platform other = (Platform) obj;
        return platformId != null && platformId.equals(other.getPlatformId());
    }

    @Override
    public int hashCode() {
        return 2;
    }
}
