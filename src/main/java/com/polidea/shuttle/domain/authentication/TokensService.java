package com.polidea.shuttle.domain.authentication;

import com.polidea.shuttle.domain.notifications.PushTokenService;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.authentication.output.AuthTokensResponse;
import com.polidea.shuttle.domain.user.refresh_token.RefreshToken;
import com.polidea.shuttle.domain.user.refresh_token.RefreshTokenInvalidException;
import com.polidea.shuttle.domain.user.refresh_token.RefreshTokenService;
import com.polidea.shuttle.domain.user.access_token.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.Optional;

import static com.polidea.shuttle.domain.user.access_token.TokenType.CLIENT;

@Service
@Transactional
public class TokensService {

    private final AccessTokenService accessTokenService;

    private final RefreshTokenService refreshTokenService;

    private final PushTokenService pushTokenService;

    @Autowired
    public TokensService(AccessTokenService accessTokenService,
                         RefreshTokenService refreshTokenService, PushTokenService pushTokenService) {
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.pushTokenService = pushTokenService;
    }

    public AuthTokensResponse generateTokens(User user) {
        return generateTokens(user, null);
    }

    public AuthTokensResponse generateTokens(User user, String deviceId) {
        String accessToken = accessTokenService.saveAccessToken(user, deviceId, CLIENT);
        refreshTokenService.deleteBy(user, deviceId);
        String refreshToken = refreshTokenService.saveRefreshToken(user, deviceId);
        return new AuthTokensResponse(accessToken, refreshToken);
    }

    public AuthTokensResponse refreshTokens(String refreshTokenValue) {
        Optional<RefreshToken> token = refreshTokenService.findRefreshTokenMatching(refreshTokenValue);

        return token.map((refreshToken) -> {
            String newAccessToken = accessTokenService.saveAccessToken(refreshToken.owner(),
                                                                       refreshToken.deviceId(),
                                                                       CLIENT);
            refreshTokenService.deleteBy(refreshToken.owner(), refreshToken.deviceId());
            String newRefreshToken = refreshTokenService.saveRefreshToken(refreshToken.owner(), refreshToken.deviceId());
            return new AuthTokensResponse(newAccessToken, newRefreshToken);

        }).orElseThrow(() -> new RefreshTokenInvalidException());
    }

    public void removeTokens(String deviceId, String accessToken) {
        if (deviceId != null) {
            pushTokenService.removePushTokensOfDevice(deviceId);
        }
        accessTokenService.removeToken(accessToken);
    }
}
