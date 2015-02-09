package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.EntityId
import io.pjan.akka.ddd.command.Command
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
  import CommandMessageHandler._
  def handleCommandMessage: HandleCommandMessage[Id]

  protected[ddd] def aroundHandleCommandMessage(handleCommandMessage: HandleCommandMessage[Id], commandMessage: CommandMessage[Id]): Unit =
    handleCommandMessage.applyOrElse(commandMessage, unhandledCommandMessage)

  def unhandledCommandMessage(commandMessage: CommandMessage[Id]): Unit =
    CommandMessageHandler.wildcardBehavior.apply(commandMessage)

  def receiveCommandMessage(cmh: CommandMessage[Id] => Unit)(ch: Command[Id] => Unit): PartialFunction[Any, Unit] = {
    case commandMessage: CommandMessage[Id] =>
      ch(commandMessage.command)
      cmh(commandMessage)
  }
}