/*
 *   NumberRoutingManagement - Site.java
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
import com.slinkytoybox.numberroutingmanagement.database.types.AUState;
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
@Table(name = "site")
@ToString(exclude = {"prefixes"})
@DynamicUpdate
public class Site implements Serializable, AuditableObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private @Setter(AccessLevel.PROTECTED)
    Long siteId;

    private String name;
    private String adname;
    private String dialplan;

    private Integer stateid;

    @OneToMany(targetEntity = NumberPrefix.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "site")
    private Set<NumberPrefix> prefixes;

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

    public void addPrefix(NumberPrefix prefix) {
        if (prefixes == null) {
            prefixes = new HashSet<>();
        }
        prefixes.add(prefix);

        prefix.setSite(this);

    }

    public void removePrefix(NumberPrefix prefix) {
        if (prefixes == null) {
            return;
        }
        prefixes.remove(prefix);
        prefix.setSite(null);
    }

    @Override
    public String getAuditString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=");
        sb.append(siteId == null ? "null" : siteId);
        sb.append("|name=");
        sb.append(name == null ? "null" : name);
        sb.append("|adname=");
        sb.append(adname == null ? "null" : adname);
        sb.append("|diaplan=");
        sb.append(dialplan == null ? "null" : dialplan);
        sb.append("|state=");
        sb.append(stateid == null ? "null" : getState());

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
        Site other = (Site) obj;
        return siteId != null && siteId.equals(other.getSiteId());
    }

    @Override
    public int hashCode() {
        return 3;
    }
}
