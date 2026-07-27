package com.rms.repository;

import com.rms.entity.Property;
import com.rms.enums.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {

    List<Property> findAllByOwner_UserId(Long ownerId);

    List<Property> findByPropertyStatus(PropertyStatus propertyStatus);
}