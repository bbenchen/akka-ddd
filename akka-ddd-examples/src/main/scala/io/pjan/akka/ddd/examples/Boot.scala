package io.pjan.akka.ddd.examples

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import io.pjan.akka.ddd._

object Boot extends App {

  implicit val system = ActorSystem("AccessSystem", ConfigFactory.load("cluster.conf"))

  val personManager = Domain(system).aggregateManagerOf[Person]

  val personId1 = PersonId(java.util.UUID.randomUUID().toString)
  val personId2 = PersonId(java.util.UUID.randomUUID().toString)

  personManager ! Person.Create(personId1, Name("Pete", "Anderson", Some("black")))
  personManager ! Person.Create(personId2, Name("Pete", "Anderson", Some("black")))

  personManager ! Person.Create(personId1, Name("Pete", "Anderson", Some("black")))

  for (i <- Range(0,50)) {
    personManager ! Person.LogState(personId1)
    personManager ! Person.ChangeName(personId1, Name("Frank", "Peters"))
    personManager ! Person.LogState(personId1)
    personManager ! Person.ChangeName(personId1, Name("John", "Sanders"))
    personManager ! Person.LogState(personId1)
    personManager ! Person.ChangeName(personId1, Name("Alfred", "Jackson"))
    personManager ! Person.LogState(personId1)
    personManager ! Person.ChangeName(personId1, Name("Tommy", "Tiger"))
  }

  personManager ! Person.ChangeName(personId2, Name("Freddy", "Johnson"))
  personManager ! Person.LogState(personId2)

  Thread.sleep(10000)

  for (i <- Range(0,50)) {
    personManager ! Person.LogState(personId1)
    personManager ! Person.ChangeName(personId1, Name("Frank", "Peters"))
    personManager ! Person.LogState(personId1)
    personManager ! Person.ChangeName(personId1, Name("John", "Sanders"))
    personManager ! Person.LogState(personId1)
    personManager ! Person.ChangeName(personId1, Name("Alfred", "Jackson"))
    personManager ! Person.LogState(personId1)
    personManager ! Person.ChangeName(personId1, Name("Tommy", "Tiger"))
  }

  for (i <- Range(0,50)) {
    personManager ! Person.LogState(personId2)
    personManager ! Person.ChangeName(personId2, Name("Frank", "Peters"))
    personManager ! Person.LogState(personId2)
    personManager ! Person.ChangeName(personId2, Name("John", "Sanders"))
    personManager ! Person.LogState(personId2)
    personManager ! Person.ChangeName(personId2, Name("Alfred", "Jackson"))
    personManager ! Person.LogState(personId2)
    personManager ! Person.ChangeName(personId2, Name("Tommy", "Tiger"))
  }

//  personManager ! Person.Boom(personId1)

  personManager ! Person.LogState(personId1)
  personManager ! Person.LogState(personId2)

  Thread.sleep(10000)

  system.shutdown()

}
