package com.journeyplus.iam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByActiveFalse();
    List<User> findByRole(Role role);

    List<User> findByEmailAndNameContainingIgnoreCaseAndRoleAndGrade_IdAndActive(String email, String name, Role role, String gradeId, Boolean active);

    /**
     * Null-safe user search. Any null filter is ignored (matches all).
     * The derived query above generates {@code column = null} predicates for null
     * arguments, which match no rows in SQL; this in-memory filter fixes that so
     * unfiltered calls (e.g. the admin directory listing) return every user.
     */
    default List<User> searchUsers(String email, String name, Role role, String gradeId, Boolean active) {
        return findAll().stream()
                .filter(u -> email == null || email.equalsIgnoreCase(u.getEmail()))
                .filter(u -> name == null || (u.getName() != null && u.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(u -> role == null || role == u.getRole())
                .filter(u -> gradeId == null || (u.getGrade() != null && gradeId.equals(u.getGrade().getId())))
                .filter(u -> active == null || active == u.isActive())
                .collect(java.util.stream.Collectors.toList());
    }
}





