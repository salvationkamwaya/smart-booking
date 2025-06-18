package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Attempting to load user with email: " + email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("User not found with email: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        System.out.println("Found user: " + user.getEmail() + ", Role: " + user.getRole() + ", Active: " + user.isActive());

        if (!user.isActive()) {
            System.out.println("User account is disabled for: " + email);
            throw new UsernameNotFoundException("User account is disabled");
        }

        System.out.println("Successfully loaded user: " + email);
        return new CustomUserPrincipal(user);
    }

    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public String getUsername() {
            return user.getEmail(); // Use email as username
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }        @Override
        public List<GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
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
            return user.isActive();
        }

        public User getUser() {
            return user;
        }
    }
}
