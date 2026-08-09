package com.rms.entity;

import com.rms.enums.OccupancyType;
import com.rms.enums.PropertyStatus;
import com.rms.enums.PropertyType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "properties")
public class Property extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Long propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 30)
    private PropertyType propertyType;

    @Column(name = "rent_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy_type", nullable = false, length = 20)
    private OccupancyType occupancyType;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(name = "area", nullable = false, length = 100)
    private String area;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_status", nullable = false, length = 20)
    private PropertyStatus propertyStatus;

    // NEW — how many independent bookable rooms this property has.
    // Default 1 preserves existing behaviour for ROOM/FLAT.
    // Set > 1 for PG/HOSTEL so multiple tenants can book simultaneously.
    @Column(name = "total_rooms", nullable = false)
    private Integer totalRooms = 1;
}