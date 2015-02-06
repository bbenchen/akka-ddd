package io.pjan.akka.ddd

case class SnapshotId[Id <: EntityId](entityId: Id, sequenceNr: Long = 0)
