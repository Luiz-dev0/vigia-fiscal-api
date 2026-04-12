package br.com.nfemonitor.api.domain.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private String plan = "TRIAL";

    @Column(name = "plan_status", nullable = false)
    @Builder.Default
    private String planStatus = "ACTIVE";

    private String whatsapp;

    @Column(name = "trial_ends_at", nullable = false)
    private LocalDateTime trialEndsAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isTrialValido() {
        return trialEndsAt.isAfter(LocalDateTime.now());
    }

    public boolean isAtivo() {
        return Boolean.TRUE.equals(active) &&
                ("ACTIVE".equals(planStatus) || ("TRIAL".equals(plan) && isTrialValido()));
    }
}