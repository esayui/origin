FROM openjdk:8-alpine
ADD ./operations-management-suite-v3-1.5.0-RELEASE.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=docker"]