package org.gutencode.jsx

import sbt._
import Keys._



object JsxPlugin extends Plugin {

  object JsxKeys {
    val jsxCompile     = TaskKey[Seq[File]]("jsx-compile",     "Compile a string to jsx")
    val jsxExtension   = SettingKey[String]("jsx-extension",   "File extension of file to compile.")
    val jsxEngineName  = SettingKey[String]("jsx-engine-name", "Javascript engine used.")
  }

  import JsxKeys._

  val jsxSettings:Seq[Setting[_]] = Seq(
    jsxEngineName := "nashorn",
    jsxExtension := "jsx",
    sourceDirectory in jsxCompile := {
      (sourceDirectory in Compile).value / "jsx"
    },
    sourceManaged in jsxCompile   <<= (sourceManaged in Compile) / "js",
    jsxCompile := {
      generateFromJsx(
        streams.value,
        jsxExtension.value,
        (sourceDirectory in jsxCompile).value,
        (sourceManaged   in jsxCompile).value)
    },
    sourceGenerators in Compile <+= jsxCompile,
    watchSources <++=
      (sourceDirectory in jsxCompile, jsxExtension) map {(path,extension) =>
        (path ** ("*." + extension)).get
      }

  )

  def generateFromJsx(streams: TaskStreams, jsxExtension: String, sourceDir: File, targetDir: File): Seq[File] = {
    val files = sourceDir ** s"*.$jsxExtension"
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
