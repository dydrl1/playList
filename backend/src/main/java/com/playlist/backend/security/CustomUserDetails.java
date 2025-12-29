package com.playlist.backend.security;

import com.playlist.backend.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public String getName() {
        return user.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 나중에 role 컬럼 있으면 여기서 변환
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // User 엔티티에 password 필드 있다고 가정
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 로그인 기준을 email로 사용한다고 가정
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
