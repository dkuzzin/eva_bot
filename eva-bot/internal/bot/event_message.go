package bot

import (
	"context"
	"fmt"
	"strings"
	"time"

	maxbot "github.com/max-messenger/max-bot-api-client-go/v2"

	"github.com/dkuzzin/eva_bot/internal/eventapi"
)

const maxBaseURL = "https://max.ru"

func (h *Handler) SendCreatedEvent(
	ctx context.Context,
	userID int64,
	event eventapi.Event,
) error {
	message := maxbot.NewMessage().
		SetUser(userID).
		SetText(h.createdEventText(event))

	if _, err := h.messages.Send(ctx, message); err != nil {
		return fmt.Errorf(
			"send created event message: %w",
			err,
		)
	}

	return nil
}

func (h *Handler) createdEventText(
	event eventapi.Event,
) string {
	var text strings.Builder

	fmt.Fprintf(
		&text,
		"📅 %s\n",
		event.Title,
	)

	if event.Description != "" {
		fmt.Fprintf(
			&text,
			"\n%s\n",
			event.Description,
		)
	}

	fmt.Fprintf(
		&text,
		"\n🕒 Начало: %s\n",
		formatEventTime(event.StartsAt),
	)

	if event.EndsAt != nil {
		fmt.Fprintf(
			&text,
			"🕒 Окончание: %s\n",
			formatEventTime(*event.EndsAt),
		)
	}

	if event.Location != nil && *event.Location != "" {
		fmt.Fprintf(
			&text,
			"📍 Место: %s\n",
			*event.Location,
		)
	}

	if event.Capacity != nil {
		fmt.Fprintf(
			&text,
			"👥 Количество мест: %d\n",
			*event.Capacity,
		)
	}

	fmt.Fprintf(
		&text,
		"\nРегистрация:\n%s",
		h.eventURL(event.ID),
	)

	return text.String()
}

func (h *Handler) eventURL(eventID int64) string {
	username := strings.TrimPrefix(
		h.botUsername,
		"@",
	)

	return fmt.Sprintf(
		"%s/%s?startapp=event_%d",
		maxBaseURL,
		username,
		eventID,
	)
}

func formatEventTime(value time.Time) string {
	return value.Format("02.01.2006 15:04")
}
