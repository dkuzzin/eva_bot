package main

import (
	"context"
	"log"
	"net/http"
	"time"

	maxbot "github.com/max-messenger/max-bot-api-client-go/v2"

	"github.com/dkuzzin/eva_bot/internal/bot"
	"github.com/dkuzzin/eva_bot/internal/config"
	"github.com/dkuzzin/eva_bot/internal/eventapi"
	"github.com/dkuzzin/eva_bot/internal/miniapp"
)

const (
	webhookPath       = "/webhook"
	eventCreatedPath  = "/bot-api/events/created"
	backendURL        = "http://server:8080"
	httpClientTimeout = 10 * time.Second
	readHeaderTimeout = 5 * time.Second
)

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatal(err)
	}

	httpClient := &http.Client{
		Timeout: httpClientTimeout,
	}

	api, err := maxbot.NewApi(
		cfg.BotToken,
		maxbot.WithHTTPClient(httpClient),
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

	botHandler := bot.NewHandler(
		api.Messages,
		botInfo.UserID,
		botInfo.Username,
	)

	eventClient := eventapi.NewClient(
		backendURL,
		httpClient,
	)

	miniAppHandler := miniapp.NewHandler(
		cfg.BotToken,
		eventClient,
		botHandler,
	)

	mux := http.NewServeMux()

	mux.Handle(
		webhookPath,
		api.GetHandler(
			botHandler.HandleUpdate,
			cfg.WebhookSecret,
		),
	)

	mux.HandleFunc(
		eventCreatedPath,
		miniAppHandler.HandleEventCreated,
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
