# syntax=docker/dockerfile:1.7
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=gradle_properties,target=/home/gradle/.gradle/gradle.properties \
    gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/src/main/resources/ssl /app/ssl
ENTRYPOINT ["java", "-jar", "app.jar"]
