package org.cakesolutions.akkapatterns.core.reporting

import net.sf.jasperreports.engine.{JasperRunManager, JasperCompileManager, JasperReport}
import scalaz.EitherT
import scalaz.Id._
import java.io.InputStream
import net.sf.jasperreports.engine.xml.JRXmlLoader
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource
import java.util

/**
 * Report expressions are evaluated and put into the parameters for the report execution. There is a special-case
 * ``EmptyExpression`` that means _no expression_. We then have the sub-report expression and parameter expression
 * to allow us to pass _products_ as well as old-school _Java Beans_ from our Scala code to the JasperReport.
 */
sealed trait Expression
case object EmptyExpression extends Expression
case class ReportExpression[A](name: String, subreport: A, expressions: List[Expression]) extends Expression
case class ParametersExpression(expressions: List[Expression]) extends Expression

case class ProductParameterExpression[A <: Product](name: String, value: A) extends Expression
case class ProductListParameterExpression[A <: Product](name: String, value: List[A]) extends Expression
case class JavaBeanParameterExpression(name: String, value: AnyRef) extends Expression

/**
 * When evaluated, the ``Expressions`` become one of the ``ExpressionValue``s. The expression values match the expressions
 * defined above.
 */
sealed trait ExpressionValue
case object EmptyExpressionValue extends ExpressionValue
case class ReportExpressionValue(name: String, subreport: JasperReport, expressionValues: List[ExpressionValue]) extends ExpressionValue
case class ParametersExpressionValue(value: List[ExpressionValue]) extends ExpressionValue

case class ProductParameterExpressionValue[A <: Product](name: String, value: A) extends ExpressionValue
case class ProductListParameterExpressionValue[A <: Product](name: String, value: List[A]) extends ExpressionValue
case class JavaBeanParameterExpressionValue(name: String, value: AnyRef) extends ExpressionValue

/**
 * Loads the report from some input ``In`` and produces the report's ``InputStream``
 */
trait ReportLoader {
  /**
   * The type of input
   */
  type In

  /**
   * Loads the report from the input ``in`` and produces ``InputStream``
   *
   * @param in the input
   * @return the ReportT holding the ``InputStream`` for the given ``in``
   */
  def load(in: In): ReportT[InputStream]
}

/**
 * Compiles the report
 */
trait ReportCompiler {
  this: ReportLoader =>

  def compileReport(in: In): ReportT[JasperReport]
}

trait InputStreamReportLoader extends ReportLoader {
  type In = InputStream
  import scalaz.syntax.monad._

  def load(in: InputStream) = in.point[ReportT]
}

trait JRXmlReportCompiler extends ReportCompiler {
  this: ReportLoader =>

  def compileReport(in: In): ReportT[JasperReport] = {
    for {
      input  <- load(in)
      design =  JRXmlLoader.load(input)
    } yield JasperCompileManager.compileReport(design)
  }
}

/**
 * Runs the report by first loading it, then compiling it, evaluating the report expression and then executing it.
 */
class ReportRunner {
  this: ReportCompiler with ReportLoader =>

  private type ReportParameters = java.util.Map[String, AnyRef]

  import scalaz.syntax.monad._

  private def toMap(value: ExpressionValue): java.util.Map[String, AnyRef] = {
    def toMap0(value: ExpressionValue): Map[String, AnyRef] = value match {
      case ReportExpressionValue(name, subreport, values)      => Map(name -> subreport) ++ values.flatMap(toMap0)
      case ParametersExpressionValue(values)                   => values.flatMap(toMap0).toMap
      case ProductParameterExpressionValue(name, product)      => Map(name -> new JRProductListDataSource(product :: Nil))
      case ProductListParameterExpressionValue(name, products) => Map(name -> new JRProductListDataSource(products))
      case JavaBeanParameterExpressionValue(name, bean)        => Map(name -> new JRBeanArrayDataSource(Array(bean)))
      case EmptyExpressionValue                                => Map.empty
    }
    val map = toMap0(value)
    val result = new util.HashMap[String, AnyRef](map.size)
    map.foreach { e => result.put(e._1, e._2) }
    result
  }

  private def eval(expression: Expression): ReportT[ExpressionValue] = {
    def toList[A](a: A): List[A] = a :: Nil

    def evalList(expressions: List[Expression]): ReportT[List[ExpressionValue]] = expressions match {
      case Nil    => List.empty.point[ReportT]
      case h::Nil => eval(h).map(toList)
      case h::t   => val eh = eval(h).map(toList); t.foldLeft(eh) { (b, e) => for { bb <- b; ee <- eval(e) } yield ee +: bb }
    }

    expression match {
      case ReportExpression(name, subreport: In, expressions) =>
        for {
          report <- compileReport(subreport)
          values <- evalList(expressions)
        } yield ReportExpressionValue(name, report, values)
      case ParametersExpression(expressions)           =>
        for {
          values <- evalList(expressions)
        } yield ParametersExpressionValue(values)
      case ProductParameterExpression(name, value)     => ProductParameterExpressionValue(name, value).point[ReportT]
      case ProductListParameterExpression(name, value) => ProductListParameterExpressionValue(name, value).point[ReportT]
      case JavaBeanParameterExpression(name, value)    => JavaBeanParameterExpressionValue(name, value).point[ReportT]
      case EmptyExpression                             => EmptyExpressionValue.point[ReportT]
    }
  }

  /**
   * Runs the report from the input ``in`` with the evaluated ``expression``, giving the container of ``Array[Byte]``
   *
   * @param in the input (defined in the ``ReportLoader``)
   * @param expression the expression to be evaluated
   * @return the container of ``Array[Byte]`` of the output
   */
  def runReport(in: In)(expression: Expression): EitherT[Id, Throwable, Array[Byte]] = {
    for {
      root   <- compileReport(in)
      values <- eval(expression)
      params =  toMap(values)
    } yield JasperRunManager.runReportToPdf(root, params)
  }

}
