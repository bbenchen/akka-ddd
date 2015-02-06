package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.EntityId
import io.pjan.akka.ddd.command.Command


object CommandHandler {
  /**
   * Type alias representing a HandleCommand-expression.
   */
  type HandleCommand[Id <: EntityId] = PartialFunction[Command[Id], Unit]

  /**
   * wildcardBehaviour is a HandleCommand-expression that matches all events, but does nothing
   */
  def wildcardBehavior[Id <: EntityId]: HandleCommand[Id] = {
    case _ => ()
  }

  /**
   * emptyBehavior is a HandleCommand-expression that matches no events at all, ever.
   */
  def emptyBehaviour[Id <: EntityId]: HandleCommand[Id] = new HandleCommand[Id] {
    override def isDefinedAt(c: Command[Id]): Boolean = false
    override def apply(c: Command[Id]): Unit = throw new UnsupportedOperationException("Empty behavior apply()")
  }
}

trait CommandHandler[Id <: EntityId] {
  import CommandHandler._
  def handleCommand: HandleCommand[Id]
}