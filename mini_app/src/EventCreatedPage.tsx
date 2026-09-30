import './EventCreatedPage.css'

type EventCreatedPageProps = {
    eventId: number
    onOpenEvent: () => void
    onOpenRegistrations: () => void
    onOpenMyEvents: () => void
}

function EventCreatedPage({
    eventId,
    onOpenEvent,
    onOpenRegistrations,
    onOpenMyEvents,
}: EventCreatedPageProps) {
    const eventUrl =
        `${window.location.origin}/?eventId=${eventId}`

    async function handleCopyLink() {
        await navigator.clipboard.writeText(eventUrl)
    }

    return (
        <main className="event-created-page">
            <div className="event-created-card">
                <h1>Мероприятие создано</h1>

                <p>
                    Отправьте ссылку участникам, чтобы они могли
                    открыть мероприятие и зарегистрироваться.
                </p>

                <div className="event-link">
                    {eventUrl}
                </div>

                <button
                    type="button"
                    onClick={handleCopyLink}
                >
                    Скопировать ссылку
                </button>

                <button
                    type="button"
                    onClick={onOpenEvent}
                >
                    Открыть мероприятие
                </button>

                <button
                    type="button"
                    onClick={onOpenRegistrations}
                >
                    Участники и управление
                </button>

                <button
                    type="button"
                    onClick={onOpenMyEvents}
                >
                    Мои мероприятия
                </button>
            </div>
        </main>
    )
}

export default EventCreatedPage