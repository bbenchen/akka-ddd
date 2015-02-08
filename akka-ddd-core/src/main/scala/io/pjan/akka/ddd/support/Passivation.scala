package io.pjan.akka.ddd.support

import akka.actor.{ReceiveTimeout, Actor, PoisonPill}
import scala.concurrent.duration._


object Passivation {
  case class Passivate(message: Any)

  case class PassivationConfig(message: Any = PoisonPill, timeout: Duration = 1.hour)
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
      context.parent ! Passivate(passivationConfig.message)
  }

}