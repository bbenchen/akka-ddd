package io.pjan.akka.ddd

import akka.actor.Actor.Receive
import akka.actor._
import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.message.CommandMessage
import io.pjan.akka.ddd.support.Passivation.Passivate
import scala.reflect.ClassTag


object AggregateManager {
  def apply[T <: AggregateRoot[_, _]](implicit classTag: ClassTag[T], actorRefFactory: ActorRefFactory): ActorRef = {
    actorRefFactory.actorOf(Props(new AggregateManager[T]), name = classTag.runtimeClass.getSimpleName)
  }
}


class AggregateManager[AR <: AggregateRoot[_, _]](implicit arClassTag: ClassTag[AR]) extends Actor {
  override def receive: Receive = {
    case cmd: Command[_] =>
      val aggregate = context.child(resolve(cmd)) getOrElse
                      context.actorOf(Props(arClassTag.runtimeClass.asInstanceOf[Class[AR]]), resolve(cmd))

      aggregate forward CommandMessage(cmd)
    case Passivate(msg) =>
      sender ! msg
  }

  def resolve(cmd: Command[_]): String = cmd.aggregateId.toString
}
