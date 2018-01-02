package com.polidea.shuttle.domain.notifications;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface PushTokenJpaRepository extends JpaRepository<PushToken, Integer> {

    @Query("   SELECT pt FROM push_tokens pt JOIN pt.owner o " +
               "WHERE o.id = :ownerId")
    List<PushToken> findByOwnerId(@Param("ownerId") Integer ownerId);

    void deleteByDeviceId(String deviceId);

}
