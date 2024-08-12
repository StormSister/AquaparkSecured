package com.example.aquaparksecured.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<AppUser,Long> {

    Optional<AppUser> findByEmail(String email);


    AppUser findByUsername(String username);




    @Query("SELECT u FROM AppUser u WHERE " +
            "(:email IS NULL OR u.email LIKE %:email%) AND " +
            "(:username IS NULL OR u.username LIKE %:username%) AND " +
            "(:firstName IS NULL OR u.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR u.lastName LIKE %:lastName%) AND " +
            "(:phoneNumber IS NULL OR u.phoneNumber LIKE %:phoneNumber%) AND " +
            "(:role IS NULL OR u.role = :role)")
    List<AppUser> searchUsers(@Param("email") String email,
                              @Param("username") String username,
                              @Param("firstName") String firstName,
                              @Param("lastName") String lastName,
                              @Param("phoneNumber") String phoneNumber,
                              @Param("role") String role);


}

