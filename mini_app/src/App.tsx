import { useState } from 'react'
import CreateEventPage from './CreateEventPage'
import EventCreatedPage from './EventCreatedPage'
import EventPage from './EventPage'

function App() {
  const params = new URLSearchParams(window.location.search)
  const eventIdFromUrl = params.get('eventId')

  const parsedEventId =
    eventIdFromUrl !== null ? Number(eventIdFromUrl) : null

  const initialEventId =
    parsedEventId !== null &&
      Number.isInteger(parsedEventId) &&
      parsedEventId > 0
      ? parsedEventId
      : null

  const [createdEventId, setCreatedEventId] =
    useState<number | null>(null)

  const [openedEventId, setOpenedEventId] =
    useState<number | null>(initialEventId)

  if (openedEventId !== null) {
    return <EventPage eventId={openedEventId} />
  }

  if (createdEventId !== null) {
    return (
      <EventCreatedPage
        eventId={createdEventId}
        onOpenEvent={() => {
          setOpenedEventId(createdEventId)
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