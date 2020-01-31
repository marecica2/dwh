FROM openjdk:8-jre-alpine
ENV profile=default
COPY target/dependency-jars /run/dependency-jars
ADD target/application.jar /run/application.jar
EXPOSE 3001
ENTRYPOINT ["java", "-Dspring.profiles.active=${profile}", "-jar", "run/application.jar"]