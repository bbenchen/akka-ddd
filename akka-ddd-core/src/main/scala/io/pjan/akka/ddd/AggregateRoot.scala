package io.pjan.akka.ddd

import akka.actor.{ReceiveTimeout, Actor}
import akka.actor.Status.Failure
import akka.persistence.PersistentActor
import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.event.Event
import io.pjan.akka.ddd.message._
import io.pjan.akka.ddd.state.AggregateState
import io.pjan.akka.ddd.support.Passivation.{Passivate, PassivationConfig}
import io.pjan.akka.ddd.support._

import scala.concurrent.duration._

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
   * Type alias representing a HandleCommand-expression for an AggregateRoot.
   */
  type HandleCommand[Id <: AggregateId[_]] = CommandHandler.HandleCommand[Id]

  /**
   * Type alias representing a Transition-expression for an AggregateRoot.
   */
  type Transition[Id <: AggregateId[_]] = StateTransitions.Transition[Id]
}

trait AggregateRoot[Id <: AggregateId[_], State <: AggregateState] extends PersistentActor
      with Entity[Id]
      with AggregateStateKeeper[State]
      with CommandHandler[Id]
      with EventHandler
      with StateTransitions[Id]
      with Passivation {

  type InitializeState = AggregateRoot.InitializeState[State]
  type HandleEvent     = AggregateRoot.HandleEvent
  type HandleCommand   = AggregateRoot.HandleCommand[Id]
  type Transition      = AggregateRoot.Transition[Id]

  def initializeState: InitializeState

  /**
   * This defines the AggregateRoot's event handling behavior.
   * It must return a partial function with the event handling logic,
   * and defaults to a wildcard behavior.
   */
  def handleEvent: HandleEvent = EventHandler.wildcardBehavior

  /**
   * This defines the AggregateRoot's command handling behavior.
   * It must return a partial function with the command handling logic.
   */
  def handleCommand: HandleCommand

  /**
   * This defines the AggregateRoot's event-based command handling transitions.
   * It must return a partial function with the event-based command handling transitions.
   */
  def transition: Transition = StateTransitions.emptyBehavior[Id]

  def passivationConfig: PassivationConfig

  def materialize(event: Event): Unit = {
    persistAsEventMessage(event){
      persistedEventMessage => {
        updateState(event)
        becomeByTransition(event)
        aroundHandleEvent(handleEvent, event)
        // todo: there should be a callback for the persistedEventMessage as well.
      }
    }
  }

  def persistAsEventMessage(event: Event)(cb: EventMessage[Id] => Unit): Unit =
    persist(toEventMessage(event))(cb)

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
    toHandleCommandMessage(handleCommand)
  }

  override def receiveRecover: Actor.Receive = {
    case eventMessage: EventMessage[Id] =>
      recoverState(eventMessage.event)
  }

  override def unhandled(msg: Any): Unit =
    handlePassivationTimeout.applyOrElse(msg, super.unhandled)

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
   * Can be overridden to intercept calls to this AggregateRoot's current command handling behavior.
   *
   * @param handleCommand current event handling behavior.
   * @param command current command.
   */
  protected[ddd] def aroundHandleCommand(handleCommand: HandleCommand, command: Command[Id]): Unit =
    handleCommand.applyOrElse(command, unhandledCommand)

  protected final def become(handleCommand: HandleCommand): Unit =
    context.become(toHandleCommandMessage(handleCommand.asInstanceOf[Actor.Receive]))

  protected final def becomeByTransition(event: Event): Unit =
    if (transition.isDefinedAt(event)) become(transition.apply(event))


  protected final def updateState(event: Event): Unit =
    _state = Some(_state.fold(initializeState(event))(_.apply(event).asInstanceOf[State]))

  protected final def recoverState(event: Event): Unit = {
    updateState(event)
    becomeByTransition(event)
  }

  private var _lastCommandMessage: Option[CommandMessage[Id]] = None

  protected final def updateLastCommandMessage(commandMessage: CommandMessage[Id]): Unit =
    _lastCommandMessage = Some(commandMessage)

  protected def toEventMessage(event: Event): EventMessage[Id] =
    _lastCommandMessage.fold(EventMessage[Id](event, Some(SnapshotId(id, lastSequenceNr))))(cmd => EventMessage(event, Some(SnapshotId(id, lastSequenceNr))).withMetaData(cmd.metaData))

  protected def toHandleCommandMessage(handleCommand: HandleCommand): Actor.Receive = {
    case commandMessage: CommandMessage[Id] =>
      updateLastCommandMessage(commandMessage)
      aroundHandleCommand(handleCommand, commandMessage.command)
  }

  /**
   * Overridable callback
   * <p/>
   * It is called when an event is not handled by the current event handler of the actor
   */
  def unhandledEvent(event: Event): Unit =
    EventHandler.wildcardBehavior.apply(event)

  /**
   * Overridable callback
   * <p/>
   * It is called when a command is not handled by the current command handler of the actor
   */
  def unhandledCommand(command: Command[Id]): Unit =
    CommandHandler.wildcardBehavior.apply(command)

}
