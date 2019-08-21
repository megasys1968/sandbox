FROM openjdk:11
ADD backend/target/backend-0.0.1-SNAPSHOT.jar /opt/backend.jar
ADD frontend/dist/frontend /opt/frontend
WORKDIR /opt
CMD ["java", "-jar", "-Dspring.profiles.active=production", "backend.jar"]