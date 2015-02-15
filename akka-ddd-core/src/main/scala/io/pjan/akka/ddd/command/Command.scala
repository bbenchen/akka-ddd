package io.pjan.akka.ddd.command

import io.pjan.akka.ddd.identifier.EntityId


trait Command[Id <: EntityId] extends Serializable {
  def aggregateId: Id
}

