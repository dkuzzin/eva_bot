# Развёртывание EVA на чистом сервере

Архитектура production:

```text
Internet
   ↓
Cloudflare
   ↓
Nginx :80/:443
   ├── eva.chernushka.fun         → 127.0.0.1:8082 → Mini App
   ├── api.chernushka.fun         → 127.0.0.1:8080 → Java Backend
   └── bot.chernushka.fun/webhook → 127.0.0.1:8081 → Go Bot

Docker Compose
   ├── miniapp
   ├── server
   ├── bot
   └── PostgreSQL
```

## 1. Настроить DNS

В Cloudflare(или любом другом днс хостинге) создать или изменить A-записи:

```text
eva.chernushka.fun → SERVER_IP
api.chernushka.fun → SERVER_IP
bot.chernushka.fun → SERVER_IP
```

На сервере должны быть доступны извне TCP-порты:

```text
22
80
443
```

## 2. Установить необходимые пакеты

На Ubuntu/Debian:

```bash
sudo apt update
sudo apt install -y \
    git \
    nginx \
    certbot \
    docker.io \
    docker-compose-v2
```

Включить Docker и Nginx:

```bash
sudo systemctl enable --now docker
sudo systemctl enable --now nginx
```

Проверить:

```bash
docker --version
docker compose version
nginx -v
certbot --version
```

## 3. Разрешить запуск Docker без sudo

```bash
sudo usermod -aG docker $USER
```

после, перезагрузить сервер для применения.

Проверить:

```bash
docker ps
```

## 4. Клонировать EVA

Например:

```bash
mkdir -p ~/Projects/max-bot
cd ~/Projects/max-bot

git clone https://github.com/dkuzzin/eva_bot.git
cd eva_bot
```

## 5. Создать `.env`

```bash
cp .env.example .env
nano .env
```

Заполнить реальные секреты.

## 6. Подготовить Nginx для первого выпуска сертификатов

Production-конфиги нельзя сразу включить на новом сервере, потому что они ссылаются на ещё не существующие файлы:

```text
/etc/letsencrypt/live/api.chernushka.fun/...
/etc/letsencrypt/live/bot.chernushka.fun/...
/etc/letsencrypt/live/eva.chernushka.fun/...
```

Поэтому сначала используются HTTP-only конфиги из:

```text
deploy/nginx/cert/
```

Создать директорию для ACME challenge:

```bash
sudo mkdir -p /var/www/letsencrypt/.well-known/acme-challenge
```

Скопировать bootstrap-конфиги:

```bash
sudo cp deploy/nginx/cert/eva-api-cert.conf \
    /etc/nginx/sites-available/eva-api.conf

sudo cp deploy/nginx/cert/eva-bot-cert.conf \
    /etc/nginx/sites-available/eva-bot.conf

sudo cp deploy/nginx/cert/eva-miniapp-cert.conf \
    /etc/nginx/sites-available/eva-miniapp.conf
```

Отключить стандартный сайт Nginx:

```bash
sudo rm -f /etc/nginx/sites-enabled/default
```

Создать ссылки:

```bash
sudo ln -sf /etc/nginx/sites-available/eva-api.conf \
    /etc/nginx/sites-enabled/eva-api.conf

sudo ln -sf /etc/nginx/sites-available/eva-bot.conf \
    /etc/nginx/sites-enabled/eva-bot.conf

sudo ln -sf /etc/nginx/sites-available/eva-miniapp.conf \
    /etc/nginx/sites-enabled/eva-miniapp.conf
```

Проверить конфигурацию:

```bash
sudo nginx -t
```

Если результат успешный:

```bash
sudo systemctl restart nginx
```

## 7. Выпустить сертификаты Let's Encrypt

Сертификаты выпускаются отдельно, потому что production-конфиги EVA ожидают отдельную директорию для каждого домена.

### Backend API

```bash
sudo certbot certonly \
    --webroot \
    -w /var/www/letsencrypt \
    -d api.chernushka.fun
```

### MAX Bot

