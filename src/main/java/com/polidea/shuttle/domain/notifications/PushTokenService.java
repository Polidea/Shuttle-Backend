package com.polidea.shuttle.domain.notifications;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.input.PushTokenRequest;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class PushTokenService {

    private static final Logger LOGGER = getLogger(PushTokenService.class);

    private final PushTokenRepository pushTokenRepository;
    private final UserService userService;

    @Autowired
    public PushTokenService(PushTokenRepository pushTokenRepository, UserService userService) {
        this.pushTokenRepository = pushTokenRepository;
        this.userService = userService;
    }

    public void addPushToken(String ownerEmail, Platform platform, String deviceId, PushTokenRequest pushTokenRequest) {
        String value = pushTokenRequest.token;
        LOGGER.info("Registering Push Token. Owner: '{}'. Platform: '{}'. Token: '{}'",
                    ownerEmail, platform, pushTokenRequest.token);
        User owner = userService.findUser(ownerEmail);
        removePushTokensOfDevice(deviceId);
        pushTokenRepository.createPushToken(
            owner,
            platform,
            deviceId,
            value
        );
    }

    Set<PushToken> findPushTokensOwnedBy(User user) {
        return pushTokenRepository.findTokensOwnedBy(user);
    }

    public void removePushTokensOfDevice(String deviceId) {
        pushTokenRepository.removePushTokensOfDevice(deviceId);
    }

}

