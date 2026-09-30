import { useEffect, useState } from 'react'
import './EventRegistrationsPage.css'
import { getMaxUserHeaders } from './maxUser'

type FormField = {
    id: number
    label: string
    position: number
}

type Answer = {
    fieldId: number
    value: string
}

type Registration = {
    id: number
    registeredAt: string
    answers: Answer[]
}

type EventRegistrations = {
    eventId: number
    formFields: FormField[]
    registrations: Registration[]
}

type EventRegistrationsPageProps = {
    eventId: number
    onBack: () => void
}

type Event = {
    id: number
    title: string
    status: string
}

function EventRegistrationsPage({
    eventId,
    onBack,
}: EventRegistrationsPageProps) {
    const [data, setData] =
        useState<EventRegistrations | null>(null)
    const [event, setEvent] =
        useState<Event | null>(null)

    const [loading, setLoading] = useState(true)

    const [error, setError] =
        useState<string | null>(null)
    const [cancellingEvent, setCancellingEvent] = useState(false)
    const [cancelMessage, setCancelMessage] =
        useState<string | null>(null)

    async function loadRegistrations() {
        setLoading(true)
        setError(null)

        try {
            const response = await fetch(
                `/api/events/${eventId}/registrations`,
                {
                    headers: getMaxUserHeaders(),
                }
            )

            if (!response.ok) {
                setError('Не удалось загрузить регистрации')
                return
            }

            const registrations: EventRegistrations =
                await response.json()

            setData(registrations)
        } catch {
            setError('Не удалось связаться с сервером')
        } finally {
            setLoading(false)
        }
    }

    async function loadEvent() {
        try {
            const response = await fetch(
                `/api/events/${eventId}`
            )

            if (!response.ok) {
                return
            }

            const event: Event = await response.json()

            setEvent(event)
        } catch {
            console.error('Не удалось загрузить мероприятие')
        }
    }

    async function downloadCsv() {
        try {
            const response = await fetch(
                `/api/events/${eventId}/registrations/export`,
                {
                    headers: getMaxUserHeaders(),
                }
            )

            if (!response.ok) {
                setError('Не удалось скачать CSV')
                return
            }

            const blob = await response.blob()
            const url = URL.createObjectURL(blob)

            const link = document.createElement('a')
            link.href = url
            link.download = `event-${eventId}-registrations.csv`
            link.click()

            URL.revokeObjectURL(url)
        } catch {
            setError('Не удалось связаться с сервером')
        }
    }

    async function cancelEvent() {
        const confirmed = window.confirm(
            'Отменить мероприятие? Это действие изменит его статус на отменённый.'
        )

        if (!confirmed) {
            return
        }

        setCancellingEvent(true)
        setCancelMessage(null)

        try {
            const response = await fetch(
                `/api/events/${eventId}/cancel`,
                {
                    method: 'PATCH',
                    headers: getMaxUserHeaders(),
                }
            )

            if (!response.ok) {
                setCancelMessage('Не удалось отменить мероприятие')
                return
            }

            const cancelledEvent: Event = await response.json()

            setEvent(cancelledEvent)
            setCancelMessage('Мероприятие отменено')
        } catch {
            setCancelMessage('Не удалось связаться с сервером')
        } finally {
            setCancellingEvent(false)
        }
    }

    useEffect(() => {
        loadEvent()
        loadRegistrations()
    }, [eventId])

    if (loading) {
        return <p>Загрузка...</p>
    }

    if (error !== null) {
        return <p>{error}</p>
    }

    if (data === null) {
        return null
    }

    return (
        <main className="registrations-page">
            <div className="registrations-card">

                <button
                    className="back-button"
                    type="button"
                    onClick={onBack}
                >
                    ← Мои мероприятия
                </button>

                <h1>{event?.title ?? 'Регистрации'}</h1>

                <p className="registrations-subtitle">
                    Участники мероприятия
                </p>

                <p>
                    Зарегистрировано: {data.registrations.length}
                </p>

                {data.registrations.length === 0 ? (
                    <p>На мероприятие пока никто не зарегистрировался.</p>
                ) : (
                    <div className="registrations-list">
                        {data.registrations.map((registration) => (
                            <div
                                className="registration-card"
                                key={registration.id}
                            >
                                {data.formFields.map((field) => {
                                    const answer = registration.answers.find(
                                        (answer) => answer.fieldId === field.id
                                    )

                                    return (
                                        <div key={field.id}>
                                            <strong>{field.label}</strong>
                                            <p>{answer?.value ?? '—'}</p>
                                        </div>
                                    )
                                })}

                                <p className="registration-date">
                                    Зарегистрирован:{' '}
                                    {new Date(
                                        registration.registeredAt
                                    ).toLocaleString('ru-RU')}
                                </p>
                            </div>
                        ))}
                    </div>
                )}

                <button
                    className="export-button"
                    type="button"
                    onClick={downloadCsv}
                >
                    Скачать CSV
                </button>

                <div className="cancel-event-section">
                    <p className="cancel-event-title">
                        Управление мероприятием
                    </p>

                    {event?.status === 'CANCELLED' ? (
                        <p className="cancel-event-message">
                            Мероприятие отменено
                        </p>
                    ) : (
                        <>
                            <button
                                className="cancel-event-button"
                                type="button"
                                onClick={cancelEvent}
                                disabled={cancellingEvent}
                            >
                                {cancellingEvent
                                    ? 'Отмена...'
                                    : 'Отменить мероприятие'}
                            </button>

                            {cancelMessage !== null && (
                                <p className="cancel-event-message">
                                    {cancelMessage}
                                </p>
                            )}
                        </>
                    )}
                </div>
            </div>
        </main>
    )
}

export default EventRegistrationsPage