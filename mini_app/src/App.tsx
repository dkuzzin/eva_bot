import { useState } from 'react'
import CreateEventPage from './CreateEventPage'
import EventCreatedPage from './EventCreatedPage'
import EventPage from './EventPage'
import EventRegistrationsPage from './EventRegistrationsPage'
import MyEventsPage from './MyEventsPage'
import MyRegistrationsPage from './MyRegistrationsPage'

function parseEventId(value: string | null): number | null {
  if (value === null) {
    return null
  }

  const eventId = Number(value)

  if (!Number.isInteger(eventId) || eventId <= 0) {
    return null
  }

  return eventId
}

function getStartParam(): string | undefined {
  return window.WebApp?.initDataUnsafe.start_param
}

function getInitialEventId(): number | null {
  // Обычное открытие в браузере:
  // https://eva.chernushka.fun/?eventId=14
  const params = new URLSearchParams(window.location.search)
  const eventIdFromUrl = parseEventId(params.get('eventId'))

  if (eventIdFromUrl !== null) {
    return eventIdFromUrl
  }

  // Открытие конкретного мероприятия через MAX:
  // https://max.ru/<bot>?startapp=event_14
  const startParam = getStartParam()

  if (startParam?.startsWith('event_')) {
    return parseEventId(
      startParam.substring('event_'.length)
    )
  }

  return null
}

function App() {
  const startParam = getStartParam()

  const [createdEventId, setCreatedEventId] =
    useState<number | null>(null)

  const [openedEventId, setOpenedEventId] =
    useState<number | null>(getInitialEventId)

  const [registrationsEventId, setRegistrationsEventId] =
    useState<number | null>(null)

  const [showMyEvents, setShowMyEvents] =
    useState(startParam === 'my_events')

  const [showMyRegistrations, setShowMyRegistrations] =
    useState(startParam === 'my_registrations')

  // Если "Мои регистрации" были открыты со страницы
  // конкретного мероприятия, здесь запоминаем его id,
  // чтобы кнопка "Назад" вернула именно туда.
  const [
    myRegistrationsBackEventId,
    setMyRegistrationsBackEventId,
  ] = useState<number | null>(null)

  // Нужно для сценария:
  // Мои регистрации -> Открыть мероприятие -> Назад
  const [
    eventOpenedFromMyRegistrations,
    setEventOpenedFromMyRegistrations,
  ] = useState(false)

  if (registrationsEventId !== null) {
    return (
      <EventRegistrationsPage
        eventId={registrationsEventId}
        onBack={() => {
          setRegistrationsEventId(null)
          setShowMyEvents(true)
        }}
      />
    )
  }

  if (showMyRegistrations) {
    return (
      <MyRegistrationsPage
        onOpenEvent={(eventId) => {
          setShowMyRegistrations(false)
          setEventOpenedFromMyRegistrations(true)
          setOpenedEventId(eventId)
        }}
        onBack={() => {
          setShowMyRegistrations(false)

          if (myRegistrationsBackEventId !== null) {
            setOpenedEventId(myRegistrationsBackEventId)
            setMyRegistrationsBackEventId(null)
          }
        }}
      />
    )
  }

  if (showMyEvents) {
    return (
      <MyEventsPage
        onOpenRegistrations={(eventId) => {
          setShowMyEvents(false)
          setRegistrationsEventId(eventId)
        }}
        onCreateEvent={() => {
          setCreatedEventId(null)
          setOpenedEventId(null)
          setRegistrationsEventId(null)
          setShowMyEvents(false)
        }}
      />
    )
  }

  if (openedEventId !== null) {
    return (
      <EventPage
        eventId={openedEventId}
        onBack={
          eventOpenedFromMyRegistrations
            ? () => {
              setOpenedEventId(null)
              setEventOpenedFromMyRegistrations(false)
              setShowMyRegistrations(true)
            }
            : undefined
        }
        onOpenMyRegistrations={() => {
          setMyRegistrationsBackEventId(openedEventId)
          setOpenedEventId(null)
          setEventOpenedFromMyRegistrations(false)
          setShowMyRegistrations(true)
        }}
      />
    )
  }

  if (createdEventId !== null) {
    return (
      <EventCreatedPage
        eventId={createdEventId}
        onOpenEvent={() => {
          setOpenedEventId(createdEventId)
          setCreatedEventId(null)
        }}
        onOpenRegistrations={() => {
          setRegistrationsEventId(createdEventId)
          setCreatedEventId(null)
        }}
        onOpenMyEvents={() => {
          setCreatedEventId(null)
          setShowMyEvents(true)
        }}
      />
    )
  }

  return (
    <CreateEventPage
      onCreated={(eventId) => {
        setCreatedEventId(eventId)
      }}
      onOpenMyEvents={() => {
        setShowMyEvents(true)
      }}
      onOpenMyRegistrations={() => {
        setShowMyRegistrations(true)
      }}
    />
  )
}

export default App