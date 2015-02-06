package io.pjan.akka.ddd

trait Entity[Id <: EntityId] {
  val id: Id

  override def hashCode: Int = id.hashCode

  override def equals(that: Any): Boolean = {
    that.isInstanceOf[Entity[Id]] &&
    this.id == that.asInstanceOf[Entity[Id]].id
  }
}