package io.pjan.akka.ddd.identifier

trait AggregateId[T] extends EntityId {
  def value: T
}
