package io.pjan.akka.ddd.message

import java.time.ZonedDateTime
import java.util.UUID
import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.identifier.EntityId
import io.pjan.akka.ddd.macros.MetaDataOps
import io.pjan.akka.ddd.message.Message.MetaData


case class CommandMessage[Id <: EntityId] (
    command: Command[Id],
    identifier: String = UUID.randomUUID().toString,
    timestamp: ZonedDateTime = ZonedDateTime.now,
    override val metaData: MetaData = Map.empty[String, Any]
  ) extends EntityMessage[Id, CommandMessage[Id]] {
  override def entityId: Id = aggregateId

  def aggregateId: Id = command.aggregateId

  override def payload: Any = command

  override def toString: String = {
    val msgClass = this.getClass.getSimpleName
    s"$msgClass($command, $identifier, $timestamp, $metaData)"
  }

  def withMetaData(m: Message.MetaData): CommandMessage[Id] = MetaDataOps.withMetaData(this, m)
}
