FROM gradle:latest as build

WORKDIR /app
COPY . /app
RUN gradle --refresh-dependencies clean build

FROM openjdk:17

COPY --from=build /app/build/libs/*.jar /app.jar
CMD ["java", "-jar", "/app.jar"]