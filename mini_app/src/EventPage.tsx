import './EventPage.css'
import { useEffect, useState } from 'react'
import { getMaxUserHeaders } from './maxUser'

type FormField = {
    id: number
    label: string
    position: number
}

type Event = {
    id: number
    title: string
    description: string
    startsAt: string
    endsAt: string | null
    location: string | null
    capacity: number | null
    status: string
    formFields: FormField[]
}

type Registration = {
    id: number
    eventId: number
    registeredAt: string
}

type EventPageProps = {
    eventId: number
    onBack?: () => void
    onOpenMyRegistrations: () => void
}

function formatDateTime(value: string) {
    const date = new Date(value)

    return date.toLocaleString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    })
}

function EventPage({
    eventId,
    onBack,
    onOpenMyRegistrations,
}: EventPageProps) {
    const [event, setEvent] = useState<Event | null>(null)
    const [error, setError] = useState<string | null>(null)
    const [loading, setLoading] = useState(true)
    const [answers, setAnswers] = useState<Record<number, string>>({})
    const [registrationMessage, setRegistrationMessage] =
        useState<string | null>(null)
    const [registration, setRegistration] =
        useState<Registration | null>(null)
    const [registrationLoading, setRegistrationLoading] = useState(true)
    const [submitting, setSubmitting] = useState(false)
    const [cancelling, setCancelling] = useState(false)

    async function loadEvent() {
        setLoading(true)
        setError(null)

        try {
            const response = await fetch(`/api/events/${eventId}`)

            if (response.status === 404) {
                setError('Мероприятие не найдено')
                return
            }

            if (!response.ok) {
                setError('Не удалось загрузить мероприятие')
                return
            }

            const event: Event = await response.json()

            setEvent(event)
        } catch {
            setError('Не удалось связаться с сервером')
        } finally {
            setLoading(false)
        }
    }

    async function loadRegistration() {
        setRegistrationLoading(true)

        try {
            const response = await fetch(
                `/api/events/${eventId}/registrations/me`,
                {
                    headers: getMaxUserHeaders(),
                }
            )

            if (response.ok) {
                const data: Registration = await response.json()
                setRegistration(data)
                return
            }

            if (response.status === 404) {
                setRegistration(null)
                return
            }

            console.error(
                'Не удалось загрузить регистрацию:',
                response.status
            )
        } catch {
            console.error(
                'Не удалось связаться с сервером при загрузке регистрации'
            )
        } finally {
            setRegistrationLoading(false)
        }
    }

    async function handleSubmit() {
        if (event === null) {
            return
        }

        const hasEmptyAnswers = event.formFields.some(
            (field) => (answers[field.id] ?? '').trim() === ''
        )

        if (hasEmptyAnswers) {
            setRegistrationMessage('Заполните все поля')
            return
        }

        setSubmitting(true)
        setRegistrationMessage(null)

        const request = {
            answers: event.formFields.map((field) => ({
                fieldId: field.id,
                value: answers[field.id] ?? '',
            })),
        }

        try {
            const response = await fetch(
                `/api/events/${eventId}/registrations`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        ...getMaxUserHeaders(),
                    },
                    body: JSON.stringify(request),
                }
            )

            if (!response.ok) {
                const error = await response.json()
                console.log(error)

                setRegistrationMessage('Не удалось зарегистрироваться')
                return
            }

            const createdRegistration: Registration = await response.json()

            setRegistration(createdRegistration)
            setRegistrationMessage(null)
        } catch {
            setRegistrationMessage('Не удалось связаться с сервером')
        } finally {
            setSubmitting(false)
        }
    }

    async function handleCancelRegistration() {
        setCancelling(true)
        setRegistrationMessage(null)

        try {
            const response = await fetch(
                `/api/events/${eventId}/registrations`,
                {
                    method: 'DELETE',
                    headers: getMaxUserHeaders(),
                }
            )

            if (!response.ok) {
                setRegistrationMessage('Не удалось отменить регистрацию')
                return
            }

            setRegistration(null)
            setAnswers({})
        } catch {
            setRegistrationMessage('Не удалось связаться с сервером')
        } finally {
            setCancelling(false)
        }
    }

    useEffect(() => {
        loadEvent()
        loadRegistration()
    }, [eventId])

    return (
        <main className="event-page">
            {loading ? (
                <p>Загрузка...</p>
            ) : error !== null ? (
                <p>{error}</p>
            ) : event !== null ? (
                <article className="event-card">
                    {onBack !== undefined && (
                        <button
                            className="event-back-button"
                            type="button"
                            onClick={onBack}
                        >
                            ← Мои регистрации
                        </button>
                    )}

                    <h1>{event.title}</h1>

                    <p className="event-description">
                        {event.description}
                    </p>

                    <div className="event-info">
                        <p>Начало: {formatDateTime(event.startsAt)}</p>

                        {event.endsAt !== null && (
                            <p>Окончание: {formatDateTime(event.endsAt)}</p>
                        )}

                        {event.location !== null && (
                            <p>Место: {event.location}</p>
                        )}

                        {event.capacity !== null && (
                            <p>Количество мест: {event.capacity}</p>
                        )}
                    </div>

                    {event.status === 'CANCELLED' ? (
                        <div className="registration-info">
                            <h2>Мероприятие отменено</h2>
                            <p>
                                Регистрация на это мероприятие недоступна.
                            </p>
                        </div>
                    ) : registrationLoading ? (
                        <div className="registration-info">
                            <h2>Регистрация</h2>
                            <p>Проверяем регистрацию...</p>
                        </div>
                    ) : registration === null ? (
                        <div className="registration-form">
                            <h2>Регистрация</h2>

                            {event.formFields.map((field) => (
                                <div key={field.id}>
                                    <label htmlFor={`field-${field.id}`}>
                                        {field.label}
                                    </label>

                                    <input
                                        id={`field-${field.id}`}
                                        type="text"
                                        value={answers[field.id] ?? ''}
                                        onChange={(inputEvent) => {
                                            setAnswers((previousAnswers) => ({
                                                ...previousAnswers,
                                                [field.id]: inputEvent.target.value,
                                            }))
                                        }}
                                    />
                                </div>
                            ))}

                            <button
                                className="register-button"
                                type="button"
                                onClick={handleSubmit}
                                disabled={submitting}
                            >
                                {submitting ? 'Регистрация...' : 'Зарегистрироваться'}
                            </button>

                            {registrationMessage !== null && (
                                <p>{registrationMessage}</p>
                            )}
                        </div>
                    ) : (
                        <div className="registration-info">
                            <h2>Регистрация</h2>
                            <p>Вы зарегистрированы</p>

                            <button
                                className="my-registrations-button"
                                type="button"
                                onClick={onOpenMyRegistrations}
                            >
                                Мои регистрации
                            </button>

                            <button
                                className="cancel-registration-button"
                                type="button"
                                onClick={handleCancelRegistration}
                                disabled={cancelling}
                            >
                                {cancelling ? 'Отмена...' : 'Отменить регистрацию'}
                            </button>

                            {registrationMessage !== null && (
                                <p>{registrationMessage}</p>
                            )}
                        </div>
                    )}

                </article>
            ) : null}
        </main>
    )
}

export default EventPage