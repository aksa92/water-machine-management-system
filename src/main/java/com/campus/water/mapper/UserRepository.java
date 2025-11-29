package com.campus.water.mapper;

import com.campus.water.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByStudentId(String studentId);
    List<User> findByStudentNameContaining(String studentName);
    List<User> findByStatus(User.UserStatus status);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.studentName LIKE %?1% AND u.status = ?2")
    List<User> findByStudentNameContainingAndStatus(String studentName, User.UserStatus status);

    boolean existsByStudentId(String studentId);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}