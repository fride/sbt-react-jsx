package org.gutencode.jsx

import org.specs2.mutable._

/**
 *
 */
class JsxCompilerSpec extends Specification {

  "A valid normal javascript" should {
    "not be changed" in {
      val script =
        """|var something = "this";
           |alert("This is " + global);
           |function fun(a) {
           |   return !a
           |}""".stripMargin
      val compiled = new JsxCompiler().transformJsx(script)
      compiled.get.code.trim must beEqualTo(script.trim)
    }
  }

  "a valid jsx file" should {
    "have all xml tags replaced" in {
      val script =
        """|/** @jsx React.DOM */
           |React.renderComponent(
           |   <h1>Hello, world!</h1>,
           |   document.getElementById('example')
           |);
        """.stripMargin.trim
      val expected =
        """|/** @jsx React.DOM */
           |React.renderComponent(
           |   React.DOM.h1(null, "Hello, world!"),
           |   document.getElementById('example')
           |);
        """.stripMargin.trim
      val compiled = new JsxCompiler().transformJsx(script)
      compiled.get.code.trim must beEqualTo(expected)
    }
  }
}
