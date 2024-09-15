FROM openjdk:17-jdk-slim-buster
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Используем образ Maven для сборки
#FROM maven:3.8.4-openjdk-17 AS build
#
## Устанавливаем рабочую директорию
#WORKDIR /app
#
## Копируем файл pom.xml и загружаем зависимости
#COPY pom.xml .
#COPY src ./src
#RUN mvn clean package
#
## Используем образ OpenJDK для выполнения
#FROM openjdk:17-jdk-slim-buster
#
## Копируем JAR-файл из предыдущего этапа
#COPY --from=build /app/target/SearchEngine-1.0-SNAPSHOT.jar app.jar
#
## Указываем команду для запуска приложения
#ENTRYPOINT ["java", "-jar", "/app.jar"]
