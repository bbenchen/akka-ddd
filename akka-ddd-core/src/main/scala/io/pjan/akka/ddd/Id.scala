package io.pjan.akka.ddd

import java.util.UUID

trait Id

trait EntityId extends Id

trait AggregateId[T] extends EntityId {
  def value: T
}

trait AggregateUuid extends AggregateId[UUID] {
  override def toString: String = value.toString
}
