package com.example.Spring_Security.service;

import com.example.Spring_Security.model.Role;
import com.example.Spring_Security.model.User;
import com.example.Spring_Security.repository.RoleRepository;
import com.example.Spring_Security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("rawPassword");
        testUser.setPhone("12345");
        testUser.setRoles(new HashSet<>());
    }

    @Test
    void saveUser_ShouldEncodePasswordAndAddUserRole() {

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.saveUser(testUser);

        verify(passwordEncoder).encode("rawPassword");
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(testUser);

        assertEquals("encodedPassword", testUser.getPassword());
        assertTrue(testUser.getRoles().contains(userRole));
        assertEquals(savedUser, testUser);
    }

    @Test
    void saveUser_ShouldThrowException_WhenRoleNotFound() {

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.saveUser(testUser));

        assertEquals("Role not found", exception.getMessage());
        verify(passwordEncoder).encode("rawPassword");
        verify(roleRepository).findByName("USER");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        testUser.setRoles(roles);
        testUser.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER")));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent@example.com"));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }
}