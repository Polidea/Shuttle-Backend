package com.polidea.shuttle.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
interface UserJpaRepository extends JpaRepository<User, Integer> {

    List<User> findByEmailIgnoreCase(String email);

    Set<User> findByIsDeletedAndIsVisibleForModerator(boolean isDeleted, boolean isVisibleForModerator);

}
