package miniapp

import (
	"encoding/json"
	"log"
	"net/http"

	maxbot "github.com/max-messenger/max-bot-api-client-go/v2"

	"github.com/dkuzzin/eva_bot/internal/bot"
	"github.com/dkuzzin/eva_bot/internal/eventapi"
)

type Handler struct {
	botToken string
	events   *eventapi.Client
	bot      *bot.Handler
}

type eventCreatedRequest struct {
	EventID  int64  `json:"eventId"`
	InitData string `json:"initData"`
}

func NewHandler(
	botToken string,
	events *eventapi.Client,
	botHandler *bot.Handler,
) *Handler {
	return &Handler{
		botToken: botToken,
		events:   events,
		bot:      botHandler,
	}
}

func (h *Handler) HandleEventCreated(
	w http.ResponseWriter,
	r *http.Request,
) {
	if r.Method != http.MethodPost {
		http.Error(
			w,
			"method not allowed",
			http.StatusMethodNotAllowed,
		)
		return
	}

	var request eventCreatedRequest

	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		http.Error(
			w,
			"invalid request",
			http.StatusBadRequest,
		)
		return
	}

	if request.EventID <= 0 || request.InitData == "" {
		http.Error(
			w,
			"eventId and initData are required",
			http.StatusBadRequest,
		)
		return
	}

	initData, err := maxbot.ValidateInitData(
		request.InitData,
		h.botToken,
	)
	if err != nil {
		log.Printf(
			"validate mini app init data: %v",
			err,
		)

		http.Error(
			w,
			"unauthorized",
			http.StatusUnauthorized,
		)
		return
	}

	userID := initData.User.ID

	if userID <= 0 {
		http.Error(
			w,
			"invalid user",
			http.StatusUnauthorized,
		)
		return
	}

	event, err := h.events.GetEvent(
		r.Context(),
		request.EventID,
	)
	if err != nil {
		log.Printf(
			"get event %d: %v",
			request.EventID,
			err,
		)

		http.Error(
			w,
			"failed to get event",
			http.StatusBadGateway,
		)
		return
	}

	if err := h.bot.SendCreatedEvent(
		r.Context(),
		userID,
		event,
	); err != nil {
		log.Printf(
			"send event %d to user %d: %v",
			request.EventID,
			userID,
			err,
		)

		http.Error(
			w,
			"failed to send message",
			http.StatusBadGateway,
		)
		return
	}

	log.Printf(
		"event created message sent: eventId=%d userId=%d",
		request.EventID,
		userID,
	)

	w.WriteHeader(http.StatusNoContent)
}
