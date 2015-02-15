package io.pjan.akka.ddd.identifier

import java.util.UUID


trait AggregateUuid extends AggregateId[UUID] {
  override def toString: String = value.toString
}
