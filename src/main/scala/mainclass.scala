import org.jruby.embed.ScriptingContainer
import scala.io.Source

object mainclass {
  def getFileContents(filename:String):Option[String] = {
      val stream = Source.fromFile(filename)
      try
        Some(stream.mkString)
      catch {
        case e:Exception=>
          None
      } finally stream.close()
  }

  def doRun(container:ScriptingContainer,filecontent:String) = {
    try {
      /* runScriptlet returns the exit code from the script, and runs synchronously */
      val r = container.runScriptlet(filecontent)
      println(s"\nrunScriptlet returned $r\n\n")
    } catch {
      /*exceptions in ruby show as EvalFailedException, not RaiseException below*/
      case e:org.jruby.embed.EvalFailedException=>
        println(s"\nRuby exception occurred (eval failed): ${e.getMessage}")
        for (l <- e.getStackTrace) {
          println(s"\t$l")
        }
      case e:org.jruby.exceptions.RaiseException=>
        println(s"\nRuby exception occurred (raiseexception): ${e.getMessage}")
        println(e.getStackTrace)
    }
  }

/* see https://github.com/jruby/jruby/wiki/RedBridge */
  def main(args: Array[String]):Unit = {
    val container = new ScriptingContainer()
    getFileContents("call_java.rb") match {
      case Some(filecontent)=>doRun(container,filecontent)
      case None=>println("could not read file")
    }
    println("done")
    while(container.isRunRubyInProcess){
      println("waiting")
      Thread.sleep(500)
    }

  }
}
