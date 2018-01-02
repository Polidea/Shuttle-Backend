package com.polidea.shuttle.domain.user.refresh_token;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.database.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;
import static javax.persistence.FetchType.EAGER;

@Entity(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(columnDefinition = "text", length = 2048)
    @NotBlank
    @SuppressWarnings("unused")
    private String value;

    private String deviceId;

    @Convert(converter = LocalDateTimeConverter.class)
    @NotNull
    @SuppressWarnings("unused")
    private LocalDateTime creationTimestamp;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public RefreshToken() {
    }

    public RefreshToken(User owner, String deviceId, String value, Instant creationTimestamp) {
        this.owner = owner;
        this.deviceId = deviceId;
        this.value = value;
        this.creationTimestamp = LocalDateTime.ofInstant(creationTimestamp, UTC.normalized());
    }

    public User owner() {
        return owner;
    }

    public String deviceId() {
        return deviceId;
    }

}
