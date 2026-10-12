# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Full JDK + Maven needed only at build time.
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Copy the Maven wrapper and descriptor first so Docker can cache the
# dependency-download layer independently of source changes.
# If the project has no mvnw, fall back to the system Maven installed below.
COPY pom.xml ./

# Download dependencies (offline-friendly layer cache).
# We use the Maven binary that ships in the image; install it if absent.
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B --no-transfer-progress

# Now copy source and build.  Skip tests — they require a live DB.
COPY src ./src

RUN mvn package -B --no-transfer-progress -DskipTests

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
# Minimal JRE — no compiler, no Maven, no source.
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security hardening: run as a non-root user.
RUN addgroup -S bites && adduser -S bites -G bites
USER bites

WORKDIR /app

# Pull the fat JAR from the build stage.
COPY --from=build /workspace/target/bites-backend-*.jar app.jar

# ── Runtime configuration ─────────────────────────────────────────────────────
# All secrets are injected at deploy time via environment variables.
# See .env.example for the full list of required variables.
#
# Defaults that are safe to set at image level (non-sensitive, easily overridden):
ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080

# Expose the port the embedded Tomcat listens on.
# The PORT env var in application.yml drives this; keep it in sync.
EXPOSE ${PORT}

# ── Start ──────────────────────────────────────────────────────────────────────
# Use the exec form so the JVM receives SIGTERM directly (graceful shutdown).
# JVM flags:
#   -XX:+UseContainerSupport          honour cgroup memory/CPU limits
#   -XX:MaxRAMPercentage=75.0         cap heap at 75% of container RAM
#   -Djava.security.egd=...           faster SecureRandom (avoids /dev/random blocking)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
