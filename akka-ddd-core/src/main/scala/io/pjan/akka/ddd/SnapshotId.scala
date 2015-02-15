package io.pjan.akka.ddd

import io.pjan.akka.ddd.identifier.EntityId

case class SnapshotId[Id <: EntityId](entityId: Id, sequenceNr: Long = 0)
