package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.event.Event
import io.pjan.akka.ddd.identifier.EntityId


object StateTransitions {
  /**
   * Type alias representing a HandleEvent-expression for an AggregateState.
   */
  type Transition[Id <: EntityId] = PartialFunction[Event, CommandHandler.HandleCommand[Id]]

  /**
   * emptyBehavior is a Evolution-expression that matches no events at all, ever.
   */
  def emptyBehavior[Id <: EntityId]: Transition[Id] = new Transition[Id] {
    override def isDefinedAt(c: Event): Boolean = false
    override def apply(c: Event): CommandHandler.HandleCommand[Id] = throw new UnsupportedOperationException("Empty behavior apply()")
  }
}

trait StateTransitions[Id <: EntityId] {
  import StateTransitions._

  def transition: Transition[Id]
}