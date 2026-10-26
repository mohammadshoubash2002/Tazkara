# Stage 1: Build JAR
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build

# Copy maven wrapper and pom.xml first for layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
