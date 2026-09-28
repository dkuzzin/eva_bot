import { useEffect, useState } from 'react'
import './EventRegistrationsPage.css'

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
}

function EventRegistrationsPage({
    eventId,
}: EventRegistrationsPageProps) {
    const [data, setData] =
        useState<EventRegistrations | null>(null)

    const [loading, setLoading] = useState(true)

    const [error, setError] =
        useState<string | null>(null)

    async function loadRegistrations() {
        setLoading(true)
        setError(null)

        try {
            const response = await fetch(
                `/api/events/${eventId}/registrations`
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

    useEffect(() => {
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
                <h1>Регистрации</h1>

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

                <a
                    className="export-button"
                    href={`/api/events/${eventId}/registrations/export`}
                >
                    Скачать CSV
                </a>
            </div>
        </main>
    )
}

export default EventRegistrationsPage