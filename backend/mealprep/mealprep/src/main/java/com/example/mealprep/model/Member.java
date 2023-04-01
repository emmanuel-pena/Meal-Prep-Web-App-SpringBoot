package com.example.mealprep.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class Member implements UserDetails {

    private String id;
    private String email;
    private String username;
    private String passwordHash;
    private boolean isActivated;
    private String accountType;

    public Member() {
    }

     public Member(String email, String username,
                   String passwordHash, boolean isActivated,
                   String accountType)
     {
         this.email = email;
         this.username = username;
         this.passwordHash = passwordHash;
         this.isActivated = isActivated;
         this.accountType = accountType;
     }

    public String getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
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

    public boolean isActivated() {
        return isActivated;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
