FROM openjdk:8-jdk
LABEL maintainer="YATRAONLINE\udadevi.srivarshini"
COPY target/Student_HallTickets-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]