package com.journeyplus.iam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT u FROM User u WHERE " +
           "(:email IS NULL OR u.email = :email) AND " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:gradeId IS NULL OR u.grade.id = :gradeId) AND " +
           "(:active IS NULL OR u.active = :active)")
    List<User> searchUsers(
            @Param("email") String email,
            @Param("name") String name,
            @Param("role") Role role,
            @Param("gradeId") String gradeId,
            @Param("active") Boolean active
    );
}
