package com.college.bustracking.repository;

import com.college.bustracking.entity.StudentStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentStopRepository extends JpaRepository<StudentStop, Long> {
    Optional<StudentStop> findByStudentIdAndBusId(Long studentId, Long busId);
    List<StudentStop> findByStudentId(Long studentId);
    void deleteByStopId(Long stopId);
}
