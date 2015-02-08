package io.pjan.akka.ddd.support

import akka.actor.Actor
import io.pjan.akka.ddd.EntityId
import io.pjan.akka.ddd.event.Event
import io.pjan.akka.ddd.message.EventMessage

object EventMessageHandler {
  type HandleEventMessage[Id <: EntityId] = PartialFunction[EventMessage[Id], Unit]
}

trait EventMessageHandler[Id <: EntityId] extends EventHandler {
  import EventMessageHandler._
//  def handleEventMessage: HandleEventMessage[Id]

  def receiveEventMessage(emh: EventMessage[Id] => Unit)(eh: Event => Unit): Actor.Receive = {
    case eventMessage: EventMessage[Id] =>
      eh(eventMessage.event)
      emh(eventMessage)
  }
}