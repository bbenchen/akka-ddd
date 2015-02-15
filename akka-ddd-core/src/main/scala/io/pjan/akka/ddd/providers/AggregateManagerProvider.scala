package io.pjan.akka.ddd.providers

import akka.actor._
import io.pjan.akka.ddd.Domain._


trait AggregateManagerProvider {
  def getAMRef(
    system: ActorSystem,
    aggregateName: String,
    aggregateProps: Props,
    idResolver: IdResolver,
    shardResolver: Option[ShardResolver] = None): ActorRef

  def passivationMessage: Any
}
