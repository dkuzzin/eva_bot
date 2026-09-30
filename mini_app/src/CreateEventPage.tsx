import './CreateEventPage.css'
import { useState } from 'react'
import { getMaxUserHeaders } from './maxUser'

type CreateEventPageProps = {
    onCreated: (eventId: number) => void
    onOpenMyEvents: () => void
    onOpenMyRegistrations: () => void
}

function CreateEventPage({
    onCreated,
    onOpenMyEvents,
    onOpenMyRegistrations,
}: CreateEventPageProps) {
    const [title, setTitle] = useState('')
    const [description, setDescription] = useState('')
    const [startsAt, setStartsAt] = useState('')
    const [endsAt, setEndsAt] = useState('')
    const [location, setLocation] = useState('')
    const [capacity, setCapacity] = useState('')

    // Первое поле формы всегда "ФИО".
    // Куратор не может изменить или удалить его.
    const [formFields, setFormFields] = useState<string[]>(['ФИО'])

    const [message, setMessage] = useState<string | null>(null)
    const [creating, setCreating] = useState(false)

    async function handleCreateEvent() {
        if (title.trim() === '') {
            setMessage('Введите название мероприятия')
            return
        }

        if (startsAt === '') {
            setMessage('Укажите время начала')
            return
        }

        if (endsAt !== '' && new Date(endsAt) <= new Date(startsAt)) {
            setMessage('Окончание должно быть позже начала')
            return
        }

        setCreating(true)
        setMessage(null)

        const request = {
            title,
            description,
            startsAt: new Date(startsAt).toISOString(),
            endsAt:
                endsAt === ''
                    ? null
                    : new Date(endsAt).toISOString(),
            location:
                location === ''
                    ? null
                    : location,
            capacity:
                capacity === ''
                    ? null
                    : Number(capacity),

            // Пустые дополнительные вопросы не отправляем.
            // "ФИО" всегда находится первым элементом и не может быть пустым.
            formFields: formFields
                .filter((label) => label.trim() !== '')
                .map((label) => ({
                    label: label.trim(),
                })),
        }

        try {
            const response = await fetch('/api/events', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...getMaxUserHeaders(),
                },
                body: JSON.stringify(request),
            })

            if (!response.ok) {
                const error = await response.json()
                console.log(error)

                setMessage('Не удалось создать мероприятие')
                return
            }

            const createdEvent = await response.json()

            // TODO: интеграция с Go-ботом.
            // После создания мероприятия Mini App сообщает боту
            // eventId, чтобы бот мог отправить куратору сообщение.
            const initData = window.WebApp?.initData

            if (initData) {
                const botResponse = await fetch(
                    '/bot-api/events/created',
                    {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            eventId: createdEvent.id,
                            initData,
                        }),
                    }
                )

                if (!botResponse.ok) {
                    console.error(
                        'Не удалось отправить сообщение через бота:',
                        botResponse.status
                    )
                }
            } else {
                console.warn(
                    'Mini App открыт не через MAX: initData отсутствует'
                )
            }

            onCreated(createdEvent.id)
        } catch {
            setMessage('Не удалось связаться с сервером')
        } finally {
            setCreating(false)
        }
    }

    return (
        <main className="create-event-page">
            <div className="create-event-card">

                <div className="create-event-navigation">
                    <button
                        className="navigation-button"
                        type="button"
                        onClick={onOpenMyEvents}
                    >
                        Мои мероприятия
                    </button>

                    <button
                        className="navigation-button"
                        type="button"
                        onClick={onOpenMyRegistrations}
                    >
                        Мои регистрации
                    </button>
                </div>

                <h1>Создать мероприятие</h1>

                <div>
                    <label htmlFor="title">
                        Название
                    </label>

                    <input
                        id="title"
                        type="text"
                        value={title}
                        onChange={(event) => {
                            setTitle(event.target.value)
                        }}
                    />
                </div>

                <div>
                    <label htmlFor="description">
                        Описание
                    </label>

                    <textarea
                        id="description"
                        value={description}
                        onChange={(event) => {
                            setDescription(event.target.value)
                        }}
                    />
                </div>

                <div>
                    <label htmlFor="startsAt">
                        Начало
                    </label>

                    <input
                        id="startsAt"
                        type="datetime-local"
                        value={startsAt}
                        onChange={(event) => {
                            setStartsAt(event.target.value)
                        }}
                    />
                </div>

                <div>
                    <label htmlFor="endsAt">
                        Окончание
                    </label>

                    <input
                        id="endsAt"
                        type="datetime-local"
                        value={endsAt}
                        onChange={(event) => {
                            setEndsAt(event.target.value)
                        }}
                    />
                </div>

                <div>
                    <label htmlFor="location">
                        Место
                    </label>

                    <input
                        id="location"
                        type="text"
                        value={location}
                        onChange={(event) => {
                            setLocation(event.target.value)
                        }}
                    />
                </div>

                <div>
                    <label htmlFor="capacity">
                        Количество мест
                    </label>

                    <input
                        id="capacity"
                        type="number"
                        min="1"
                        value={capacity}
                        onChange={(event) => {
                            setCapacity(event.target.value)
                        }}
                    />
                </div>

                <h2>Форма регистрации</h2>

                {formFields.map((field, index) => (
                    <div key={index}>
                        <label htmlFor={`form-field-${index}`}>
                            {index === 0
                                ? 'Обязательное поле'
                                : `Вопрос ${index + 1}`}
                        </label>

                        <input
                            id={`form-field-${index}`}
                            type="text"
                            value={field}
                            readOnly={index === 0}
                            onChange={(event) => {
                                if (index === 0) {
                                    return
                                }

                                const newFormFields = [...formFields]

                                newFormFields[index] =
                                    event.target.value

                                setFormFields(newFormFields)
                            }}
                        />

                        {index !== 0 && (
                            <button
                                className="remove-field-button"
                                type="button"
                                onClick={() => {
                                    setFormFields(
                                        formFields.filter(
                                            (_, fieldIndex) =>
                                                fieldIndex !== index
                                        )
                                    )
                                }}
                            >
                                Удалить вопрос
                            </button>
                        )}
                    </div>
                ))}

                <button
                    className="add-field-button"
                    type="button"
                    onClick={() => {
                        setFormFields([
                            ...formFields,
                            '',
                        ])
                    }}
                >
                    + Добавить вопрос
                </button>

                <button
                    className="create-event-button"
                    type="button"
                    onClick={handleCreateEvent}
                    disabled={creating}
                >
                    {creating
                        ? 'Создание...'
                        : 'Создать мероприятие'}
                </button>

                {message !== null && (
                    <p>{message}</p>
                )}
            </div>
        </main>
    )
}

export default CreateEventPage