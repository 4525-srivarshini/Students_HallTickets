FROM openjdk:8-jdk-alpine
LABEL authors="YATRAONLINE\udadevi.srivarshini"
ARG JAR_FILE=/target/*.jar
COPY ./target/Student_HallTickets-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]