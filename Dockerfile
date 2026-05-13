# syntax=docker/dockerfile:1.7
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=gradle_properties,target=/home/gradle/.gradle/gradle.properties,required=false \
    --mount=type=secret,id=GPR_USER,required=false \
    --mount=type=secret,id=GPR_TOKEN,required=false \
    sh -c '\
      if [ -f /run/secrets/GPR_USER ] && [ -f /run/secrets/GPR_TOKEN ]; then \
        export GPR_USER="$(cat /run/secrets/GPR_USER)"; \
        export GPR_TOKEN="$(cat /run/secrets/GPR_TOKEN)"; \
      fi; \
      gradle bootJar --no-daemon \
    '

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
