package br.com.nfemonitor.api.domain.sefaz;

import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sefaz_consultas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SefazConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cnpj_id", nullable = false)
    private Cnpj cnpj;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "status_http")
    private Integer statusHttp;

    @Column(name = "sucesso", nullable = false)
    private boolean sucesso;

    @Column(name = "mensagem", columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "notas_encontradas")
    private Integer notasEncontradas;

    @Column(name = "iniciado_em", nullable = false, updatable = false)
    private LocalDateTime iniciadoEm;

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    @PrePersist
    void prePersist() {
        iniciadoEm = LocalDateTime.now();
    }
}