package com.proiect.SCD.CropHealthAdvisor.repositories;

import com.proiect.SCD.CropHealthAdvisor.models.Reports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Reports, Long> {
    List<Reports> findByLocationId(Long locationId);
}