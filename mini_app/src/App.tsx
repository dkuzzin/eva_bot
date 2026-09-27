import { useState } from 'react'
import CreateEventPage from './CreateEventPage'
import EventPage from './EventPage'

function App() {
  const [createdEventId, setCreatedEventId] =
    useState<number | null>(null)

  if (createdEventId !== null) {
    return <EventPage eventId={createdEventId} />
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