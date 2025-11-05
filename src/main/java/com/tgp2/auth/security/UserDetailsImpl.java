package com.tgp2.auth.security;

import com.tgp2.auth.entity.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String username; // optional
    private String email;
    private String password;
    private String role;
    private User user; // reference to actual User entity

    public static UserDetailsImpl build(User user) {
        UserDetailsImpl u = new UserDetailsImpl();
        u.setId(user.getId());
        u.setUsername(user.getUsername());
        u.setEmail(user.getEmail());
        u.setPassword(user.getPassword());
        u.setRole(user.getRole());
        u.setUser(user);
        return u;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // no roles implemented currently — return empty list.
        return Collections.emptyList();
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() {
        // We treat email as the principal for authentication flows; but keep username field available.
        return email;
    }
    public String getRole() { return role; }
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
