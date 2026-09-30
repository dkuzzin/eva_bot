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

	createEventButtonText     = "Создать мероприятие"
	myEventsButtonText        = "Мои мероприятия"
	myRegistrationsButtonText = "Мои регистрации"

	createEventPayload     = "create_event"
	myEventsPayload        = "my_events"
	myRegistrationsPayload = "my_registrations"
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
		AddButton(model.Button{
			Type:      model.ButtonOpenApp,
			Text:      myEventsButtonText,
			WebApp:    strings.TrimPrefix(h.botUsername, "@"),
			ContactID: h.botID,
			Payload:   myEventsPayload,
		})

	keyboard.
		AddRow().
		AddButton(model.Button{
			Type:      model.ButtonOpenApp,
			Text:      myRegistrationsButtonText,
			WebApp:    strings.TrimPrefix(h.botUsername, "@"),
			ContactID: h.botID,
			Payload:   myRegistrationsPayload,
		})

	message := maxbot.NewMessage().
		SetChat(chatID).
		SetText(welcomeText).
		AddKeyboard(keyboard)

	if _, err := h.messages.Send(ctx, message); err != nil {
		return fmt.Errorf("send main menu: %w", err)
	}

	return nil
}
