package io.pjan.akka.ddd.providers

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.contrib.pattern.ShardRegion.{Passivate, IdExtractor}
import io.pjan.akka.ddd.Domain.{ShardResolver, IdResolver}


class ClusterAggregateManagerProvider(system: ExtendedActorSystem) extends AggregateManagerProvider {
  override def getAMRef(
    system: ActorSystem,
    aggregateName: String,
    aggregateProps: Props,
    idResolver: IdResolver,
    shardResolver: Option[ShardResolver]): ActorRef = {
      val idExtractor: IdExtractor = {
        case msg if idResolver.isDefinedAt(msg) => (idResolver(msg).toString, msg)
      }
      ClusterSharding(system).start(
        typeName = aggregateName,
        entryProps = Some(aggregateProps),
        idExtractor = idExtractor,
        shardResolver = shardResolver.get
      )
  }

  override def passivationMessage: Any = Passivate(PoisonPill)
}
