# SimpleWMS: образ приложения (см. Project_Docs/Развёртывание.md).
# Сборка: docker compose --profile app build  (или docker build -t simplewms .)

# ---------- 1. Сборка jar ----------
# Gradle из образа, а не gradlew: дистрибутив Gradle не скачивается при каждой сборке
FROM gradle:8.12.1-jdk17 AS build
WORKDIR /src
# Сначала только файлы сборки — слой с зависимостями кэшируется, пока они не меняются
COPY settings.gradle build.gradle ./
RUN gradle dependencies --no-daemon --quiet > /dev/null || true
COPY src ./src
# Тесты запускаются отдельно (gradlew test), в образ не входят
RUN gradle bootJar --no-daemon -x test

# ---------- 2. Запуск ----------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S wms && adduser -S -G wms wms
WORKDIR /app
COPY --from=build /src/build/libs/SimpleWMS.jar app.jar
# Скрипты начальных данных: приложение выполняет их при старте (spring.sql.init, пути file:SQL/migrations/...)
COPY SQL/migrations ./SQL/migrations
USER wms

# LANG — кодировка вывода JVM: без неё кириллица в docker logs превращается в «???»
ENV SPRING_PROFILES_ACTIVE=prod \
    TZ=Europe/Moscow \
    LANG=C.UTF-8 \
    JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
    CMD wget -q -O /dev/null http://localhost:8080/login || exit 1

ENTRYPOINT ["sh", "-c", "exec java -Dfile.encoding=UTF-8 $JAVA_OPTS -jar app.jar"]
