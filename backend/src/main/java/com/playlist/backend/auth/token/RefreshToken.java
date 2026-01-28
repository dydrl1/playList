package com.playlist.backend.auth.token;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token",
        indexes = {
                @Index(name="idx_refresh_token_user", columnList="user_id"),
                @Index(name="idx_refresh_token_hash", columnList="token_hash", unique = true)
        })
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="token_hash", nullable=false, length=64, unique=true)
    private String tokenHash;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(name="revoked", nullable=false)
    private boolean revoked;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="last_used_at")
    private LocalDateTime lastUsedAt;

    protected RefreshToken() {}

    public static RefreshToken issue(Long userId, String tokenHash, LocalDateTime expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.userId = userId;
        rt.tokenHash = tokenHash;
        rt.expiresAt = expiresAt;
        rt.revoked = false;
        rt.createdAt = LocalDateTime.now();
        return rt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void markUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
