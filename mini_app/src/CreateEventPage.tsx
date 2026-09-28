import './CreateEventPage.css'
import { useState } from 'react'

type CreateEventPageProps = {
    onCreated: (eventId: number) => void
}

function CreateEventPage({ onCreated }: CreateEventPageProps) {
    const [title, setTitle] = useState('')
    const [description, setDescription] = useState('')
    const [startsAt, setStartsAt] = useState('')
    const [endsAt, setEndsAt] = useState('')
    const [location, setLocation] = useState('')
    const [capacity, setCapacity] = useState('')
    const [formFields, setFormFields] = useState<string[]>([''])
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

        if (formFields.some((field) => field.trim() === '')) {
            setMessage('Заполните все вопросы формы регистрации')
            return
        }

        setCreating(true)
        setMessage(null)

        const request = {
            title,
            description,
            startsAt: new Date(startsAt).toISOString(),
            endsAt: endsAt === '' ? null : new Date(endsAt).toISOString(),
            location: location === '' ? null : location,
            capacity: capacity === '' ? null : Number(capacity),
            formFields: formFields.map((label) => ({
                label,
            })),
        }

        try {
            const response = await fetch('/api/events', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
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
                <h1>Создать мероприятие</h1>

                <div>
                    <label htmlFor="title">
                        Название
                    </label>

                    <input
                        id="title"
                        type="text"
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                    />
                </div>

                <div>
                    <label htmlFor="description">
                        Описание
                    </label>

                    <textarea
                        id="description"
                        value={description}
                        onChange={(event) => setDescription(event.target.value)}
                    />
                </div>

                <div>
                    <label htmlFor="startsAt">Начало</label>

                    <input
                        id="startsAt"
                        type="datetime-local"
                        value={startsAt}
                        onChange={(event) => setStartsAt(event.target.value)}
                    />
                </div>

                <div>
                    <label htmlFor="endsAt">Окончание</label>

                    <input
                        id="endsAt"
                        type="datetime-local"
                        value={endsAt}
                        onChange={(event) => setEndsAt(event.target.value)}
                    />
                </div>

                <div>
                    <label htmlFor="location">Место</label>

                    <input
                        id="location"
                        type="text"
                        value={location}
                        onChange={(event) => setLocation(event.target.value)}
                    />
                </div>

                <div>
                    <label htmlFor="capacity">Количество мест</label>

                    <input
                        id="capacity"
                        type="number"
                        min="1"
                        value={capacity}
                        onChange={(event) => setCapacity(event.target.value)}
                    />
                </div>

                <h2>Форма регистрации</h2>

                {formFields.map((field, index) => (
                    <div key={index}>
                        <label htmlFor={`form-field-${index}`}>
                            Вопрос {index + 1}
                        </label>

                        <input
                            id={`form-field-${index}`}
                            type="text"
                            value={field}
                            onChange={(event) => {
                                const newFormFields = [...formFields]
                                newFormFields[index] = event.target.value
                                setFormFields(newFormFields)
                            }}
                        />

                        {formFields.length > 1 && (
                            <button
                                type="button"
                                onClick={() => {
                                    setFormFields(
                                        formFields.filter(
                                            (_, fieldIndex) => fieldIndex !== index
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
                        setFormFields([...formFields, ''])
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
                    {creating ? 'Создание...' : 'Создать мероприятие'}
                </button>

                {message !== null && (
                    <p>{message}</p>
                )}
            </div>
        </main>
    )
}

export default CreateEventPage