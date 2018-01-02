package com.polidea.shuttle.domain.verification_code;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.database.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "verification_codes")
public class VerificationCode extends BaseEntity {

    @NotBlank
    private String deviceId;

    @NotBlank
    private String encodedValue;

    @ManyToOne(fetch = LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    private VerificationCode() {
    }

    public VerificationCode(String deviceId, User user, String encodedValue) {
        this.deviceId = deviceId;
        this.user = user;
        this.encodedValue = encodedValue;
    }

    public String deviceId() {
        return deviceId;
    }

    public String encodedValue() {
        return encodedValue;
    }

    public void setEncodedValue(String encodedValue) {
        this.encodedValue = encodedValue;
    }

    public User user() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
