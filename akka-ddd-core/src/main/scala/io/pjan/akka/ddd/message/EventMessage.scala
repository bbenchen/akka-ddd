package io.pjan.akka.ddd.message

import java.time.ZonedDateTime
import java.util.UUID

import io.pjan.akka.ddd.identifier.EntityId
import io.pjan.akka.ddd.SnapshotId
import io.pjan.akka.ddd.event.Event
import io.pjan.akka.ddd.macros.MetaDataOps
import io.pjan.akka.ddd.message.Message.MetaData


case class EventMessage[Id <: EntityId] (
    event: Event,
    snapshotId: Option[SnapshotId[Id]] = None,
    identifier: String = UUID.randomUUID().toString,
    timestamp: ZonedDateTime = ZonedDateTime.now,
    override val metaData: MetaData = Map.empty[String, Any]
  ) extends EntityMessage[Id, EventMessage[Id]] {
  override def entityId: Id = aggregateId.get

  def aggregateId: Option[Id] = snapshotId.map(_.entityId)

  override def payload: Any = event

  override def toString: String = {
    val msgClass = this.getClass.getSimpleName
    s"$msgClass($event, $snapshotId, $identifier, $timestamp, $metaData)"
  }

  def withMetaData(m: MetaData): EventMessage[Id] = MetaDataOps.withMetaData(this, m)
}
