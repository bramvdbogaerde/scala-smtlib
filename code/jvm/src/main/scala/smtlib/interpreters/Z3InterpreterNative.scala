package smtlib
package interpreters

import smtlib.printer.RecursivePrinter
import smtlib.Interpreter
import smtlib.parser.*
import smtlib.trees.Commands.{Script, Command}
import smtlib.trees.CommandsResponses.CommandResponse
import smtlib.trees.Terms._
import smtlib.trees.CommandsResponses._
import smtlib.printer.Printer
import scala.concurrent.*
import scala.concurrent.duration.DurationInt
import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.Status
import com.microsoft.z3.FuncDecl

/** An interpreter for SMTLib code backed by the libz3-java library */
class Z3InterpreterNative extends Interpreter {
  val printer: Printer = RecursivePrinter
  val parser: Parser = ???

  lazy val context: Context = Context()
  lazy val solver: Solver = context.mkSolver()

  def evalAsync(cmd: SExpr)(using ExecutionContext): Future[SExpr] =
    Future {
      import smtlib.trees.Commands.*
      cmd match {
        case CheckSat() =>
          solver.check() match {
            case Status.SATISFIABLE   => CheckSatStatus(SatStatus)
            case Status.UNSATISFIABLE => CheckSatStatus(UnsatStatus)
            case Status.UNKNOWN       => CheckSatStatus(UnknownStatus)
          }
        case GetAssertions()       => ???
        case GetUnsatCore()        => ???
        case GetUnsatAssumptions() => ???
        case GetProof()            => ???
        case GetValue(_, _)        => ???
        case GetOption(_)          => ???
        case GetModel()            => ???
        case _ =>
          val input = printer.toString(cmd)
          val exprs =
            context.parseSMTLIB2String(
              input,
              Array[com.microsoft.z3.Symbol](),
              Array[com.microsoft.z3.Sort](),
              Array[com.microsoft.z3.Symbol](),
              Array[com.microsoft.z3.FuncDecl[_]]()
            )
          solver.reset()
          exprs.foreach(solver.add(_))
          Success
      }
    }

  def free(): Unit = {
    context.close()
  }

  def interrupt(): Unit = solver.interrupt()
}
