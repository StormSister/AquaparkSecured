package com.example.aquaparksecured.user;


import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {


    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;



    public AppUser saveUser(AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }



    public Optional<AppUser> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }



    public List<AppUser> findAllUsers() {
        return (List<AppUser>) userRepository.findAll();
    }

    public List<AppUser> searchUsers(String email, String username, String firstName, String lastName, String phoneNumber, String role) {
        System.out.println("Searching with parameters: "
                + "email=" + email + ", username=" + username
                + ", firstName=" + firstName + ", lastName=" + lastName
                + ", phoneNumber=" + phoneNumber + ", role=" + role);

        List<AppUser> results = userRepository.searchUsers(email, username, firstName, lastName, phoneNumber, role);
        System.out.println("results: " + results);

        return results;
    }

    public AppUser updateUser(Long userId, AppUser updatedUser) {
        Optional<AppUser> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            AppUser existingUser = optionalUser.get();

            if (isPasswordChangeAllowed(existingUser, updatedUser) && updatedUser.getPassword() != null) {
                updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            } else {
                updatedUser.setPassword(existingUser.getPassword());
            }

            if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
                existingUser.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
                existingUser.setUsername(updatedUser.getUsername());
            }
            if (updatedUser.getFirstName() != null && !updatedUser.getFirstName().isEmpty()) {
                existingUser.setFirstName(updatedUser.getFirstName());
            }
            if (updatedUser.getLastName() != null && !updatedUser.getLastName().isEmpty()) {
                existingUser.setLastName(updatedUser.getLastName());
            }
            if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().isEmpty()) {
                existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            }
            if (updatedUser.getRole() != null && !updatedUser.getRole().isEmpty()) {
                existingUser.setRole(updatedUser.getRole());
            }

            return userRepository.save(existingUser);
        } else {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
    }
    public boolean isPasswordChangeAllowed(AppUser updatedUser, AppUser existingUser) {
        return (updatedUser.getRole().equals("client") || updatedUser.getRole().equals("worker"))
                && updatedUser.getId().equals(existingUser.getId());
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<AppUser> getUserById(Long id) {
        return userRepository.findById(id);}}