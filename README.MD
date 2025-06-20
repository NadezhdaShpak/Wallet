# Wallet Service API
Это простой RESTful-сервис для управления балансами кошельков с помощью операций пополнения и снятия средств.
****

## 📌 Особенности реализации

- Депозит в кошелек
- Вывод из кошелька
- Получение баланса кошелька
- PostgreSQL
- Миграции Liquibase
- Контейниризация с docker compose
- Поддержка высокой степени параллелизма (оптимистическая блокировка)
- Правильная обработка ошибок и проверка

## Endpoints

| Method | URL                        | Description                  |
|--------|----------------------------|------------------------------|
| POST   | `/api/v1/wallet`           | Deposit or withdraw funds    |
| GET    | `/api/v1/wallets/{uuid}`   | Get wallet balance           |

## Запуск проекта
1. Docker 20.10+ и Docker Compose
```bash
   git clone https://github.com/NadezhdaShpak/Wallet
   cd Wallet
```
2. Запусти команду:

```bash
    docker compose up --build
```
3. Для проверки работоспособности, в БД добавлен тестовый кошелек
проверить работу можно с помощью команд:
```bash
    GET http://localhost:8080/api/v1/wallets/2f3a52ff-3969-476f-a25f-d99bc3718f73
```
```bash
    POST http://localhost:8080/api/v1/wallet
Content-Type: application/json
{
  "walletId": "2f3a52ff-3969-476f-a25f-d99bc3718f73",
  "operationType": "DEPOSIT",
  "amount": 1000
}
```

Остановка приложения
```bash
    docker compose down
```