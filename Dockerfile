# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copiar primero los archivos de configuración de Gradle para aprovechar la caché
# de capas Docker: si solo cambia el código fuente, no se re-descargan dependencias
COPY gradle       gradle
COPY gradlew      .
COPY settings.gradle .
COPY build.gradle .

RUN chmod +x gradlew

# Descargar dependencias (capa cacheada independiente del código)
RUN ./gradlew dependencies --no-daemon --quiet

# Copiar el código fuente y generar la distribución
COPY src src
RUN ./gradlew installDist --no-daemon

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copiar solo la distribución generada (bin/ + lib/)
COPY --from=build /app/build/install/javalin-graphql-demo .

EXPOSE 7000

# El script generado por Gradle ya incluye los --add-opens necesarios
# definidos en applicationDefaultJvmArgs del build.gradle
ENTRYPOINT ["bin/javalin-graphql-demo"]
