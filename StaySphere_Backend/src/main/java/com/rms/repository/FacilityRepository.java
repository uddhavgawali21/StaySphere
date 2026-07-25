package com.rms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rms.entity.Facility;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    List<Facility> findAllByProperty_PropertyId(Long propertyId);
}