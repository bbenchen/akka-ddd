package io.pjan.akka.ddd.support

import akka.actor.{ReceiveTimeout, Actor}
import scala.concurrent.duration._


object Passivation {
  type PassivationMessage = Any

  case class PassivationConfig(message: PassivationMessage, timeout: Duration = 1.hour)
}

trait Passivation {
  this: Actor =>
  import Passivation._

  def passivationConfig: PassivationConfig

  def schedulePassivationTimeout(): Unit = {
    context.setReceiveTimeout(passivationConfig.timeout)
  }

  def handlePassivationTimeout: PartialFunction[Any, Unit] = {
    case ReceiveTimeout =>
      context.parent ! passivationConfig.message
  }
}
