package io.pjan.akka.ddd.providers

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.contrib.pattern.ShardRegion.{Passivate, IdExtractor}
import io.pjan.akka.ddd.Domain.{ShardResolver, IdResolver}
import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.message.CommandMessage


class ClusterAggregateManagerProvider(system: ExtendedActorSystem) extends AggregateManagerProvider {
  override def getAMRef(
    system: ActorSystem,
    aggregateName: String,
    aggregateProps: Props,
    idResolver: IdResolver,
    shardResolver: Option[ShardResolver]): ActorRef = {
      val idExtractor: IdExtractor = {
        case msg if idResolver.isDefinedAt(msg) => (idResolver(msg).toString, wrapInCommandMessage(msg))
      }
      ClusterSharding(system).start(
        typeName = aggregateName,
        entryProps = Some(aggregateProps),
        idExtractor = idExtractor,
        shardResolver = shardResolver.get
      )
  }

  override def passivationMessage: Any = Passivate(PoisonPill)

  private def wrapInCommandMessage(msg: Any) = msg match {
    case cmdMsg: CommandMessage[_] => cmdMsg
    case cmd: Command[_]           => CommandMessage(cmd)
  }
}
