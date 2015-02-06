package io.pjan.akka.ddd.examples

import akka.actor.{ActorLogging, Props}
import io.pjan.akka.ddd._
import io.pjan.akka.ddd.state.AggregateState


case class PersonId(value: java.util.UUID) extends AggregateUuid

object Person {
  sealed trait Command extends command.Command[PersonId] {
    def personId: PersonId
    override def aggregateId: PersonId = personId
  }
  case class Create(personId: PersonId, name: Name) extends Command
  case class ChangeName(personId: PersonId, name: Name) extends Command
  case class LogState(personId: PersonId) extends Command

  sealed trait Event extends event.Event
  case class PersonCreated(personId: PersonId, name: Name) extends Event
  case class PersonNameChanged(personId: PersonId, name: Name) extends Event

  case class PersonState(id: PersonId, name: Name) extends AggregateState {
    override def apply: AggregateState.StateTransitions = {
      case PersonNameChanged(_, newName) => copy(name = newName)
    }
  }

  def apply(): Person = new Person()
  def props(): Props = Props(Person())
}

class Person extends AggregateRoot[PersonId, Person.PersonState] with ActorLogging {
  import Person._

  val id: PersonId = PersonId(java.util.UUID.fromString(self.path.name))

  override def initializeState: InitializeState = {
    case PersonCreated(personId, name) => PersonState(personId, name)
  }

  override def transition: Transition = {
    case PersonCreated(_, _) => initialized
  }

  override def handleCommand: HandleCommand = uninitialized

  def uninitialized: HandleCommand = {
    case Create(personId, name) => if (!isInitialized) materialize(PersonCreated(personId, name)) else throw new RuntimeException("already initialized")
  }

  def initialized: HandleCommand = {
    case ChangeName(personId, name) =>
      println(toEventMessage(PersonNameChanged(personId, name)))
      materialize(PersonNameChanged(personId, name))
    case LogState(_)                => log.info(state.toString)
  }
}