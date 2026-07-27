package com.rms.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rms.dtos.OwnerDashboardResponseDTO;
import com.rms.service.OwnerDashboardService;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;

    // No {ownerId} path variable on purpose — the owner can only ever see their own dashboard.
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerDashboardResponseDTO> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(ownerDashboardService.getDashboard(authentication.getName()));
    }
}