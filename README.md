# SimpleWMS

Система управления складом: приёмка, адресное хранение, перемещение, резервирование заказов по FEFO,
комплектация, отгрузка, отчёты, REST API для ERP. Дипломный проект.

Java 17 · Spring Boot 3.4 · Spring Security · Spring Data JPA · Thymeleaf · PostgreSQL 16 · Docker Compose ·
JUnit 5 / Testcontainers.

## Быстрый старт (Docker)

```powershell
copy .env.example .env      # задать WMS_API_KEY; APP_PROFILES=prod,demo — с демо-данными
docker compose --profile app up -d --build
```

Откройте http://localhost:8080, вход `admin` / `admin` (в демо-режиме также `manager/manager`, `storekeeper/storekeeper`).

## Разработка

```powershell
docker compose up -d        # только PostgreSQL
.\gradlew.bat bootRun       # приложение, профиль dev
.\gradlew.bat test          # тесты (интеграционные — при запущенном Docker)
```

Документация — [`Project_Docs/`](Project_Docs/README.md); журнал работ и соглашения — [`CLAUDE.md`](CLAUDE.md).
