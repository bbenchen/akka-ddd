package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.event.Event
import io.pjan.akka.ddd.identifier.EntityId
import io.pjan.akka.ddd.message.EventMessage


object EventMessageHandler {
  type HandleEventMessage[Id <: EntityId] = PartialFunction[EventMessage[Id], Unit]

  /**
   * wildcardBehaviour is a HandleEventMessage-expression that matches all event messages, but does nothing
   */
  def wildcardBehavior[Id <: EntityId]: HandleEventMessage[Id] = {
    case _ => ()
  }

  /**
   * emptyBehavior is a HandleEventMessage-expression that matches no event messages at all, ever.
   */
  def emptyBehaviour[Id <: EntityId]: HandleEventMessage[Id] = new HandleEventMessage[Id] {
    override def isDefinedAt(em: EventMessage[Id]): Boolean = false
    override def apply(em: EventMessage[Id]): Unit = throw new UnsupportedOperationException("Empty behavior apply()")
  }
}

trait EventMessageHandler[Id <: EntityId] extends EventHandler {
  import EventHandler._
  import EventMessageHandler._
  def handleEventMessage: HandleEventMessage[Id]

  /**
   * INTERNAL API.
   *
   * Can be overridden to intercept calls to this AggregateRoot's current event message handling behavior.
   *
   * @param handleEventMessage current event message handling behavior.
   * @param eventMessage current event message.
   */
  protected[ddd] def aroundHandleEventMessage(handleEventMessage: HandleEventMessage[Id], eventMessage: EventMessage[Id]): Unit =
    handleEventMessage.applyOrElse(eventMessage, unhandledEventMessage)

  /**
   * Overridable callback
   * <p/>
   * It is called when an event is not handled by the current event handler
   */
  def unhandledEventMessage(eventMessage: EventMessage[Id]): Unit =
    EventMessageHandler.wildcardBehavior.apply(eventMessage)

  /**
   * @param handleEventMessage a [[HandleEventMessage]] instance
   * @param handleEvent a [[HandleEvent]] instance
   * @return a partialFunction that handles `EventMessage`s, and applies both the handleEventMessage and the handleEvent, wrapped
   *         with their respective aroundHandleEventMessage and aroundHandleEvent in scope.
   */
  def receiveEventMessage(handleEventMessage: HandleEventMessage[Id])(handleEvent: HandleEvent): PartialFunction[Any, Unit] = {
    case evtMsg: EventMessage[Id] =>
      aroundHandleEventMessage(handleEventMessage, evtMsg)
      aroundHandleEvent(handleEvent, evtMsg.event)
  }

  def receiveEventMessage(handleEvent: HandleEvent): PartialFunction[Any, Unit] =
    receiveEventMessage(EventMessageHandler.wildcardBehavior[Id])(handleEvent)
}