package io.pjan.akka.ddd.support

import io.pjan.akka.ddd.EntityId
import io.pjan.akka.ddd.command.Command
import io.pjan.akka.ddd.message.CommandMessage
import io.pjan.akka.ddd.support.CommandHandler.HandleCommand
import io.pjan.akka.ddd.support.CommandMessageHandler.HandleCommandMessage
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpecLike}

class CommandMessageHandlerSpec extends WordSpecLike
                                with Matchers
                                with MockFactory {

  class TestId extends EntityId
  class TestCommand(val aggregateId: TestId) extends Command[TestId]

  val testId = new TestId
  val testCommand = new TestCommand(testId)
  val testCommandMessage = CommandMessage(testCommand)

  object TestCommandMessageHandler extends CommandMessageHandler[TestId] {
    def handleCommand: HandleCommand[TestId] = CommandHandler.emptyBehaviour

    override def handleCommandMessage: HandleCommandMessage[TestId] = CommandMessageHandler.wildcardBehavior
  }

  val cmh = mockFunction[CommandMessage[TestId], Unit]
  val ch  = mockFunction[Command[TestId], Unit]

  "#receiveCommandMessage" when {
    "invoked with a CommandMessageHandler and CommandHandler" should {
      "return a function that invokes both" in {
        val f = TestCommandMessageHandler.receiveCommandMessage(cmh)(ch)

        cmh expects testCommandMessage
        ch expects testCommand

        f(testCommandMessage)
      }
    }
  }

}
