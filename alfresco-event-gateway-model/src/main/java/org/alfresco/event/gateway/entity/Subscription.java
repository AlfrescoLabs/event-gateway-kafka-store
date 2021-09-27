/*
 * Copyright 2021-2021 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.event.gateway.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import net.bytebuddy.build.ToStringPlugin.Exclude;
import org.alfresco.event.gateway.ConfigJsonConverter;
import org.hibernate.annotations.GenericGenerator;

/**
 * Model that represents the subscriptions that a client application can request the event gateway to create
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Subscription extends AuditableEntity {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @NotEmpty(message = "Subscription type must not be null or empty")
    private String type;

    @Column(name = "subscription_user")
    private String user;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = ConfigJsonConverter.class)
    private Map<String, String> config;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "subscription_id")
    @Exclude
    private List<@Valid Filter> filters = new ArrayList<>();

    /**
     * No-arg constructor to be used by JPA
     */
    public Subscription() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "Subscription{" +
            "id='" + id + '\'' +
            ", status=" + status +
            ", type='" + type + '\'' +
            ", user='" + user + '\'' +
            ", config=" + config +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Subscription that = (Subscription) o;
        return Objects.equals(getId(), that.getId()) &&
            Objects.equals(getStatus(), that.getStatus()) &&
            Objects.equals(getType(), that.getType()) &&
            Objects.equals(getUser(), that.getUser()) &&
            Objects.equals(getConfig(), that.getConfig());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStatus(), getType(), getUser(), getConfig());
    }
}
