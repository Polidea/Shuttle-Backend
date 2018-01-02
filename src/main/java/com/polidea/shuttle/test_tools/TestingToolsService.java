package com.polidea.shuttle.test_tools;

import com.polidea.shuttle.data.DataLoadHelper;
import com.polidea.shuttle.test_tools.input.NewAccessTokenRequest;
import com.polidea.shuttle.test_tools.input.UserCreationRequest;
import com.polidea.shuttle.test_tools.input.UserDeletionRequest;
import com.polidea.shuttle.test_tools.input.VerificationCodeRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.polidea.shuttle.domain.user.permissions.PermissionType.ADMIN;
import static com.polidea.shuttle.domain.user.access_token.TokenType.CLIENT;

@Profile("testing")
@Service
public class TestingToolsService {

    private final DataLoadHelper dataLoadHelper;

    public TestingToolsService(DataLoadHelper dataLoadHelper) {
        this.dataLoadHelper = dataLoadHelper;
    }

    void createUser(UserCreationRequest request) {
        dataLoadHelper.createUserIfMissing(request.email, request.name);
    }

    void createAdmin(UserCreationRequest request) {
        dataLoadHelper.createUserIfMissing(request.email, request.name);
        dataLoadHelper.setGlobalPermissions(request.email, ADMIN);
    }

    public void deleteUser(UserDeletionRequest request) {
        dataLoadHelper.deleteUser(request.email);
    }

    void setNewAccessToken(NewAccessTokenRequest request) {
        dataLoadHelper.createOrRenewAccessToken(request.email, CLIENT, request.accessToken, request.deviceId);
    }

    void setVerificationCode(VerificationCodeRequest request) {
        dataLoadHelper.setVerificationCode(request.deviceId, request.email, request.verificationCode);
    }
}
