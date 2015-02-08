package io.pjan.akka.ddd.support

import akka.actor.Actor
import io.pjan.akka.ddd.EntityId
import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.message.CommandMessage


object CommandMessageHandler {
  type HandleCommandMessage[Id <: EntityId] = PartialFunction[CommandMessage[Id], Unit]

  def wildcardBehavior[Id <: EntityId]: HandleCommandMessage[Id] = {
    case _ => ()
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