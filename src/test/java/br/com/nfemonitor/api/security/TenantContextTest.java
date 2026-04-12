package br.com.nfemonitor.api.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TenantContext — isolamento multi-tenant via ThreadLocal")
class TenantContextTest {

    @AfterEach
    void limpar() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve armazenar e recuperar o tenantId corretamente")
    void deveArmazenarERecuperarTenantId() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.set(tenantId);
        assertThat(TenantContext.get()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("Deve retornar null após clear()")
    void deveRetornarNullAposClear() {
        TenantContext.set(UUID.randomUUID());
        TenantContext.clear();
        assertThat(TenantContext.get()).isNull();
    }

    @Test
    @DisplayName("Não deve vazar dados entre threads — isolamento ThreadLocal")
    void naoDeveVazarDadosEntreThreads() throws InterruptedException {
        UUID tenantA = UUID.randomUUID();
        TenantContext.set(tenantA);

        AtomicReference<UUID> capturadoPelaThreadB = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread threadB = new Thread(() -> {
            capturadoPelaThreadB.set(TenantContext.get());
            latch.countDown();
        });

        threadB.start();
        latch.await();

        assertThat(TenantContext.get()).isEqualTo(tenantA);
        assertThat(capturadoPelaThreadB.get()).isNull();
    }

    @Test
    @DisplayName("Threads diferentes devem ter tenants independentes")
    void threadsDiferentesDevemTerTenantsIndependentes() throws InterruptedException {
        UUID tenantX = UUID.randomUUID();
        UUID tenantY = UUID.randomUUID();

        AtomicReference<UUID> resultadoA = new AtomicReference<>();
        AtomicReference<UUID> resultadoB = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);

        Thread threadA = new Thread(() -> {
            TenantContext.set(tenantX);
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            resultadoA.set(TenantContext.get());
            TenantContext.clear();
            latch.countDown();
        });

        Thread threadB = new Thread(() -> {
            TenantContext.set(tenantY);
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            resultadoB.set(TenantContext.get());
            TenantContext.clear();
            latch.countDown();
        });

        threadA.start();
        threadB.start();
        latch.await();

        assertThat(resultadoA.get()).isEqualTo(tenantX);
        assertThat(resultadoB.get()).isEqualTo(tenantY);
    }
}