package com.rms.service;

import com.rms.dtos.OwnerDashboardResponseDTO;

public interface OwnerDashboardService {
    OwnerDashboardResponseDTO getDashboard(String ownerEmail);
}