```bash
sudo certbot certonly \
    --webroot \
    -w /var/www/letsencrypt \
    -d bot.chernushka.fun
```

### Mini App

```bash
sudo certbot certonly \
    --webroot \
    -w /var/www/letsencrypt \
    -d eva.chernushka.fun
```

Проверить:

```bash
sudo certbot certificates
```

Должны существовать директории:

```bash
sudo ls /etc/letsencrypt/live/api.chernushka.fun/
sudo ls /etc/letsencrypt/live/bot.chernushka.fun/
sudo ls /etc/letsencrypt/live/eva.chernushka.fun/
```

В каждой должны быть как минимум:

```text
fullchain.pem
privkey.pem
cert.pem
chain.pem
```

## 8. Запустить EVA через Docker Compose

Из корня репозитория:

```bash
docker compose up -d --build
```

Проверить:

```bash
docker compose ps
```

Ожидаемая схема:

```text
server    → 127.0.0.1:8080
bot       → 127.0.0.1:8081
miniapp   → 127.0.0.1:8082
db        → только внутренняя Docker-сеть
```

Проверить непосредственно с VPS:

```bash
curl -i http://127.0.0.1:8080
curl -i http://127.0.0.1:8081/webhook
curl -I http://127.0.0.1:8082
```

`/webhook` не обязан отвечать `200` на обычный GET. Важно, чтобы порт был доступен и запрос попадал в приложение.

Логи:

```bash
docker compose logs --tail=100
```

Или отдельного сервиса:

```bash
docker compose logs -f server
docker compose logs -f bot
docker compose logs -f miniapp
docker compose logs -f db
```

## 9. Установить production-конфиги Nginx

Теперь сертификаты существуют, поэтому можно заменить временные конфиги настоящими:

```bash
sudo cp deploy/nginx/eva-api.conf \
    /etc/nginx/sites-available/eva-api.conf

sudo cp deploy/nginx/eva-bot.conf \
    /etc/nginx/sites-available/eva-bot.conf

sudo cp deploy/nginx/eva-miniapp.conf \
    /etc/nginx/sites-available/eva-miniapp.conf
```

Проверить:

```bash
sudo nginx -t
```

Если конфигурация корректна:

```bash
sudo systemctl reload nginx
```

## 10. Проверить HTTPS

Mini App:

```bash
curl -I https://eva.chernushka.fun
```

Backend:

```bash
curl -i https://api.chernushka.fun
```

Bot:

```bash
curl -i https://bot.chernushka.fun/webhook
```

Также проверить сайт в браузере:

```text
https://eva.chernushka.fun
```

## 12. Вернуть Cloudflare Proxy

После успешной проверки HTTPS можно вернуть Cloudflare-записи:

```text
DNS only → Proxied
```

Для Cloudflare SSL/TLS установить:

```text
Full (strict)
```

Не использовать `Flexible`, потому что между Cloudflare и Nginx уже работает полноценный HTTPS с Let's Encrypt.

После включения Cloudflare ещё раз проверить:

```bash
curl -I https://eva.chernushka.fun
```

## Обновление проекта

После изменений в Git:

```bash
cd ~/Projects/max-bot/eva_bot
git pull
docker compose up -d --build
```

Если Nginx-конфиги также изменились:

```bash
sudo cp deploy/nginx/eva-api.conf /etc/nginx/sites-available/eva-api.conf
sudo cp deploy/nginx/eva-bot.conf /etc/nginx/sites-available/eva-bot.conf
sudo cp deploy/nginx/eva-miniapp.conf /etc/nginx/sites-available/eva-miniapp.conf

sudo nginx -t &&
sudo systemctl reload nginx
```

## Важное замечание о PostgreSQL

`docker compose up` на новом VPS создаёт новый Docker volume PostgreSQL.

Он не переносит данные со старого сервера автоматически.

Если необходимо сохранить старые мероприятия, регистрации и другие данные, перед использованием новой БД необходимо отдельно восстановить PostgreSQL из дампа или перенести volume.
