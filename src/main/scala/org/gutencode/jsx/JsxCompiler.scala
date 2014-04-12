package org.gutencode.jsx

import javax.script.{ScriptException, ScriptEngine, ScriptEngineManager}

/**
 *
 * @param code
 * @param sourceMap
 * @param sourceMapFile
 */
case class JsxCompileResult(code: String, sourceMap: Option[Any], sourceMapFile: Option[String])

/**
 * Some rather silly and simple wrapper for the JSXTransformer script using the nashorn engine.
 * @param engineName
 */
class JsxCompiler(engineName: String="nashorn") {

  private val transformerPath = "META-INF/resources/webjars/react/0.9.0/JSXTransformer.js"

  lazy val engine: ScriptEngine= {
    val manager = new ScriptEngineManager(classOf[JsxCompiler].getClassLoader)
    val e = Option(manager.getEngineByName(engineName))
    e.map{ engine =>
      engine.eval("var global = this;")
      engine.eval(io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(transformerPath)).mkString)
      engine
    }.getOrElse{
      println("Engines: ")
      val iter = manager.getEngineFactories.iterator()
      while (iter.hasNext) {
        println(iter.next.getEngineName)
      }
      throw new IllegalArgumentException(s"No engine named $engineName found")
    };

  }

  /**
   *
   * @param component
   * @return
   */
  def transformJsx(component: String):Option[JsxCompileResult] = {
    val transformCommand =
      s"""
        |(function(transform){
        |  var _script=${cleanup(component)};
        |  return transform(_script);
        |})(JSXTransformer.transform);
      """.stripMargin
    try {
      val result = Option(engine.eval(transformCommand)).map(_.asInstanceOf[jdk.nashorn.api.scripting.ScriptObjectMirror])
      result match {
        case Some(js) => Some(JsxCompileResult(code = js.get("code").asInstanceOf[String], None, None))
        case None => None
      }
    } catch {
      case e:ScriptException => {
        println("Invalid script: \n" + e.getMessage + "\n\n" + component)
        None
      }
    }
  }

  /**
   * create a javascript string out of the nice scala multiline string using concatenation.
   * @param mess the messy string
   * @return a clean version
   */
  def cleanup(mess: String): String =
    "\"" + mess.split("\r?\n").filterNot(_.isEmpty).map(_.replace("\"", "\\\"") + "\\n").mkString(""""+"""") + "\""
}
