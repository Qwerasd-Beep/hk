package com.security.userdetails;

import com.entity.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class HKUserDetails implements UserDetails {
    private UserInfo user;

    public HKUserDetails(UserInfo user){
        this.user=user;
    }

    public String getUsername() {
        return user.getName();
    }

    public UserInfo getUser() {
        return user;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.authorities();
    }

    public String getPassword() {
        return user.getPassword();
    }

    public int getId() {
        return user.getId();
    }
}
