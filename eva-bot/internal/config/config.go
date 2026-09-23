package config

import (
	"errors"
	"os"
)

const DefaultHTTPAddr = ":8081"

type Config struct {
	BotToken      string
	WebhookSecret string
	HTTPAddr      string
}

func Load() (Config, error) {
	cfg := Config{
		BotToken:      os.Getenv("BOT_TOKEN"),
		WebhookSecret: os.Getenv("WEBHOOK_SECRET"),
		HTTPAddr:      DefaultHTTPAddr,
	}

	if cfg.BotToken == "" {
		return Config{}, errors.New("BOT_TOKEN is not set")
	}

	if cfg.WebhookSecret == "" {
		return Config{}, errors.New("WEBHOOK_SECRET is not set")
	}

	return cfg, nil
}
