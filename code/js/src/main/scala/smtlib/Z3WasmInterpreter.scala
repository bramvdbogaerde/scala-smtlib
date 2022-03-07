package smtlib

import parser.Parser
import lexer.Lexer
import printer.RecursivePrinter
import java.io.StringReader
import trees.Commands.{Script, Command}
import trees.CommandsResponses.CommandResponse
import trees.Terms.*
import printer.Printer
import typings.z3Solver.mod as z3mod
import scala.concurrent.{Future, ExecutionContext}
import typings.z3Solver.anon.Addconstinterp

/** A Z3 interpreter that can be used in a web browser environment.
  *
  * It uses the Z3 Javascript bindings from:
  * https://www.npmjs.com/package/z3-solver
  */
class Z3WasmInterpreter(using ExecutionContext) extends Interpreter {
  override val printer: Printer = RecursivePrinter
  override val parser: Parser = ???

  /** Prints the SExpr to an SMTlib String */
  private def toSMTLibString(expr: SExpr): String = ???

  /** Access to Z3 */
  private var z3: Addconstinterp = null

  /** Access to Z3 context */
  private var z3Context: z3mod.Z3Context = null

  (for {
    // initialize the WASM module
    result <- z3mod.init().toFuture
    em = result.em
    z3 = result.Z3
    // create a context
    cfg = z3.mk_config()
    ctx = z3.mk_context(cfg)
    _ = z3.del_config(cfg)
  } yield (ctx, z3)).map { case (ctx, z3) =>
    this.z3 = z3
    this.z3Context = ctx
  }

  def evalAsync(cmd: SExpr)(using ec: ExecutionContext): Future[SExpr] = {
    import scala.scalajs.js.Thenable.*
    Future {
      val output =
        this.z3.eval_smtlib2_string(this.z3Context, toSMTLibString(cmd))
      new Parser(new Lexer(new StringReader(output))).parseGenResponse
    }(using ec)
  }

  def free(): Unit =
    this.z3.del_context(this.z3Context)

  def interrupt(): Unit = ()
}
