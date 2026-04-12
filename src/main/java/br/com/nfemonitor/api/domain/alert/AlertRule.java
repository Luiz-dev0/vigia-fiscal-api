package br.com.nfemonitor.api.domain.alert;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "cnpj_id")
    private UUID cnpjId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AlertEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private AlertChannel channel;

    @Column(name = "destination", nullable = false)
    private String destination; // número WhatsApp ou email

    @Column(name = "minutes_before", nullable = false)
    private Integer minutesBefore;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Alias para compatibilidade com AlertService
    public String getDestinatario() {
        return this.destination;
    }

    public boolean isAtiva() {
        return this.active;
    }
}