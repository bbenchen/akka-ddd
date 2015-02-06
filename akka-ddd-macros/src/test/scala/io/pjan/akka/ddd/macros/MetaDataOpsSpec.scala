package io.pjan.akka.ddd.macros

import org.scalacheck.{Arbitrary, Gen}
import Arbitrary._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpecLike}

class MetaDataOpsSpec extends WordSpecLike
                      with GeneratorDrivenPropertyChecks
                      with Matchers {

  case class MapMeta(name: String, metaData: Map[String, Any])
  case class AnyMeta(name: String, metaData: Any)

  implicit val anyArb = Arbitrary[Any](Gen.oneOf(arbitrary[Number], arbitrary[Int], arbitrary[Boolean]))

  implicit override val generatorDrivenConfig =
    PropertyCheckConfig(minSuccessful = 100)

  "MetaDataOps" should {
    "provide a withMetaData operation" which {
      "updates the object's metadata attribute correctly" when {
        "the metadata is of type Map[String, Any]" in {
          forAll { (n: String, m1: Map[String, Any], m2: Map[String, Any]) =>
            val startDummy = MapMeta(n, m1)
            val endDummy = MapMeta(n, m2)

            if (m1 != m2) startDummy should not equal (endDummy)
            MetaDataOps.withMetaData(startDummy, m2) should equal(endDummy)
            startDummy should equal (MapMeta(n, m1))
          }
        }
        "the metadata is of type Any" in {
          forAll { (n: String, m1: Any, m2: Any) =>
            val startDummy = AnyMeta(n, m1)
            val endDummy = AnyMeta(n, m2)

            if (m1 != m2) startDummy should not equal (endDummy)
            MetaDataOps.withMetaData(startDummy, m2) should equal(endDummy)
            startDummy should equal (AnyMeta(n, m1))
          }
        }
      }
    }
  }
}
