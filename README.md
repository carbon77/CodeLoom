# 🧵 CodeLoom

> Сервис для решения задач по программированию — своя мини-платформа в духе LeetCode/Codeforces.

[![Kotlin](https://img.shields.io/badge/Kotlin-Backend-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring-Boot-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-Frontend-4FC08D?logo=vue.js&logoColor=white)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-Message%20Broker-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

---

## 📖 О проекте

**CodeLoom** — это сервис, в котором пользователи могут решать задачи по программированию: выбирать задачу по теме и сложности, отправлять решение и сразу получать результат проверки.

Проект состоит из бэкенда на Kotlin/Spring и фронтенда на Vue, с авторизацией через Keycloak и асинхронной обработкой отправленных решений через Kafka.

## ✨ Возможности (CodeLoom v1)

- 👤 Регистрация и вход пользователей
- 🛡 Две роли: **ADMIN** и **USER**
- 🛠 Администратор может создавать, редактировать и удалять задачи, тесты к ним и темы (CRUD)
- 🔍 Пользователи могут искать задачи с фильтрацией по сложности и теме
- 📤 Отправка решений (submissions) и просмотр результата проверки
- 💻 Поддерживаемые языки: **Python, Java, C++**

## 🛠 Технологический стек

**Backend**
- [Kotlin](https://kotlinlang.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- Spring Data JDBC
- Spring Security + OAuth2 Resource Server

**Frontend**
- [TypeScript](https://www.typescriptlang.org/)
- [Vue](https://vuejs.org/) + Vue Router
- [Pinia](https://pinia.vuejs.org/) — управление состоянием
- [Axios](https://axios-http.com/) — HTTP-клиент

**Инфраструктура**
- [Keycloak](https://www.keycloak.org/) — авторизация и аутентификация (OAuth2 / OpenID Connect)
- [PostgreSQL](https://www.postgresql.org/) — база данных
- [Apache Kafka](https://kafka.apache.org/) — брокер сообщений (асинхронная обработка отправленных решений)
- Docker / Docker Compose

## 📂 Структура проекта

```
CodeLoom/
├── backend/              # Kotlin + Spring Boot API
├── frontend/              # Vue + TypeScript SPA
├── init.sql               # Инициализация схемы БД
├── docker-compose.yaml
└── README.md
```

## 🚀 Быстрый старт

Поднимает сразу весь стек: backend, frontend, PostgreSQL, Kafka и Keycloak.

```bash
git clone https://github.com/carbon77/CodeLoom.git
cd CodeLoom

docker compose up --build
```

## 🗺 Roadmap

- [ ] Расширение списка поддерживаемых языков
- [ ] Рейтинги и статистика пользователей
- [ ] Соревновательный режим (контесты)


## 👤 Автор

**carbon77** — [GitHub](https://github.com/carbon77)
