FROM eclipse-temurin:21-jdk

WORKDIR /app

# Gradle関連
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# ソース
COPY src src

# ビルド
RUN ./gradlew bootJar --no-daemon

EXPOSE 8080

CMD ["java", "-jar", "build/libs/backend-0.0.1-SNAPSHOT.jar"]