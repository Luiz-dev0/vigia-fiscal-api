package br.com.nfemonitor.api.domain.manifestacao;

import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.nfe.NotaFiscal;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "manifestacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManifestacaoDestinatario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nota_fiscal_id", nullable = false)
    private NotaFiscal notaFiscal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cnpj_id", nullable = false)
    private Cnpj cnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 20)
    private TipoManifestacao tipoEvento;

    @Column(name = "codigo_evento", nullable = false, length = 6)
    private String codigoEvento;

    @Column(name = "protocolo", length = 50)
    private String protocolo;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "c_stat", length = 10)
    private String cStat;

    @Column(name = "x_motivo", columnDefinition = "TEXT")
    private String xMotivo;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.enviadoEm == null) {
            this.enviadoEm = LocalDateTime.now();
        }
    }
}