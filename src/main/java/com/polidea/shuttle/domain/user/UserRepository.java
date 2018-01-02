package com.polidea.shuttle.domain.user;

import com.polidea.shuttle.domain.notifications.PushTokenRepository;
import com.polidea.shuttle.domain.user.access_token.AccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Repository
public class UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final PushTokenRepository pushTokenRepository;

    @Autowired
    public UserRepository(UserJpaRepository userJpaRepository,
                          AccessTokenRepository accessTokenRepository,
                          PushTokenRepository pushTokenRepository) {
        this.userJpaRepository = userJpaRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.pushTokenRepository = pushTokenRepository;
    }

    User createUser(String email, String name, String avatarHref, boolean isVisibleForModerator) {
        User newUser = new User(email, name);
        newUser.setAvatarHref(avatarHref);
        newUser.setVisibleForModerator(isVisibleForModerator);
        return userJpaRepository.save(newUser);
    }

    public Set<User> allUsers() {
        return userJpaRepository.findAll()
                                .stream()
                                .filter(user -> !user.isDeleted())
                                .collect(toSet());
    }

    public Set<User> allUsersVisibleForModerator() {
        return userJpaRepository.findByIsDeletedAndIsVisibleForModerator(false, true);
    }

    public Optional<User> findUser(String email) {
        return userJpaRepository.findByEmailIgnoreCase(email)
                                .stream()
                                .filter(user -> !user.isDeleted())
                                .findAny();
    }

    void deleteUser(User userToDelete) {
        accessTokenRepository.findAccessTokensOwnedBy(userToDelete)
                             .forEach(accessToken -> removePushTokensOfDevice(accessToken.deviceId()));
        accessTokenRepository.deleteAccessTokensOwnedBy(userToDelete);
        userToDelete.delete();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void removePushTokensOfDevice(Optional<String> deviceId) {
        deviceId.ifPresent(pushTokenRepository::removePushTokensOfDevice);
    }
}
