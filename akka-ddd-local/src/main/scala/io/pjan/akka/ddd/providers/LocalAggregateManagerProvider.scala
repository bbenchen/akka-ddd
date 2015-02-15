package io.pjan.akka.ddd.providers

import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import io.pjan.akka.ddd.Domain
import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.message.CommandMessage

import scala.concurrent.Await
import scala.concurrent.duration._


class LocalAggregateManagerProvider(system: ExtendedActorSystem) extends AggregateManagerProvider {
  import LocalGuardian._
  private val aggregateManagers: ConcurrentHashMap[String, ActorRef] = new ConcurrentHashMap
  private lazy val guardian: ActorRef = system.actorOf(Props[LocalGuardian], "local-guardian")

  def getAMRef(
    system: ActorSystem,
    aggregateName: String,
    aggregateProps: Props,
    idResolver: Domain.IdResolver,
    shardResolver: Option[Domain.ShardResolver] = None): ActorRef = {
      // TODO: specify the timeout, or get it from settings...
      implicit val timeout = Timeout(5000.millis)
      val startMessage = Start(aggregateName, aggregateProps, idResolver)
      val Started(aggregateManager) = Await.result(guardian ? startMessage, timeout.duration)
      aggregateManagers.put(aggregateName, aggregateManager)
      aggregateManager
  }

  val passivationMessage = LocalAggregateManager.Passivate(PoisonPill)
}

private[ddd] object LocalGuardian {
  case class Start(aggregateName: String, aggregateProps: Props, idResolver: Domain.IdResolver)
  case class Started(aggregateManager: ActorRef)
}

private[ddd] class LocalGuardian extends Actor {
  import LocalGuardian._

  override def receive: Actor.Receive = {
    case Start(aggregateName, aggregateProps, idResolver) =>
      val encAggregateName = URLEncoder.encode(aggregateName, "utf-8")
      val aggregateManager = context.child(encAggregateName).getOrElse {
        context.actorOf(LocalAggregateManager.props(encAggregateName, aggregateProps, idResolver), encAggregateName)
      }
      sender() ! Started(aggregateManager)

  }

  // TODO: check whether the default supervision for this is ok
}

object LocalAggregateManager {

  /**
   * Factory method for the [[akka.actor.Props]] of the [[LocalAggregateManager]] actor.
   */
  def props(
    aggregateName: String,
    aggregateProps: Props,
    idResolver: Domain.IdResolver): Props =
    Props(classOf[LocalAggregateManager], aggregateName, aggregateProps, idResolver)

  sealed trait LocalAggregateManagerCommand

  @SerialVersionUID(1L) case class Passivate(stopMessage: Any) extends LocalAggregateManagerCommand
}

class LocalAggregateManager(
    aggregateName: String,
    aggregateProps: Props,
    idResolver: Domain.IdResolver
  ) extends Actor with ActorLogging {
  import LocalAggregateManager._

  def receive: Actor.Receive = {
    case Terminated(aggregate)              => receiveTerminated(aggregate)
    case cmd: LocalAggregateManagerCommand  => receiveCommand(cmd)
    case msg if idResolver.isDefinedAt(msg) => deliverMessage(msg, sender())
  }

  def receiveTerminated(aggregate: ActorRef): Unit = {
    log.debug("Aggregate {} stopped", aggregate)
  }

  def receiveCommand(cmd: LocalAggregateManagerCommand): Unit = cmd match {
    case Passivate(stopMessage) => passivate(sender(), stopMessage)
  }

  def deliverMessage(msg: Any, sender: ActorRef): Unit = {
    val id = idResolver(msg)
    if (id == null || id.toString == "") {
      log.debug("The id must not be empty. Dropping message {}", msg)
      context.system.deadLetters ! msg
    } else {
      val encIdString = URLEncoder.encode(id.toString, "utf-8")
      val aggregate = context.child(encIdString).getOrElse {
        log.debug("Starting aggregate [{}/{}]", aggregateName, encIdString)
        context.watch(context.actorOf(aggregateProps, encIdString))
      }
      log.debug("Message [{}] sent to aggregate [{}/{}]", msg, aggregateName, encIdString)
      msg match {
        case cmd: Command[_]               => aggregate.tell(CommandMessage(cmd), sender)
        case cmdMessage: CommandMessage[_] => aggregate.tell(cmdMessage, sender)
      }
      aggregate.tell(msg, sender)
    }
  }

  def passivate(aggregate: ActorRef, stopMessage: Any): Unit = {
    log.debug("Passivating aggregate {}", aggregate)
    aggregate ! stopMessage
  }
}
