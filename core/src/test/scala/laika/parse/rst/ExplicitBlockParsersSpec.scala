/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package laika.parse.rst

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import laika.parse.helper.DefaultParserHelpers
import laika.parse.helper.ParseResultHelpers
import laika.tree.Elements.Span
import laika.tree.helper.ModelBuilder
import laika.parse.rst.Elements._
import laika.tree.Elements._
import laika.parse.rst.TextRoles.RoleDirectivePart
import laika.parse.rst.Directives.DirectivePart
    
class ExplicitBlockParsersSpec extends FlatSpec 
                        with Matchers 
                        with BlockParsers 
                        with InlineParsers
                        with ParseResultHelpers 
                        with DefaultParserHelpers[RootElement] 
                        with ModelBuilder {

  
  val defaultParser: Parser[RootElement] = rootElement
  
  
  def blockDirective (name: String): Option[DirectivePart[Block]] = None
  def spanDirective (name: String): Option[DirectivePart[Span]] = None
  def textRole (name: String): Option[RoleDirectivePart[String => Span]] = None
  
  
  "The citation parser" should "parse a citation spanning a single line" in {
    val input = """.. [label] This is a citation"""
    Parsing (input) should produce (root (Citation("label", List(p("This is a citation")))))
  }
  
  it should "parse a citation spanning two lines" in {
    val input = """.. [label] This is a
      |   citation""".stripMargin
    Parsing (input) should produce (root (Citation("label", List(p("This is a\ncitation")))))
  }
  
  
  "The footnote parser" should "parse a footnote with autonumber label" in {
    Parsing (".. [#] This is a footnote") should produce (root(FootnoteDefinition(Autonumber, List(p("This is a footnote")))))
  }
  
  it should "parse a footnote with autosymbol label" in {
    Parsing (".. [*] This is a footnote") should produce (root(FootnoteDefinition(Autosymbol, List(p("This is a footnote")))))
  }
  
  it should "parse a footnote with an autonumber named label" in {
    Parsing (".. [#foo] This is a footnote") should produce (root(FootnoteDefinition(AutonumberLabel("foo"), List(p("This is a footnote")))))
  }
  
  it should "parse a footnote with a numeric label" in {
    Parsing (".. [17] This is a footnote") should produce (root(FootnoteDefinition(NumericLabel(17), List(p("This is a footnote")))))
  }
  
  
  "The link target parser" should "parse a named external target on one line" in {
    val input = """.. _some-link: http://www.foo.bar/"""
    Parsing (input) should produce (root(ExternalLinkDefinition("some-link", "http://www.foo.bar/")))
  }
  
  it should "parse a named external target with the reference name in backticks" in {
    val input = """.. _`some:link`: http://www.foo.bar/"""
    Parsing (input) should produce (root(ExternalLinkDefinition("some:link", "http://www.foo.bar/")))
  }
  
  it should "parse a named external target on two lines" in {
    val input = """.. _some-link: http://www.
      |     foo.bar/""".stripMargin
    Parsing (input) should produce (root(ExternalLinkDefinition("some-link", "http://www.foo.bar/")))
  }
  
  it should "parse a named external target with the URL entirely on the next line" in {
    val input = """.. _some-link: 
      |     http://www.foo.bar/""".stripMargin
    Parsing (input) should produce (root(ExternalLinkDefinition("some-link", "http://www.foo.bar/")))
  }
  
  it should "parse an anonymous external target" in {
    val input = """.. __: http://www.foo.bar/"""
    Parsing (input) should produce (root(ExternalLinkDefinition("", "http://www.foo.bar/")))
  }
  
  it should "parse a short anonymous external target" in {
    val input = """__ http://www.foo.bar/"""
    Parsing (input) should produce (root(ExternalLinkDefinition("", "http://www.foo.bar/")))
  }
  
  it should "parse an indirect simple reference" in {
    val input = """.. _ref: other_"""
    Parsing (input) should produce (root(LinkAlias("ref", "other")))
  }
  
  it should "parse an indirect phrase reference on one line" in {
    val input = """.. _ref: `other ref`_"""
    Parsing (input) should produce (root(LinkAlias("ref", "other ref")))
  }
  
  it should "parse an indirect phrase reference on two lines" in {
    val input = """.. _ref: `other
      | ref`_""".stripMargin
    Parsing (input) should produce (root(LinkAlias("ref", "other ref")))
  }
  
  it should "parse an indirect phrase reference on the following" in {
    val input = """.. _ref: 
      | `other ref`_""".stripMargin
    Parsing (input) should produce (root(LinkAlias("ref", "other ref")))
  }
  
  it should "parse an internal target" in {
    val input = """.. _some-target:
      |
      |Some text""".stripMargin
    Parsing (input) should produce (root(Paragraph(List(Text("Some text")), Id("some-target"))))
  }
  
  
  "The comment parser" should "parse a comment on one line" in {
    val input = """.. This is a comment"""
    Parsing (input) should produce (root(Comment("This is a comment")))
  }
  
  it should "parse a comment on two lines" in {
    val input = """.. This is
      |  a comment""".stripMargin
    Parsing (input) should produce (root(Comment("This is\na comment")))
  }
  
  it should "parse a comment with all text on the second line" in {
    val input = """..  
      |  This is a comment""".stripMargin
    Parsing (input) should produce (root(Comment("This is a comment")))
  }
  
  
}
