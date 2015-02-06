package io.pjan.akka.ddd.examples

import akka.actor.ActorSystem
import io.pjan.akka.ddd.AggregateManager
import io.pjan.akka.ddd.message.CommandMessage

object Boot extends App {

  implicit val system = ActorSystem("access")

  val personManager = AggregateManager[Person]

  val personId1 = PersonId(java.util.UUID.randomUUID())
  val personId2 = PersonId(java.util.UUID.randomUUID())
  //  val person = system.actorOf(Person.props(), personId.value.toString)

  personManager ! Person.Create(personId1, Name("Pete", "Anderson", Some("black")))
  personManager ! Person.Create(personId2, Name("Pete", "Anderson", Some("black")))

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

  Thread.sleep(5000)

  system.shutdown()

}
