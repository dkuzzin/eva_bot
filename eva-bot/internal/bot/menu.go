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
	profileButtonText     = "Посмотреть профиль"

	createEventPayload = "create_event"
	profilePayload     = "profile"
)

func (h *Handler) sendMainMenu(
	ctx context.Context,
	chatID int64,
) error {
	keyboard := model.NewKeyboard()

	keyboard.
		AddRow().
		AddButton(model.Button{
			Type:      model.ButtonOpenApp,
			Text:      createEventButtonText,
			WebApp:    strings.TrimPrefix(h.botUsername, "@"),
			ContactID: h.botID,
			Payload:   createEventPayload,
		})

	keyboard.
		AddRow().
		AddCallBack(
			profileButtonText,
			profilePayload,
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
