package com.chatapp.userservice.repository;

import com.chatapp.userservice.model.RefreshToken;
import com.chatapp.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity operations.
 * Provides data access methods for refresh token management.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its token value.
     *
     * @param token the token string to search for
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes refresh tokens associated with a specific user.
     *
     * @param user the user whose tokens should be deleted
     */
    void deleteByUser(User user);

    /**
     * Deletes all expired refresh tokens.
     *
     * @param now the current date and time for comparison
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

    /**
     * Deletes all refresh tokens for a specific user.
     *
     * @param user the user whose tokens should be deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteAllByUser(User user);
}