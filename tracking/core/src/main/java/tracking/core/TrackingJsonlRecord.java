package tracking.core;

import java.util.Objects;

/** One typed record in a self-contained local tracking outbox. */
public sealed interface TrackingJsonlRecord {
  /**
   * The first record in every outbox.
   *
   * @param session persisted session descriptor
   */
  record Session(TrackingSessionDescriptor session) implements TrackingJsonlRecord {
    /**
     * Requires a session descriptor.
     *
     * @param session persisted session descriptor
     */
    public Session {
      session = Objects.requireNonNull(session, "session");
    }
  }

  /**
   * One ordered tracking event.
   *
   * @param event persisted event
   */
  record Event(TrackingEvent event) implements TrackingJsonlRecord {
    /**
     * Requires an event.
     *
     * @param event persisted event
     */
    public Event {
      event = Objects.requireNonNull(event, "event");
    }
  }

  /**
   * The optional terminal record in a cleanly closed outbox.
   *
   * @param finish persisted terminal state
   */
  record Finish(TrackingSessionFinish finish) implements TrackingJsonlRecord {
    /**
     * Requires a finish.
     *
     * @param finish persisted terminal state
     */
    public Finish {
      finish = Objects.requireNonNull(finish, "finish");
    }
  }
}
