package io.pjan.akka.ddd.examples

import akka.actor.ActorSystem
import io.pjan.akka.ddd.AggregateManager
import io.pjan.akka.ddd.message.CommandMessage

object Boot extends App {

  implicit val system = ActorSystem("access")

  val personManager = AggregateManager[Person]

  val personId = PersonId(java.util.UUID.randomUUID())
  val person = system.actorOf(Person.props(), personId.value.toString)

  person ! CommandMessage(Person.Create(personId, Name("Pete", "Anderson", Some("black"))))

  for (i <- Range(0,50)) {
    person ! CommandMessage(Person.LogState(personId))
    person ! CommandMessage(Person.ChangeName(personId, Name("Frank", "Peters"))).withMetaData(Map("trace" -> 1))
    person ! CommandMessage(Person.LogState(personId))
    person ! CommandMessage(Person.ChangeName(personId, Name("John", "Sanders")))
    person ! CommandMessage(Person.LogState(personId))
    person ! CommandMessage(Person.ChangeName(personId, Name("Alfred", "Jackson")))
    person ! CommandMessage(Person.LogState(personId))
    person ! CommandMessage(Person.ChangeName(personId, Name("Tommy", "Tiger")))
  }

  person ! CommandMessage(Person.LogState(personId))

  Thread.sleep(5000)

  system.shutdown()

}
