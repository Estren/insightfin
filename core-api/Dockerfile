FROM eclipse-temurin:21-jdk
WORKDIR /app

# Install Gradle and generate wrapper
ENV GRADLE_VERSION=8.12
RUN apt-get update && apt-get install -y wget unzip && \
    wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip -q gradle-${GRADLE_VERSION}-bin.zip -d /opt && \
    rm gradle-${GRADLE_VERSION}-bin.zip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
ENV PATH="/opt/gradle-${GRADLE_VERSION}/bin:${PATH}"

COPY . .
RUN gradle wrapper --no-daemon && ./gradlew classes --no-daemon

EXPOSE 8080
ENTRYPOINT ["./gradlew", "bootRun", "--no-daemon"]
