# Build stage
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
COPY . .

# Build the application
RUN gradle clean build --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Install curl for health check (before switching to non-root user)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN addgroup --system --gid 1001 enfila
RUN adduser --system --uid 1001 enfila

# Change ownership
RUN chown -R enfila:enfila /app
USER enfila

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
