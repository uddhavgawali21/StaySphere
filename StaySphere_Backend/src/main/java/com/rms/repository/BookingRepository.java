package com.rms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rms.entity.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByTenant_UserId(Long tenantId);

    List<Booking> findAllByProperty_PropertyId(Long propertyId);
}