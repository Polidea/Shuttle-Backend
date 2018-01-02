package com.polidea.shuttle.domain.notifications;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.database.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "push_tokens")
public class PushToken extends BaseEntity {

    @ManyToOne(fetch = LAZY,
               cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
               optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @SuppressWarnings("unused")
    private User owner;

    @NotBlank
    @SuppressWarnings("unused")
    private String deviceId;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @NotBlank
    private String value;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public PushToken() {
    }

    PushToken(User owner, Platform platform, String deviceId, String value) {
        this.owner = owner;
        this.platform = platform;
        this.deviceId = deviceId;
        this.value = value;
    }

    boolean isFor(Platform platformToCheck) {
        return platform.equals(platformToCheck);
    }

    public String value() {
        return value;
    }
}

