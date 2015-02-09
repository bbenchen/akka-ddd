package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.EntityId
import io.pjan.akka.ddd.event.Event
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
  import EventMessageHandler._
  def handleEventMessage: HandleEventMessage[Id]

  protected[ddd] def aroundHandleEventMessage(handleEventMessage: HandleEventMessage[Id], eventMessage: EventMessage[Id]): Unit =
    handleEventMessage.applyOrElse(eventMessage, unhandledEventMessage)

  def unhandledEventMessage(eventMessage: EventMessage[Id]): Unit =
    EventMessageHandler.wildcardBehavior.apply(eventMessage)

  def receiveEventMessage(emh: EventMessage[Id] => Unit)(eh: Event => Unit): PartialFunction[Any, Unit] = {
    case eventMessage: EventMessage[Id] =>
      eh(eventMessage.event)
      emh(eventMessage)
  }
}