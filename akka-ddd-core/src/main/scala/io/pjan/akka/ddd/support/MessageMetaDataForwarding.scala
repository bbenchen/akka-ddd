package io.pjan.akka.ddd.support

import java.util.UUID
import java.time.ZonedDateTime

import io.pjan.akka.ddd.{SnapshotId, EntityId}
import io.pjan.akka.ddd.event.Event
import io.pjan.akka.ddd.message.{EventMessage, CommandMessage}

trait MessageMetaDataForwarding[Id <: EntityId] {
  this: CommandMessageHandler[Id] with EventMessageHandler[Id] =>

  private var _lastCommandMessage: Option[CommandMessage[Id]] = None

  protected final def updateLastCommandMessage(commandMessage: CommandMessage[Id]): Unit =
    _lastCommandMessage = Some(commandMessage)

  protected def forwardedMetaDataEventMessage(
      event: Event,
      snapshotId: Option[SnapshotId[Id]] = None,
      identifier: String = UUID.randomUUID().toString,
      timestamp: ZonedDateTime = ZonedDateTime.now): EventMessage[Id] = {
    _lastCommandMessage.fold(EventMessage[Id](event, snapshotId, identifier, timestamp))(cmd => EventMessage(event, snapshotId, identifier, timestamp).withMetaData(cmd.metaData))
  }
}
