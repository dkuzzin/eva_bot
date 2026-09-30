import { useEffect, useState } from 'react'
import { getMaxUserHeaders } from './maxUser'
import './MyRegistrationsPage.css'

type RegistrationEvent = {
    id: number
    title: string
    startsAt: string
    endsAt: string | null
    location: string | null
    status: string
}

type MyRegistration = {
    registrationId: number
    registeredAt: string
    event: RegistrationEvent
}

type MyRegistrationsPageProps = {
    onOpenEvent: (eventId: number) => void
    onBack: () => void
}

function MyRegistrationsPage({
    onOpenEvent,
    onBack,
}: MyRegistrationsPageProps) {
    const [registrations, setRegistrations] =
        useState<MyRegistration[]>([])

    const [loading, setLoading] = useState(true)
    const [error, setError] =
        useState<string | null>(null)

    async function loadRegistrations() {
        setLoading(true)
        setError(null)

        try {
            const response = await fetch(
                '/api/registrations/me',
                {
                    headers: getMaxUserHeaders(),
                }
            )

            if (!response.ok) {
                setError('Не удалось загрузить регистрации')
                return
            }

            const data: MyRegistration[] =
                await response.json()

            setRegistrations(data)
        } catch {
            setError('Не удалось связаться с сервером')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadRegistrations()
    }, [])

    if (loading) {
        return <p>Загрузка...</p>
    }

    if (error !== null) {
        return <p>{error}</p>
    }

    return (
        <main className="my-registrations-page">
            <div className="my-registrations-container">
                <button
                    className="my-registrations-back-button"
                    type="button"
                    onClick={onBack}
                >
                    ← Назад
                </button>

                <h1>Мои регистрации</h1>

                {registrations.length === 0 ? (
                    <div className="my-registrations-empty">
                        Вы пока не зарегистрированы ни на одно мероприятие.
                    </div>
                ) : (
                    <div className="my-registrations-list">
                        {registrations.map((registration) => {
                            const event = registration.event
                            const isCancelled =
                                event.status === 'CANCELLED'

                            return (
                                <article
                                    className={
                                        isCancelled
                                            ? 'my-registration-card my-registration-card-cancelled'
                                            : 'my-registration-card'
                                    }
                                    key={registration.registrationId}
                                >
                                    <h2>{event.title}</h2>

                                    {isCancelled && (
                                        <span className="my-registration-status">
                                            Отменено
                                        </span>
                                    )}

                                    <div className="my-registration-info">
                                        <p>
                                            {new Date(event.startsAt)
                                                .toLocaleString('ru-RU')}
                                        </p>

                                        {event.location !== null && (
                                            <p>{event.location}</p>
                                        )}
                                    </div>

                                    <button
                                        className="open-registration-event-button"
                                        type="button"
                                        onClick={() => {
                                            onOpenEvent(event.id)
                                        }}
                                    >
                                        Открыть мероприятие
                                    </button>
                                </article>
                            )
                        })}
                    </div>
                )}
            </div>
        </main>
    )
}

export default MyRegistrationsPage