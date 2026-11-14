# Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests

# Execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S portal && adduser -S portal -G portal
COPY --from=build /app/target/portal-*.jar app.jar
USER portal
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
