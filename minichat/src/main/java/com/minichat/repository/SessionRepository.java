package com.minichat.repository;

import com.minichat.entity.Session;
import com.minichat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByRefreshToken(String refreshToken);

    List<Session> findByUserId(Long userId);

    List<Session> findByUserIdAndActiveTrue(Long userId);

    @Query("SELECT s FROM Session s WHERE s.user = :user AND s.active = true AND s.expiresAt > :now")
    List<Session> findActiveSessionsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Session s WHERE s.refreshToken = :refreshToken AND s.active = true AND s.expiresAt > :now")
    Optional<Session> findValidSession(@Param("refreshToken") String refreshToken, @Param("now") LocalDateTime now);

    void deleteByUserIdAndActiveFalse(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    int countByUserIdAndActiveTrue(Long userId);
}
