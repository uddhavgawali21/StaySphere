package com.rms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Append-only trail of sensitive/admin actions (logins, admin password resets,
// account status changes). Never write a raw password or full token into
// this table — only who did what, to which record, and when.
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "actor_email", nullable = false, length = 150)
    private String actorEmail;

    @Column(name = "actor_role", length = 20)
    private String actorRole;

    // e.g. LOGIN_SUCCESS, LOGIN_FAILED, USER_STATUS_UPDATED, PASSWORD_RESET_BY_ADMIN
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    // e.g. "USER", target_id = the affected user's id. Null for actions with no single target.
    @Column(name = "target_type", length = 30)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "details", length = 255)
    private String details;
}