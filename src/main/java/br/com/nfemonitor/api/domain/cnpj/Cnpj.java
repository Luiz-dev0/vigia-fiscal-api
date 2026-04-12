package br.com.nfemonitor.api.domain.cnpj;

import br.com.nfemonitor.api.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cnpjs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cnpj {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String cnpj;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    private String ie;

    @Column(nullable = false, columnDefinition = "VARCHAR(2)")
    private String uf;

    @Column(name = "email_contato")
    private String emailContato;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "last_consulted_at")
    private LocalDateTime lastConsultedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}