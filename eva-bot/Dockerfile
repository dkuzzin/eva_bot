FROM golang:1.27-alpine AS build

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 go build -o /eva-bot ./cmd/bot


FROM alpine:3.22

RUN apk add --no-cache ca-certificates

COPY certs/*.crt /usr/local/share/ca-certificates/
RUN update-ca-certificates

WORKDIR /app

COPY --from=build /eva-bot ./eva-bot

EXPOSE 8081

ENTRYPOINT ["./eva-bot"]
