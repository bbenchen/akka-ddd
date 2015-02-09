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

  sealed trait TestCommand extends Command[TestId] {
    def testId: TestId
    override def aggregateId: TestId = testId
  }
  case class HandledCommand(testId: TestId) extends TestCommand
  case class UnhandledCommand(testId: TestId) extends TestCommand

  val commandMessageHandlerMock = mockFunction[CommandMessage[TestId], Unit]
  val commandHandlerMock  = mockFunction[Command[TestId], Unit]

  val testId = new TestId
  val handledCommand = HandledCommand(testId)
  val handledCommandMessage = CommandMessage(handledCommand)

  object TestCommandMessageHandler extends CommandMessageHandler[TestId] {
    def handleCommand: HandleCommand[TestId] = {
      case cmd @ HandledCommand(_) => commandHandlerMock(cmd)
    }

    def handleCommandMessage: HandleCommandMessage[TestId] = {
      case cmdMsg @ CommandMessage(cmd: HandledCommand, _, _, _) => commandMessageHandlerMock(cmdMsg)
    }
  }

  "#receiveCommandMessage" when {
    "invoked with a CommandMessageHandler and CommandHandler" should {
      "return a function that invokes both" in {
        val f = TestCommandMessageHandler.receiveCommandMessage(commandMessageHandlerMock)(commandHandlerMock)

        commandMessageHandlerMock expects handledCommandMessage
        commandHandlerMock expects handledCommand

        f(handledCommandMessage)
      }
    }
  }

}
