package smtlib

import parser.Parser
import trees.Commands.{Script, Command}
import trees.CommandsResponses.CommandResponse
import trees.Terms._
import printer.Printer
import scala.concurrent.*
import scala.concurrent.duration.DurationInt

/*
 * An interpreter is a stateful object that can eval Commands and returns
 * CommandResponse.
 *
 * Note that despite returning the command response, the interpreter should handle the printing
 * of those responses itself. That is because it needs to handle the verbosity and *-output-channel
 * options commands, and will do the correct printing depending on the internal state.
 * The responses are returned as a way to progamatically communicate with a solver.

 * TODO: The interaction of the set-option for the channels with the eval interface
         seems just wrong. Need to clarify that.
 */
trait Interpreter {

  val printer: Printer
  val parser: Parser

  final def eval(cmd: SExpr)(using ExecutionContext): SExpr = 
    Await.result(evalAsync(cmd), 0.nanos)

  def evalAsync(cmd: SExpr)(using ExecutionContext): Future[SExpr]
  

  //A free method is kind of justified by the need for the IO streams to be closed, and
  //there seems to be a decent case in general to have such a method for things like solvers
  //note that free can be used even if the solver is currently solving, and act as a sort of interrupt
  def free(): Unit

  def interrupt(): Unit
}

object Interpreter {
  import scala.concurrent.ExecutionContext.Implicits.global
  import java.io.Reader
  import java.io.FileReader
  import java.io.BufferedReader
  import java.io.File

  def execute(script: Script)(implicit interpreter: Interpreter): Unit = {
    for(cmd <- script.commands)
      interpreter.eval(cmd)
  }

  def execute(scriptReader: Reader)(implicit interpreter: Interpreter): Unit = {
    val parser = new Parser(new lexer.Lexer(scriptReader))
    val cmd: Command = null
    while ({ {
      val cmd = parser.parseCommand
      if(cmd != null)
        interpreter.eval(cmd)
    } ;cmd != null}) ()
  }

  def execute(file: File)(implicit interpreter: Interpreter): Unit = {
    val parser = new Parser(new lexer.Lexer(new BufferedReader(new FileReader(file))))
    var cmd: Command = null
    while ({ {
      cmd = parser.parseCommand
      if(cmd != null)
        interpreter.eval(cmd)
    } ;cmd != null}) ()
  }

}
