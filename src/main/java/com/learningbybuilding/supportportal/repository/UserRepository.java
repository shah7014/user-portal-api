package com.learningbybuilding.supportportal.repository;

import com.learningbybuilding.supportportal.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);

    User findByEmail(String email);
}
