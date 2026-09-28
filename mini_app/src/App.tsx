import { useState } from 'react'
import CreateEventPage from './CreateEventPage'
import EventCreatedPage from './EventCreatedPage'
import EventPage from './EventPage'
import EventRegistrationsPage from './EventRegistrationsPage'

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

function getInitialEventId(): number | null {
  // Обычное открытие в браузере:
  // https://eva.chernushka.fun/?eventId=14
  const params = new URLSearchParams(window.location.search)
  const eventIdFromUrl = parseEventId(params.get('eventId'))

  if (eventIdFromUrl !== null) {
    return eventIdFromUrl
  }

  // Открытие через MAX:
  // https://max.ru/<bot>?startapp=event_14
  const startParam =
    window.WebApp?.initDataUnsafe.start_param

  if (startParam?.startsWith('event_')) {
    return parseEventId(
      startParam.substring('event_'.length)
    )
  }

  // create_event и отсутствие параметра
  // приводят на страницу создания.
  return null
}

function App() {
  const [createdEventId, setCreatedEventId] =
    useState<number | null>(null)

  const [openedEventId, setOpenedEventId] =
    useState<number | null>(getInitialEventId)

  const [registrationsEventId, setRegistrationsEventId] =
    useState<number | null>(null)

  if (registrationsEventId !== null) {
    return (
      <EventRegistrationsPage
        eventId={registrationsEventId}
      />
    )
  }

  if (openedEventId !== null) {
    return (
      <EventPage
        eventId={openedEventId}
      />
    )
  }

  if (createdEventId !== null) {
    return (
      <EventCreatedPage
        eventId={createdEventId}
        onOpenEvent={() => {
          setOpenedEventId(createdEventId)
        }}
        onOpenRegistrations={() => {
          setRegistrationsEventId(createdEventId)
        }}
      />
    )
  }

  return (
    <CreateEventPage
      onCreated={(eventId) => {
        setCreatedEventId(eventId)
      }}
    />
  )
}

export default App
