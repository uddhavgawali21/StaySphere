package com.rms.service;

import com.rms.dtos.AuditLogResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    // actorEmail/actorRole = who performed the action (or the attempted-login email
    // if authentication itself failed). targetType/targetId identify the affected
    // record, if any. Never pass a raw password or token in details.
    void record(String actorEmail, String actorRole, String action, String targetType, Long targetId, String details);

    Page<AuditLogResponseDTO> getAuditLogs(Pageable pageable);
}