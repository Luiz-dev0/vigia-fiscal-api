# =============================================================
# Stage 1 — Build
# =============================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copia apenas o pom.xml primeiro para cachear dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests -B

# =============================================================
# Stage 2 — Runtime
# =============================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Usuário não-root por segurança
RUN addgroup --system spring && adduser --system --ingroup spring spring

# Copia o JAR gerado no stage anterior
COPY --from=build /app/target/*.jar app.jar

# Diretório para logs persistidos via volume
RUN mkdir -p /app/logs && chown spring:spring /app/logs

USER spring

EXPOSE 8080

# JVM tunada para container: limita heap, habilita G1GC, logs em UTF-8
ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Dfile.encoding=UTF-8", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]