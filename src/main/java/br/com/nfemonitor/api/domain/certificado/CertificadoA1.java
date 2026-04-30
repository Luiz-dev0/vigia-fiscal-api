package br.com.nfemonitor.api.domain.certificado;

import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificados_a1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificadoA1 {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cnpj_id", nullable = false, unique = true)
    private Cnpj cnpj;

    @Column(name = "pfx_data", nullable = false, columnDefinition = "bytea")
    private byte[] pfxData;

    @Column(name = "validade", nullable = false)
    private LocalDateTime validade;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}