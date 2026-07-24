package com.rms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rms.entity.PropertyImage;

import java.util.List;
import java.util.Optional;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    List<PropertyImage> findAllByProperty_PropertyId(Long propertyId);

    Optional<PropertyImage> findByProperty_PropertyIdAndPrimaryTrue(Long propertyId);
}