# Этап сборки
FROM openjdk:17-jdk-slim AS build

# Установка Maven
RUN apt-get update && apt-get install -y maven

WORKDIR /app

# Копируем pom.xml и загружаем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходники и компилируем проект
COPY src /app/src
RUN mvn clean package -DskipTests

# Финальный образ
FROM openjdk:17-jdk-slim

WORKDIR /app

# Копируем собранный JAR файл в контейнер
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Экспонируем порт для доступа к приложению
EXPOSE 8080

# Запускаем Spring Boot приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
