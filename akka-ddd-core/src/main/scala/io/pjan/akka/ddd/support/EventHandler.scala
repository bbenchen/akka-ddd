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
}