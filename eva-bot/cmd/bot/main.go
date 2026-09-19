package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"syscall"

	maxbot "github.com/max-messenger/max-bot-api-client-go"
	"github.com/max-messenger/max-bot-api-client-go/schemes"
)

func main() {
	api, err := maxbot.New(os.Getenv("TOKEN"))
	if err != nil {
		panic(err)
	}

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	info, err := api.Bots.GetBot(ctx)
	if err != nil {
		panic(err)
	}

	fmt.Printf("Get me: %#v\n", info)

	go func() {
		exit := make(chan os.Signal, 1)

		signal.Notify(exit, os.Interrupt, syscall.SIGTERM)

		<-exit
		cancel()
	}()

	for upd := range api.GetUpdates(ctx) {
		switch upd := upd.(type) {
		case *schemes.MessageCreatedUpdate:
			err := api.Messages.Send(
				ctx,
				maxbot.NewMessage().
					SetChat(upd.Message.Recipient.ChatId).
					SetText("Hello from Bot"),
			)

			if err != nil {
				fmt.Println("send error:", err)
			}
		}
	}
}
