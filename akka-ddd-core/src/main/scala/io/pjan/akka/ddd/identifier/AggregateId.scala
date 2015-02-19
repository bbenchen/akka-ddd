package io.pjan.akka.ddd.identifier

trait AggregateId extends EntityId {
  def value: String
  override def toString = value
}
