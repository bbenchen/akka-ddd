package io.pjan.akka.ddd

import io.pjan.akka.ddd.identifier.EntityId
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpecLike}

class EntitySpec extends WordSpecLike
                 with Matchers
                 with MockFactory {

  case class DummyId(value: Int) extends EntityId

  case class DummyEntity(id: DummyId, name: String) extends Entity[DummyId]

  "Entities" when {
    "having the same id" should {
      "in comparison be evaluated as equal" in {
        val id = DummyId(1)
        val d1 = DummyEntity(id, "foo")
        val d2 = DummyEntity(id, "bar")
        d1 should equal (d2)
      }
    }
    "having different ids" should {
      "in comparison be evaluated as not equal" in {
        val id1 = DummyId(1)
        val id2 = DummyId(2)
        val d1 = DummyEntity(id1, "foo")
        val d2 = DummyEntity(id2, "foo")
        d1 should not equal (d2)
      }
    }
  }

}
