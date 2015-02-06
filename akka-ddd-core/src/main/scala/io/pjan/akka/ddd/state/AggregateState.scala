package io.pjan.akka.ddd.state

import io.pjan.akka.ddd.event.Event

object AggregateState {
  /**
   * Type alias representing a Register-expression for an AggregateState.
   */
  type StateTransitions = PartialFunction[Event, AggregateState]
}

trait AggregateState {
  import AggregateState._

  def apply: StateTransitions
}