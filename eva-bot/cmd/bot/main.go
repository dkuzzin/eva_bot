package main

import (
	"context"
	"log"
	"net/http"
	"time"

	maxbot "github.com/max-messenger/max-bot-api-client-go/v2"

	"github.com/dkuzzin/eva_bot/internal/bot"
	"github.com/dkuzzin/eva_bot/internal/config"
)

const (
	webhookPath = "/webhook"

	httpClientTimeout = 10 * time.Second
	readHeaderTimeout = 5 * time.Second
)

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatal(err)
	}

	api, err := maxbot.NewApi(
		cfg.BotToken,
		maxbot.WithHTTPClient(&http.Client{
			Timeout: httpClientTimeout,
		}),
	)
	if err != nil {
		log.Fatal(err)
	}

	ctx := context.Background()

	botInfo, err := api.Bots.GetMyInfo(ctx)
	if err != nil {
		log.Fatal(err)
	}

	log.Printf(
		"bot started: id=%d username=%s",
		botInfo.UserID,
		botInfo.Username,
	)

	updateHandler := bot.NewHandler(
		api.Messages,
		botInfo.UserID,
		botInfo.Username,
	)

	mux := http.NewServeMux()

	mux.Handle(
		webhookPath,
		api.GetHandler(
			updateHandler.HandleUpdate,
			cfg.WebhookSecret,
		),
	)

	server := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           mux,
		ReadHeaderTimeout: readHeaderTimeout,
	}

	log.Printf(
		"webhook server listening on %s",
		cfg.HTTPAddr,
	)

	if err := server.ListenAndServe(); err != nil {
		log.Fatal(err)
	}
}
