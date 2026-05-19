# Project Rename: nfe-monitor-api → vigia-fiscal-api

## Summary of Changes

Successfully renamed the Spring Boot project from **nfe-monitor-api** to **vigia-fiscal-api**. All references have been updated across the entire codebase.

### 1. Package Structure ✓
- **Old**: `br.com.nfemonitor.api`
- **New**: `br.com.vigiafiscal.api`
- **Location**: `/src/main/java/br/com/vigiafiscal/api/` and `/src/test/java/br/com/vigiafiscal/api/`
- All subdirectories maintained (application, config, domain, infrastructure, security, rest, etc.)

### 2. Main Application Class ✓
- **Old File**: `NfeMonitorApiApplication.java`
- **New File**: `VigiaFiscalApiApplication.java`
- **Class Name**: Updated from `NfeMonitorApiApplication` to `VigiaFiscalApiApplication`
- **Location**: `/src/main/java/br/com/vigiafiscal/api/VigiaFiscalApiApplication.java`

### 3. Test Application Class ✓
- **Old File**: `NfeMonitorApiApplicationTests.java`
- **New File**: `VigiaFiscalApiApplicationTests.java`
- **Class Name**: Updated from `NfeMonitorApiApplicationTests` to `VigiaFiscalApiApplicationTests`
- **Location**: `/src/test/java/br/com/vigiafiscal/api/VigiaFiscalApiApplicationTests.java`

### 4. Scheduled Job Class ✓
- **Old File**: `NfeMonitorJob.java`
- **New File**: `NfeVigiaJob.java`
- **Class Name**: Updated from `NfeMonitorJob` to `NfeVigiaJob`
- **Log Messages**: All `[NfeMonitorJob]` prefixes updated to `[NfeVigiaJob]`
- **Updated In**: 
  - `/src/main/java/br/com/vigiafiscal/api/infrastructure/sefaz/NfeVigiaJob.java`
  - `/src/main/java/br/com/vigiafiscal/api/application/rest/AdminController.java`
  - `/src/main/java/br/com/vigiafiscal/api/rest/AdminController.java`

### 5. pom.xml ✓
- `<groupId>`: Changed from `br.com.nfemonitor` to `br.com.vigiafiscal`
- `<artifactId>`: Changed from `api` to `vigia-fiscal-api`
- `<name>`: Changed from `nfe-monitor-api` to `vigia-fiscal-api`
- `<description>`: Changed from `NF-e Monitor — API Backend` to `Vigia Fiscal — API Backend`

### 6. application.yml ✓
- `spring.application.name`: Set to `vigia-fiscal-api`
- `spring.datasource.url`: Changed from `jdbc:postgresql://localhost:5432/nfemonitor` to `jdbc:postgresql://localhost:5432/vigiafiscal`
- `spring.datasource.username`: Changed from `nfemonitor` to `vigiafiscal`
- `notification.email.remetente`: Changed from `nfemonitor@seudominio.com` to `vigiafiscal@seudominio.com`
- `notification.whatsapp.instance-name`: Changed from `NFeMonitor` to `VigiaFiscal`
- `springdoc.packages-to-scan`: Changed from `br.com.nfemonitor` to `br.com.vigiafiscal`

### 7. application.yml.example ✓
- Database name: Changed from `nfemonitor` to `vigiafiscal`
- Database user: Changed from `nfemonitor` to `vigiafiscal` (in comments)
- Email sender: Changed from `nfemonitor@seudominio.com` to `vigiafiscal@seudominio.com`
- Instance name: Changed from `NFeMonitor` to `VigiaFiscal`

### 8. application-prod.yml ✓
- Already had correct references to `vigiafiscal`

### 9. application-test.properties ✓
- Test database name: Changed from `nfemonitor_test` to `vigiafiscal_test`
- JWT secret: Changed from `chave-secreta-de-teste-nfe-monitor-123456789012345` to `chave-secreta-de-teste-vigia-fiscal-123456789012345`

### 10. docker-compose.yml ✓
- Postgres container_name: Changed from `nfe-monitor-db` to `vigia-fiscal-db`
- POSTGRES_DB: Changed from `nfemonitor` to `vigiafiscal`
- POSTGRES_USER: Changed from `nfemonitor` to `vigiafiscal`
- Evolution API key: Changed from `nfemonitor-evolution-key-local` to `vigiafiscal-evolution-key-local`
- Evolution API connection URI: Updated user credentials to use `vigiafiscal`

### 11. Dockerfile ✓
- No hardcoded references to old project name (uses generic patterns)

### 12. .github/workflows/deploy.yml ✓
- Docker image tag: Changed from `ghcr.io/luiz-dev0/nfe-monitor-api:latest` to `ghcr.io/luiz-dev0/vigia-fiscal-api:latest`

### 13. SwaggerConfig.java ✓
- API title: Changed from `NF-e Monitor API` to `Vigia Fiscal API`
- Contact name: Changed from `NF-e Monitor` to `Vigia Fiscal`
- Contact email: Changed from `suporte@nfemonitor.com.br` to `suporte@vigiafiscal.com.br`

### 14. Test Integration Files ✓
- Test email addresses: Changed from `integracao@nfemonitor.com.br` to `integracao@vigiafiscal.com.br`

### 15. Import Statements ✓
- All Java import statements automatically updated with new package structure
- All references to `br.com.nfemonitor.api.*` changed to `br.com.vigiafiscal.api.*`

### 16. Directory Cleanup ✓
- Old directory structure `/src/main/java/br/com/nfemonitor/` removed
- Old directory structure `/src/test/java/br/com/nfemonitor/` removed

## Verification

✓ No remaining references to:
  - `nfemonitor` (lowercase)
  - `nfe-monitor` (with hyphen)
  - `NfeMonitor` (old class name)
  - `br.com.nfemonitor` (old package)

✓ All files with new naming convention verified:
  - `VigiaFiscalApiApplication.java`
  - `VigiaFiscalApiApplicationTests.java`
  - `NfeVigiaJob.java`
  - Package: `br.com.vigiafiscal.api`

✓ Configuration files updated:
  - pom.xml
  - application.yml
  - application.yml.example
  - application-prod.yml
  - application-test.properties
  - docker-compose.yml
  - Dockerfile
  - .github/workflows/deploy.yml

## Next Steps

1. **Build & Test**: Run `mvn clean package` to compile and verify
2. **Docker**: Build Docker image with `docker build -t vigia-fiscal-api:latest .`
3. **Database**: Create new PostgreSQL database named `vigiafiscal` with user `vigiafiscal`
4. **Git**: Commit changes with message: "Rename project: nfe-monitor-api → vigia-fiscal-api"
5. **Deploy**: Update deployment configurations if needed

---

**Completed**: 2026-05-18
**Total Files Modified**: 40+
**Renaming Status**: ✅ COMPLETE
