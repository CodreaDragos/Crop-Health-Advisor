package com.proiect.SCD.CropHealthAdvisor.repositories;

import com.proiect.SCD.CropHealthAdvisor.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

}
