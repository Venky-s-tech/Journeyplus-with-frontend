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

    List<User> findByEmailAndNameContainingIgnoreCaseAndRoleAndGrade_IdAndActive(String email, String name, Role role, String gradeId, Boolean active);

    default List<User> searchUsers(String email, String name, Role role, String gradeId, Boolean active) {
        return findByEmailAndNameContainingIgnoreCaseAndRoleAndGrade_IdAndActive(email, name, role, gradeId, active);
    }
}





