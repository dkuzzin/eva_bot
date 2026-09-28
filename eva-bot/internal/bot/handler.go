package bot

import (
	"context"
	"log"

	maxbot "github.com/max-messenger/max-bot-api-client-go/v2"
	"github.com/max-messenger/max-bot-api-client-go/v2/model"
)

type Handler struct {
	messages    maxbot.MessagesAPI
	botID       int64
	botUsername string
}

func NewHandler(
	messages maxbot.MessagesAPI,
	botID int64,
	botUsername string,
) *Handler {
	return &Handler{
		messages:    messages,
		botID:       botID,
		botUsername: botUsername,
	}
}

func (h *Handler) HandleUpdate(
	ctx context.Context,
	update model.Update,
) {
	switch update.UpdateType {
	case model.UpdateBotStarted:
		if err := h.sendMainMenu(ctx, update.ChatID); err != nil {
			log.Printf("send main menu: %v", err)
		}

	case model.UpdateMessageCallback:
		// Кнопка профиля пока ничего не делает.
		return
	}
}
