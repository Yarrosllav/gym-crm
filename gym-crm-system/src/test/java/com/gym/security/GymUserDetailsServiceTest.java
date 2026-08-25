package com.gym.security;

import com.gym.dao.IUserDao;
import com.gym.model.Role;
import com.gym.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymUserDetailsServiceTest {

    @Mock
    private IUserDao userDao;

    private GymUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new GymUserDetailsService(userDao);
    }

    private User buildUser(boolean active, Role role) {
        var user = new User();
        user.setUsername("John.Smith");
        user.setPassword("$2a$10$encodedHash");
        user.setActive(active);
        user.setRole(role);
        return user;
    }

    @Test
    void loadUserByUsername_shouldMapUserToUserDetails() {
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(buildUser(true, Role.ROLE_TRAINEE)));

        var result = userDetailsService.loadUserByUsername("John.Smith");

        assertEquals("John.Smith", result.getUsername());
        assertEquals("$2a$10$encodedHash", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINEE")));
    }

    @Test
    void loadUserByUsername_shouldMarkDisabled_whenUserInactive() {
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(buildUser(false, Role.ROLE_TRAINER)));

        var result = userDetailsService.loadUserByUsername("John.Smith");

        assertFalse(result.isEnabled());
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        when(userDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("Unknown"));
    }
}
