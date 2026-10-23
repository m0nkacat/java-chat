# 💭 Java Socket Chat

![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white)
![Sockets](https://img.shields.io/badge/TCP-Sockets-555)
![Multithreading](https://img.shields.io/badge/Multithreading-555)

**RU** | [EN](#english)

Многопользовательский клиент-серверный чат на чистых Java-сокетах с собственным текстовым протоколом.

## Возможности
- 🔐 Регистрация и авторизация пользователей (`REG`, `AUTH`)
- 📢 Каналы: вход, переключение, сообщения в канал (`JOIN`, `SWITCH`, `MSG`)
- 🤫 Личные сообщения (`PRIVATE`, `OPEN_PRIVATE`)
- 🕓 История сообщений канала (`HISTORY`)
- 🧵 Каждый клиент обслуживается в своём потоке, общие данные в `ConcurrentHashMap`

## Архитектура
```
chatapp/
├── common/Protocol.java      # команды и ответы протокола
├── server/                   # ChatServer, ClientHandler, ChannelManager, UserManager, HistoryManager
└── client/                   # ChatClient, ClientMenu, IncomingHandler
```

## Запуск
1. Запусти `chatapp.server.ChatServer` (порт `12345`).
2. Запусти один или несколько клиентов `chatapp.client.ChatClient`.

---

## English

A multi-user client–server chat built on plain Java sockets with a custom text protocol: registration/login, channels, private messages and channel history. Each client runs in its own thread; shared state lives in `ConcurrentHashMap`s.

**Run:** start `chatapp.server.ChatServer` (port `12345`), then one or more `chatapp.client.ChatClient` instances.
