package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.AuditLogResponseDTO;
import com.rms.entity.AuditLog;
import com.rms.repository.AuditLogRepository;
import com.rms.service.AuditLogService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void record(String actorEmail, String actorRole, String action, String targetType, Long targetId, String details) {
        try {
            AuditLog entry = new AuditLog();
            entry.setActorEmail(actorEmail);
            entry.setActorRole(actorRole);
            entry.setAction(action);
            entry.setTargetType(targetType);
            entry.setTargetId(targetId);
            entry.setDetails(details);
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            // An audit-log write must never break the real operation it's
            // recording (e.g. a login or a status update) — log and move on.
            log.error("Failed to write audit log entry for action {}: {}", action, ex.getMessage(), ex);
        }
    }

    @Override
    public Page<AuditLogResponseDTO> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapToResponseDTO);
    }

    private AuditLogResponseDTO mapToResponseDTO(AuditLog log) {
        return AuditLogResponseDTO.builder()
                .auditId(log.getAuditId())
                .actorEmail(log.getActorEmail())
                .actorRole(log.getActorRole())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}