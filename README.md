# java-chat

Консольный многопользовательский чат на сокетах · *Multi-user console chat over TCP sockets*

`Java` `TCP sockets` `Multithreading`

## Что сделано
- Клиент-серверная архитектура на `ServerSocket` и `Socket`, каждый клиент обслуживается в отдельном потоке
- Собственный текстовый протокол: команды `REG`, `AUTH`, `JOIN`, `SWITCH`, `MSG`, `OPEN_PRIVATE`, `PRIVATE`, `HISTORY`, `LOGOUT`, ответы `OK` и `ERR`
- Регистрация и вход по логину и паролю, у каждого пользователя уникальный ник
- Каналы `#general`, `#random`, `#help`
- Личные сообщения. Если получатель не в сети, сообщение сохраняется и приходит ему при следующем входе
- История: при входе в канал или личный чат показываются сообщения за последние 3 дня
- Список пользователей онлайн при входе
- Потокобезопасность: `ConcurrentHashMap` для пользователей онлайн, `ReentrantReadWriteLock` для файла пользователей
- Хранение в файлах: `users.txt` для аккаунтов, `logs/channels`, `logs/private` и `logs/offline` для сообщений

## Структура
```
src/main/java/chatapp/
├── common/
│   └── Protocol.java           # команды, коды ответов, разделитель
├── server/
│   ├── ChatServer.java         # точка входа, принимает подключения на порту 12345
│   ├── ClientHandler.java      # поток одного клиента: разбор и обработка команд
│   ├── UserManager.java        # регистрация и аутентификация, users.txt
│   ├── ChannelManager.java     # рассылка сообщений канала
│   └── HistoryManager.java     # логи каналов и личных чатов, офлайн-сообщения
└── client/
    ├── ChatClient.java         # подключение к серверу (ChatClient2/3 — копии для запуска нескольких клиентов)
    ├── ClientMenu.java         # консольное меню: вход/регистрация, выбор канала, личный чат
    └── IncomingHandler.java    # поток, который читает и выводит входящие сообщения
```

## Запуск
1. Запусти `chatapp.server.ChatServer`.
2. Запусти один или несколько клиентов: `ChatClient`, `ChatClient2`, `ChatClient3`.
3. В меню клиента зарегистрируйся, войди и выбери канал или личный чат.

---

## English

A multi-user console chat over TCP sockets.

### Features
- Client–server architecture on `ServerSocket` / `Socket`; each client is served by its own thread
- Custom text protocol: `REG`, `AUTH`, `JOIN`, `SWITCH`, `MSG`, `OPEN_PRIVATE`, `PRIVATE`, `HISTORY`, `LOGOUT` commands with `OK` / `ERR` responses
- Sign-up and login with a unique nickname per user
- Channels `#general`, `#random`, `#help`
- Private messages; messages to offline users are stored and delivered on their next login
- History: joining a channel or a private chat shows messages from the last 3 days
- Online user list on login
- Thread safety: `ConcurrentHashMap` for online users, `ReentrantReadWriteLock` for the user file
- File storage: `users.txt` for accounts; `logs/channels`, `logs/private` and `logs/offline` for messages

### Structure
```
src/main/java/chatapp/
├── common/Protocol.java        # commands, response codes, separator
├── server/                     # ChatServer (port 12345), ClientHandler, UserManager, ChannelManager, HistoryManager
└── client/                     # ChatClient (+ copies 2/3 for multiple clients), ClientMenu, IncomingHandler
```

### Run
1. Start `chatapp.server.ChatServer`.
2. Start one or more clients: `ChatClient`, `ChatClient2`, `ChatClient3`.
3. Register, log in, then pick a channel or a private chat from the client menu.
