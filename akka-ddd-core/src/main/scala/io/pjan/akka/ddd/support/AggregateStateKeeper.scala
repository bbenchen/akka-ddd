package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.event.Event
import io.pjan.akka.ddd.state.AggregateState


class AggregateStateNotInitializedException(message: String, cause: Throwable) extends RuntimeException(message, cause) with Serializable {
  def this(msg: String) = this(msg, null)
  def this() = this("", null)
}

object AggregateStateKeeper {
  type InitializeState[State <: AggregateState] = PartialFunction[Event, State]
}

trait AggregateStateKeeper[State <: AggregateState] {
  import AggregateStateKeeper._

  def initializeState: InitializeState[State]

  private[ddd] var _state: Option[State] = None

  protected final def isInitialized: Boolean = _state.isDefined

  protected final def state: State = {
    _state.getOrElse(throw new AggregateStateNotInitializedException(s"${this.getClass.getSimpleName} has not been initialized."))
  }

  protected final def setState(state: State): Unit = {
    _state = Some(state)
  }
}
