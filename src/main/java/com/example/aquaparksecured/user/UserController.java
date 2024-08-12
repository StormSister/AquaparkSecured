package com.example.aquaparksecured.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;



@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class UserController {


    private final UserService userService;




    @RequestMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        try {
            System.out.println("Received request to fetch all users");
            List<AppUser> users = userService.findAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.out.println("Exception occurred while fetching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/users/search")
    public ResponseEntity<?> getUserByEmail(@RequestParam(required = false) String email) {
        System.out.println("Received request to fetch user by email: " + email);

        Optional<AppUser> user = userService.findUserByEmail(email);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<AppUser>> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String role) {

        System.out.println("Received request to search users with parameters: "
                + "email=" + email + ", username=" + username + ", firstName=" + firstName
                + ", lastName=" + lastName + ", phoneNumber=" + phoneNumber
                + ", role=" + role);

        List<AppUser> users = userService.searchUsers(email, username, firstName, lastName, phoneNumber, role);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody AppUser updatedUser) {
        System.out.println("Received request to update user with ID: " + userId);

        try {
            AppUser updatedUserFromService = userService.updateUser(userId, updatedUser);
            System.out.println("User with ID " + userId + " updated successfully");
            return ResponseEntity.ok(updatedUserFromService);
        } catch (IllegalArgumentException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user");
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        System.out.println("Received request to delete user with ID: " + id);

        userService.deleteUser(id);
        System.out.println("User with ID " + id + " deleted successfully");
        return ResponseEntity.noContent().build();
    }

    public boolean isPasswordChangeAllowed(AppUser updatedUser, Optional<AppUser> existingUserOptional) {
        AppUser existingUser = existingUserOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));

        return (updatedUser.getRole().equals("client") || updatedUser.getRole().equals("worker"))
                && updatedUser.getId().equals(existingUser.getId());
    }
}