package io.pjan.akka.ddd.message

import io.pjan.akka.ddd.identifier.EntityId


object Message {
  type MetaData = Map[String, Any]
}

trait Message[M <: Message[M]] extends Serializable { self =>
  import Message._

  def payload: Any

  val metaData: MetaData = Map.empty[String, Any]

  def withMetaData(m: MetaData): M
  def addMetaData(m: MetaData): M = withMetaData(metaData ++ m)
  def addMetaData(m: (String, Any)): M = withMetaData(metaData + m)
  def withoutMetaData(): M = withMetaData(Map.empty[String, Any])
}

trait EntityMessage[Id <: EntityId, M <: EntityMessage[Id, M]] extends Message[M] {
  def entityId: Id
}
