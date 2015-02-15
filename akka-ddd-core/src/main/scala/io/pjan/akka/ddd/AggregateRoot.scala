package io.pjan.akka.ddd

import akka.actor.Actor
import akka.actor.Status.Failure
import akka.persistence.PersistentActor
import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.event.Event
import io.pjan.akka.ddd.identifier.AggregateId
import io.pjan.akka.ddd.message._
import io.pjan.akka.ddd.state.AggregateState
import io.pjan.akka.ddd.support.Passivation.PassivationConfig
import io.pjan.akka.ddd.support._


object AggregateRoot {
  /**
   * Type alias representing a Initialize-expression for an AggregateRoot.
   */
  type InitializeState[State <: AggregateState] = AggregateStateKeeper.InitializeState[State]

  /**
   * Type alias representing a HandleEvent-expression for an AggregateRoot.
   */
  type HandleEvent = EventHandler.HandleEvent

  /**
   * Type alias representing a HandleEventMessage-expression for an AggregateRoot.
   */
  type HandleEventMessage[Id <: AggregateId[_]] = EventMessageHandler.HandleEventMessage[Id]

  /**
   * Type alias representing a HandleCommand-expression for an AggregateRoot.
   */
  type HandleCommand[Id <: AggregateId[_]] = CommandHandler.HandleCommand[Id]

  /**
   * Type alias representing a HandleCommandMessage-expression for an AggregateRoot.
   */
  type HandleCommandMessage[Id <: AggregateId[_]] = CommandMessageHandler.HandleCommandMessage[Id]

  /**
   * Type alias representing a Transition-expression for an AggregateRoot.
   */
  type Transition[Id <: AggregateId[_]] = StateTransitions.Transition[Id]
}

trait AggregateRoot[Id <: AggregateId[_], State <: AggregateState] extends PersistentActor
      with Entity[Id]
      with AggregateStateKeeper[State]
      with CommandMessageHandler[Id]
      with EventMessageHandler[Id]
      with MessageMetaDataForwarding[Id]
      with StateTransitions[Id]
      with Passivation {

  type InitializeState      = AggregateRoot.InitializeState[State]
  type HandleEvent          = AggregateRoot.HandleEvent
  type HandleEventMessage   = AggregateRoot.HandleEventMessage[Id]
  type HandleCommand        = AggregateRoot.HandleCommand[Id]
  type HandleCommandMessage = AggregateRoot.HandleCommandMessage[Id]
  type Transition           = AggregateRoot.Transition[Id]

  def initializeState: InitializeState

  /**
   * This defines the AggregateRoot's event handling behavior.
   * It must return a partial function with the event handling logic,
   * and defaults to a wildcard behavior.
   */
  def handleEvent: HandleEvent = EventHandler.wildcardBehavior

  /**
   * This defines the AggregateRoot's event message handling behavior.
   * It must return a partial function with the event message handling logic,
   * and defaults to a behavior where it updates the entity's state,
   * changes its command handling behaviour based on the Transition,
   * and calls the handleEvent behavior on the encapsulated event.
   */
  def handleEventMessage: HandleEventMessage =
    receiveEventMessage(EventMessageHandler.wildcardBehavior){ e: Event =>
      updateState(e)
      becomeByTransition(e)
      aroundHandleEvent(handleEvent, e)
    }

  /**
   * This defines the AggregateRoot's command handling behavior.
   * It must return a partial function with the command handling logic.
   */
  def handleCommand: HandleCommand

  /**
   * This defines the AggregateRoot's command message handling behavior.
   * It must return a partial function with the command message handling logic,
   * and defaults to a behaviour where it updates the internal cached last command message,
   * and calls the handleCommand behavior on the encapsulated event.
   */
  def handleCommandMessage: HandleCommandMessage =
    receiveCommandMessage(updateLastCommandMessage){ c: Command[Id] =>
      aroundHandleCommand(handleCommand, c)
    }

  /**
   * This defines the AggregateRoot's event-based command handling transitions.
   * It must return a partial function with the event-based command handling transitions.
   */
  def transition: Transition = StateTransitions.emptyBehavior[Id]

  def passivationConfig: PassivationConfig

  def materialize(event: Event): Unit = {
    persistAsEventMessage(event) {
      e: EventMessage[Id] => handleEventMessage(e)
    }
  }

  private def persistAsEventMessage(event: Event)(cb: EventMessage[Id] => Unit): Unit =
    persist(forwardedMetaDataEventMessage(event, Some(SnapshotId(id, lastSequenceNr))))(cb)

  override def persistenceId = s"${this.getClass.getSimpleName}-${id.value}"

  override def preStart() = {
    super.preStart()
    schedulePassivationTimeout()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    sender ! Failure(reason)
    super.preRestart(reason, message)
  }

  override def receiveCommand: Actor.Receive = {
    handleCommandMessage.asInstanceOf[Actor.Receive]
  }

  override def receiveRecover: Actor.Receive = {
    receiveEventMessage(EventMessageHandler.wildcardBehavior){ e: Event =>
      updateState(e)
      becomeByTransition(e)
    }
  }

  override def unhandled(msg: Any): Unit =
    handlePassivationTimeout.applyOrElse(msg, super.unhandled)

  protected final def become(handleCommand: HandleCommand): Unit = context.become(
    receiveCommandMessage(updateLastCommandMessage){ c: Command[Id] =>
      aroundHandleCommand(handleCommand, c)
    })

  protected final def becomeByTransition(event: Event): Unit =
    if (transition.isDefinedAt(event)) become(transition.apply(event))

  protected final def updateState(event: Event): Unit =
    if (isInitialized) setState(state.apply(event).asInstanceOf[State])
    else setState(initializeState(event))
}
