# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy just the pom first, so Docker can cache the downloaded
# dependencies as their own "layer" -- re-used on every rebuild
# unless pom.xml itself changes, instead of re-downloading on every code edit
COPY pom.xml .
RUN mvn dependency:go-offline

# Now copy the actual source and build the jar
COPY src ./src
RUN mvn package -DskipTests

# ---- Stage 2: run ----
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
