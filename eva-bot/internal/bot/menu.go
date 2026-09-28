package bot

import (
	"context"
	"fmt"
	"strings"

	maxbot "github.com/max-messenger/max-bot-api-client-go/v2"
	"github.com/max-messenger/max-bot-api-client-go/v2/model"
)

const (
	welcomeText = "Добро пожаловать в EVA"

	createEventButtonText = "Создать мероприятие"
	openAppButtonText     = "Открыть EVA"

	maxBaseURL            = "https://max.ru"
	createEventStartParam = "create_event"
)

func (h *Handler) sendMainMenu(
	ctx context.Context,
	chatID int64,
) error {
	keyboard := model.NewKeyboard()

	keyboard.
		AddRow().
		AddLink(
			createEventButtonText,
			h.createEventURL(),
		)

	keyboard.
		AddRow().
		AddOpenApp(
			openAppButtonText,
			h.botID,
		)

	message := maxbot.NewMessage().
		SetChat(chatID).
		SetText(welcomeText).
		AddKeyboard(keyboard)

	if _, err := h.messages.Send(ctx, message); err != nil {
		return fmt.Errorf("send main menu: %w", err)
	}

	return nil
}

func (h *Handler) createEventURL() string {
	username := strings.TrimPrefix(h.botUsername, "@")

	return fmt.Sprintf(
		"%s/%s?startapp=%s",
		maxBaseURL,
		username,
		createEventStartParam,
	)
}
