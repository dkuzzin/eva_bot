package eventapi

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"time"
)

type Event struct {
	ID          int64      `json:"id"`
	Title       string     `json:"title"`
	Description string     `json:"description"`
	StartsAt    time.Time  `json:"startsAt"`
	EndsAt      *time.Time `json:"endsAt"`
	Location    *string    `json:"location"`
	Capacity    *int       `json:"capacity"`
}

type Client struct {
	baseURL    string
	httpClient *http.Client
}

func NewClient(
	baseURL string,
	httpClient *http.Client,
) *Client {
	return &Client{
		baseURL:    strings.TrimRight(baseURL, "/"),
		httpClient: httpClient,
	}
}

func (c *Client) GetEvent(
	ctx context.Context,
	eventID int64,
) (Event, error) {
	url := fmt.Sprintf(
		"%s/api/events/%d",
		c.baseURL,
		eventID,
	)

	request, err := http.NewRequestWithContext(
		ctx,
		http.MethodGet,
		url,
		nil,
	)
	if err != nil {
		return Event{}, fmt.Errorf(
			"create get event request: %w",
			err,
		)
	}

	response, err := c.httpClient.Do(request)
	if err != nil {
		return Event{}, fmt.Errorf(
			"get event: %w",
			err,
		)
	}
	defer response.Body.Close()

	if response.StatusCode != http.StatusOK {
		return Event{}, fmt.Errorf(
			"get event: unexpected status %d",
			response.StatusCode,
		)
	}

	var event Event

	if err := json.NewDecoder(response.Body).Decode(&event); err != nil {
		return Event{}, fmt.Errorf(
			"decode event: %w",
			err,
		)
	}

	return event, nil
}
