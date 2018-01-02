package com.polidea.shuttle.domain.notifications;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Repository
public class PushTokenRepository {

    private final PushTokenJpaRepository jpaRepository;

    @Autowired
    PushTokenRepository(PushTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    void createPushToken(User owner, Platform platform, String deviceId, String value) {
        PushToken newPushToken = new PushToken(
            owner,
            platform,
            deviceId,
            value
        );
        jpaRepository.save(newPushToken);
    }

    Set<PushToken> findTokensOwnedBy(User tokenOwner) {
        return jpaRepository.findByOwnerId(tokenOwner.id())
                            .stream()
                            .collect(toSet());
    }

    public void removePushTokensOfDevice(String deviceId) {
        jpaRepository.deleteByDeviceId(deviceId);
        jpaRepository.flush();
    }

}
