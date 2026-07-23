package com.rms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rms.entity.Property;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findAllByOwner_UserId(Long ownerId);
}