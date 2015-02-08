package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.event.Event


object EventHandler {
  /**
   * Type alias representing a HandleEvent-expression.
   */
  type HandleEvent = PartialFunction[Event, Unit]

  /**
   * wildcardBehaviour is a HandleEvent-expression that matches all events, but does nothing
   */
  def wildcardBehavior: HandleEvent = {
    case _ => ()
  }

  /**
   * emptyBehavior is a HandleEvent-expression that matches no events at all, ever.
   */
  object emptyBehaviour extends HandleEvent {
    override def isDefinedAt(e: Event) = false
    override def apply(e: Event) = throw new UnsupportedOperationException("Empty behavior apply()")
  }
}

trait EventHandler {
  import EventHandler._
  def handleEvent: HandleEvent

  /**
   * INTERNAL API.
   *
   * Can be overridden to intercept calls to this AggregateRoot's current event handling behavior.
   *
   * @param handleEvent current event handling behavior.
   * @param event current event.
   */
  protected[ddd] def aroundHandleEvent(handleEvent: HandleEvent, event: Event): Unit =
    handleEvent.applyOrElse(event, unhandledEvent)

  /**
   * Overridable callback
   * <p/>
   * It is called when an event is not handled by the current event handler of the actor
   */
  def unhandledEvent(event: Event): Unit =
    EventHandler.wildcardBehavior.apply(event)
}