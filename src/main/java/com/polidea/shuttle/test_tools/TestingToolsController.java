package com.polidea.shuttle.test_tools;

import com.polidea.shuttle.test_tools.input.NewAccessTokenRequest;
import com.polidea.shuttle.test_tools.input.UserCreationRequest;
import com.polidea.shuttle.test_tools.input.UserDeletionRequest;
import com.polidea.shuttle.test_tools.input.VerificationCodeRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@Profile("testing")
@RestController
@RequestMapping("testing")
public class TestingToolsController {

    private final TestingToolsService testingTools;

    public TestingToolsController(TestingToolsService testingTools) {
        this.testingTools = testingTools;
    }

    @PostMapping("/users")
    @ResponseStatus(NO_CONTENT)
    public void createUser(@RequestBody UserCreationRequest request) {
        testingTools.createUser(request);
    }

    @PostMapping("/users/admins")
    @ResponseStatus(NO_CONTENT)
    public void createAdmin(@RequestBody UserCreationRequest request) {
        testingTools.createAdmin(request);
    }

    @DeleteMapping("/users")
    @ResponseStatus(NO_CONTENT)
    public void deleteUser(@RequestBody UserDeletionRequest request) {
        testingTools.deleteUser(request);
    }

    @PutMapping("/users/access-tokens")
    @ResponseStatus(NO_CONTENT)
    public void setAccessToken(@RequestBody NewAccessTokenRequest request) {
        testingTools.setNewAccessToken(request);
    }

    @PutMapping("users/verification-codes")
    @ResponseStatus(NO_CONTENT)
    public void setVerificationCode(@RequestBody VerificationCodeRequest request) {
        testingTools.setVerificationCode(request);
    }
}
