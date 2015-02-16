package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.identifier.EntityId
import io.pjan.akka.ddd.message.CommandMessage


object CommandMessageHandler {
  type HandleCommandMessage[Id <: EntityId] = PartialFunction[CommandMessage[Id], Unit]

  /**
   * wildcardBehaviour is a HandleCommandMessage-expression that matches all command messages, but does nothing
   */
  def wildcardBehavior[Id <: EntityId]: HandleCommandMessage[Id] = {
    case _ => ()
  }

  /**
   * emptyBehavior is a HandleCommandMessage-expression that matches no command messages at all, ever.
   */
  def emptyBehaviour[Id <: EntityId]: HandleCommandMessage[Id] = new HandleCommandMessage[Id] {
    override def isDefinedAt(cm: CommandMessage[Id]): Boolean = false
    override def apply(cm: CommandMessage[Id]): Unit = throw new UnsupportedOperationException("Empty behavior apply()")
  }
}

trait CommandMessageHandler[Id <: EntityId] extends CommandHandler[Id] {
  import CommandHandler._
  import CommandMessageHandler._
  def handleCommandMessage: HandleCommandMessage[Id]

  /**
   * INTERNAL API.
   *
   * Can be overridden to intercept calls to this AggregateRoot's current command message handling behavior.
   *
   * @param handleCommandMessage current command message handling behavior.
   * @param commandMessage current command message.
   */
  protected[ddd] def aroundHandleCommandMessage(handleCommandMessage: HandleCommandMessage[Id], commandMessage: CommandMessage[Id]): Unit =
    handleCommandMessage.applyOrElse(commandMessage, unhandledCommandMessage)

  /**
   * Overridable callback
   * <p/>
   * It is called when an command message is not handled by the current command message handler
   */
  def unhandledCommandMessage(commandMessage: CommandMessage[Id]): Unit =
    CommandMessageHandler.wildcardBehavior.apply(commandMessage)

  /**
   * @param handleCommandMessage a [[HandleCommandMessage]] instance
   * @param handleCommand a [[HandleCommand]] instance
   * @return a partialFunction that handles `EventMessage`s, and applies both the handleCommandMessage and the handleCommand, wrapped
   *         with their respective aroundHandleCommandMessage and aroundHandleCommand in scope.
   */
  def receiveCommandMessage(handleCommandMessage: HandleCommandMessage[Id])(handleCommand: HandleCommand[Id]): PartialFunction[Any, Unit] = {
    case cmdMsg: CommandMessage[Id] =>
      aroundHandleCommandMessage(handleCommandMessage, cmdMsg)
      aroundHandleCommand(handleCommand, cmdMsg.command)
  }
}