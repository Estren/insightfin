FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew classes --no-daemon
EXPOSE 8080
ENTRYPOINT ["./gradlew", "bootRun", "--no-daemon"]
