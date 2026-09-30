import { useEffect, useState } from 'react'
import { getMaxUserHeaders } from './maxUser'
import './MyEventsPage.css'

type Event = {
    id: number
    title: string
    description: string
    startsAt: string
    endsAt: string | null
    location: string | null
    capacity: number | null
    status: string
}

type MyEventsPageProps = {
    onOpenRegistrations: (eventId: number) => void
    onCreateEvent: () => void
}

function MyEventsPage({
    onOpenRegistrations,
    onCreateEvent,
}: MyEventsPageProps) {
    const [events, setEvents] = useState<Event[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    async function loadEvents() {
        setLoading(true)
        setError(null)

        try {
            const response = await fetch('/api/events', {
                headers: getMaxUserHeaders(),
            })

            if (!response.ok) {
                setError('Не удалось загрузить мероприятия')
                return
            }

            const events: Event[] = await response.json()

            setEvents(events)
        } catch {
            setError('Не удалось связаться с сервером')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadEvents()
    }, [])

    if (loading) {
        return <p>Загрузка...</p>
    }

    if (error !== null) {
        return <p>{error}</p>
    }

    return (
        <main className="my-events-page">
            <div className="my-events-container">
                <h1>Мои мероприятия</h1>

                <button
                    className="new-event-button"
                    type="button"
                    onClick={onCreateEvent}
                >
                    + Создать мероприятие
                </button>

                {events.length === 0 ? (
                    <div className="my-events-empty">
                        У вас пока нет мероприятий.
                    </div>
                ) : (
                    <div className="my-events-list">
                        {events.map((event) => {
                            const registrationUrl =
                                `${window.location.origin}/?eventId=${event.id}`

                            const isCancelled = event.status === 'CANCELLED'

                            return (
                                <article
                                    className={
                                        isCancelled
                                            ? 'my-event-card my-event-card-cancelled'
                                            : 'my-event-card'
                                    }
                                    key={event.id}
                                >
                                    <h2>{event.title}</h2>

                                    {isCancelled && (
                                        <span className="event-status event-status-cancelled">
                                            Отменено
                                        </span>
                                    )}

                                    <div className="my-event-info">
                                        <p>
                                            {new Date(event.startsAt)
                                                .toLocaleString('ru-RU')}
                                        </p>

                                        {event.location !== null && (
                                            <p>{event.location}</p>
                                        )}
                                    </div>

                                    <button
                                        className="manage-event-button"
                                        type="button"
                                        onClick={() => {
                                            onOpenRegistrations(event.id)
                                        }}
                                    >
                                        Участники и управление
                                    </button>

                                    {!isCancelled && (
                                        <div className="registration-link">
                                            <span className="registration-link-label">
                                                Ссылка для регистрации
                                            </span>

                                            <div className="registration-link-row">
                                                <p className="registration-link-value">
                                                    {registrationUrl}
                                                </p>

                                                <button
                                                    className="copy-link-button"
                                                    type="button"
                                                    onClick={() => {
                                                        navigator.clipboard.writeText(registrationUrl)
                                                    }}
                                                >
                                                    Копировать
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </article>
                            )
                        })}
                    </div>
                )}
            </div>
        </main>
    )
}

export default MyEventsPage