package com.polidea.shuttle.domain.user.access_token;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.database.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static javax.persistence.FetchType.EAGER;

@Entity(name = "access_tokens")
public class AccessToken extends BaseEntity {

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @SuppressWarnings("unused")
    @Column(name = "value", columnDefinition = "text", length = 2048)
    @NotBlank
    private String value;

    @Column(name = "type")
    @NotNull
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "creation_timestamp")
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @NotNull
    private LocalDateTime creationTimestamp;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public AccessToken() {
    }

    public AccessToken(User owner, String deviceId, String value, TokenType type, Instant creationTimestamp) {
        this.owner = owner;
        this.deviceId = deviceId;
        this.value = value;
        this.type = type;
        this.creationTimestamp = LocalDateTime.ofInstant(creationTimestamp, UTC.normalized());
    }

    public boolean isOfType(TokenType typeToCheck) {
        return type == typeToCheck;
    }

    public User tokenOwner() {
        return owner;
    }

    public Optional<String> deviceId() {
        return Optional.ofNullable(deviceId);
    }

    public Instant creationTimestamp() {
        return creationTimestamp.toInstant(UTC);
    }
}
