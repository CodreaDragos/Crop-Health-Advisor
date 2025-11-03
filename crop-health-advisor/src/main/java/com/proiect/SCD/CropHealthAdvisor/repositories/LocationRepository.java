package com.proiect.SCD.CropHealthAdvisor.repositories;

import com.proiect.SCD.CropHealthAdvisor.models.Location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>{

    List<Location> findByUserId(Long userId);
    Location findByLatitudeAndLongitude(Double latitude, Double longitude);

}
