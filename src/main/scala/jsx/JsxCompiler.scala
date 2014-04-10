package jsx

import javax.script.ScriptEngineManager

case class JsxCompileResult(code: String, sourceMap: Option[Any], sourceMapFile: Option[String])

/**
 * Some rather silly and simple wrapper for the JSXTransformer script using the nashorn engine.
 * @param engineName
 */
class JsxCompiler(engineName: String="nashorn") {

  private val transformerPath = "META-INF/resources/webjars/react/0.9.0/JSXTransformer.js"

  lazy val engine = {
    val e = new ScriptEngineManager().getEngineByName(engineName)
    e.eval("var global = this;");
    e.eval(io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(transformerPath)).mkString)
    e
  }

  def render(component: String):Option[JsxCompileResult] = {
    val jsCommand = s"""JSXTransformer.transform("${cleanup(component)}");"""
    val result = Option(engine.eval(jsCommand)).map(_.asInstanceOf[jdk.nashorn.api.scripting.ScriptObjectMirror])

      result match {
      case Some(js) => Some(JsxCompileResult(code = js.get("code").asInstanceOf[String], None, None))
      case None => None
    }
  }

  def cleanup(mess: String): String =
    mess.split("\r?\n").filterNot(_.isEmpty).map(_ + "\\n").mkString(""""+"""")
}
