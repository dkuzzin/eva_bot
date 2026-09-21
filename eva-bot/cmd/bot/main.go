package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"time"

	maxbot "github.com/max-messenger/max-bot-api-client-go/v2"
	"github.com/max-messenger/max-bot-api-client-go/v2/model"
)

func main() {
	botToken := os.Getenv("BOT_TOKEN")
	webhookSecret := os.Getenv("WEBHOOK_SECRET")

	if botToken == "" {
		log.Fatal("BOT_TOKEN is not set")
	}

	if webhookSecret == "" {
		log.Fatal("WEBHOOK_SECRET is not set")
	}

	opts := []maxbot.Opt{
		maxbot.WithHTTPClient(&http.Client{
			Timeout: 10 * time.Second,
		}),
	}

	api, err := maxbot.NewApi(botToken, opts...)
	if err != nil {
		log.Fatal(err)
	}

	ctx := context.Background()

	info, err := api.Bots.GetMyInfo(ctx)
	if err != nil {
		log.Fatal(err)
	}

	log.Printf("bot started: %+v", info)

	handle := func(ctx context.Context, update model.Update) {
		log.Printf(
			"received update: type=%s chat=%d user=%d",
			update.UpdateType,
			update.ChatID,
			update.UserID,
		)

		switch update.UpdateType {
		case model.UpdateMessageCreated:
			msg := maxbot.NewMessage().
				SetChat(update.ChatID).
				SetText("Hello from EVA bot")

			_, err := api.Messages.Send(ctx, msg)
			if err != nil {
				log.Printf("failed to send message: %v", err)
			}
		}
	}

	webhookHandler := api.GetHandler(
		handle,
		webhookSecret,
	)

	mux := http.NewServeMux()

	mux.Handle("/webhook", webhookHandler)

	server := &http.Server{
		Addr:              ":8081",
		Handler:           mux,
		ReadHeaderTimeout: 5 * time.Second,
	}

	log.Println("webhook server listening on :8081")

	if err := server.ListenAndServe(); err != nil {
		log.Fatal(err)
	}
}
