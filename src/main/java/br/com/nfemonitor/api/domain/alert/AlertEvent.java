package br.com.nfemonitor.api.domain.alert;

import br.com.nfemonitor.api.domain.nfe.NotaFiscal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_events")
@Getter
@Setter
public class AlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private AlertRule alertRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_fiscal_id")
    private NotaFiscal notaFiscal;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private AlertEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AlertEventStatus status;

    @Column(name = "created_at")
    private LocalDateTime criadoEm;

    @Column(name = "sent_at")
    private LocalDateTime enviadoEm;

    @Column(name = "error_msg", length = 1000)
    private String errorMsg;
}