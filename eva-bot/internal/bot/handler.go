package bot

import (
	"context"
	"log"

	maxbot "github.com/max-messenger/max-bot-api-client-go/v2"
	"github.com/max-messenger/max-bot-api-client-go/v2/model"
)

const startCommand = "/start"

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

func (h *Handler) HandleUpdate(ctx context.Context, update model.Update) {
	log.Printf(
		"received update: type=%s chat=%d user=%d",
		update.UpdateType,
		update.ChatID,
		update.UserID,
	)

	var err error

	switch update.UpdateType {
	case model.UpdateBotStarted:
		err = h.handleBotStarted(ctx, update)

	case model.UpdateMessageCreated:
		err = h.handleMessageCreated(ctx, update)

	default:
		return
	}

	if err != nil {
		log.Printf(
			"failed to handle update %s: %v",
			update.UpdateType,
			err,
		)
	}
}

func (h *Handler) handleBotStarted(
	ctx context.Context,
	update model.Update,
) error {
	return h.sendMainMenu(ctx, update.ChatID)
}

func (h *Handler) handleMessageCreated(
	ctx context.Context,
	update model.Update,
) error {
	if update.Message == nil {
		return nil
	}

	if maxbot.GetCommand(update) != startCommand {
		return nil
	}

	return h.sendMainMenu(ctx, update.ChatID)
}
