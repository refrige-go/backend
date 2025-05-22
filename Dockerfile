FROM eclipse-temurin:17
RUN mkdir -p /opt/app
COPY build/libs/refrige-go-backend-0.0.1-SNAPSHOT.jar /opt/app/app.jar
WORKDIR /opt/app
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080