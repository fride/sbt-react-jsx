package org.gutencode.jsx

import sbt._
import Keys._

object JsxKeys {
  val jsxCompile     = TaskKey[Seq[File]]("jsx-compile", "Compile a string to jsx")
  val jsxEngineName  = SettingKey[String]("jsx-engine-name", "Javascript engine used.")
}

object JsxPlugin extends Plugin {

  import JsxKeys._

  val jsxSettings:Seq[Setting[_]] = Seq(
    jsxEngineName := "nashorn",
    sourceDirectory in jsxCompile <<= (sourceDirectory in Compile) / "jsx",
    sourceManaged in jsxCompile <<= (sourceManaged in Compile) / "js",
    jsxCompile := {
      generateFromJsx(
        streams.value,
        (sourceDirectory in jsxCompile).value,
        (sourceManaged   in jsxCompile).value)
    },
    resourceGenerators in Compile <+= jsxCompile
  )

  def generateFromJsx(streams: TaskStreams, sourceDir: File, targetDir: File): Seq[File] = {
    println(s"Generating from ${sourceDir} to $targetDir")
    val files = sourceDir ** "*.jsx"
    val compiler = new JsxCompiler()

    def changeExtension(f: File): File = {
      val (ext, name) = f.getName.reverse.span(_ != '.')
      new File(f.getParent, name.drop(1).reverse.toString + ".js")
    }

    val mapping = (files x rebase(sourceDir, targetDir)).map {
      case (orig, target) => (orig, changeExtension(target))
    }
    mapping foreach {
      case (jsx, target) =>
        if (jsx.lastModified > target.lastModified) {
          streams.log.info("Generating '%s'" format target.getName)
          val template = IO.read(jsx)
          val rendered = compiler.transformJsx(template)
          rendered.foreach(res => IO.write(target, res.code))
        } else
          streams.log.debug("Template '%s' older than target. Ignoring." format jsx.getName)
    }
    Nil
  }
}
