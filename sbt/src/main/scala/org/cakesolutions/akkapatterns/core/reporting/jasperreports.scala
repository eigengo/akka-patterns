package org.cakesolutions.akkapatterns.core.reporting

import net.sf.jasperreports.engine.{JasperRunManager, JasperCompileManager, JasperReport}
import scalaz.EitherT
import scalaz.Id._
import java.io.InputStream
import net.sf.jasperreports.engine.xml.JRXmlLoader
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource
import java.util

sealed trait Expression
case object EmptyExpression extends Expression
case class ReportExpression[A](name: String, subreport: A, expressions: List[Expression]) extends Expression
case class ParametersExpression(expressions: List[Expression]) extends Expression

case class ProductParameterExpression[A <: Product](name: String, value: A) extends Expression
case class ProductListParameterExpression[A <: Product](name: String, value: List[A]) extends Expression
case class JavaBeanParameterExpression(name: String, value: AnyRef) extends Expression

sealed trait ExpressionValue
case object EmptyExpressionValue extends ExpressionValue
case class ReportExpressionValue(name: String, subreport: JasperReport, expressionValues: List[ExpressionValue]) extends ExpressionValue
case class ParametersExpressionValue(value: List[ExpressionValue]) extends ExpressionValue

case class ProductParameterExpressionValue[A <: Product](name: String, value: A) extends ExpressionValue
case class ProductListParameterExpressionValue[A <: Product](name: String, value: List[A]) extends ExpressionValue
case class JavaBeanParameterExpressionValue(name: String, value: AnyRef) extends ExpressionValue

trait ReportCompiler {
  this: ReportLoader =>

  def compileReport(in: In): ReportT[JasperReport]
}

trait ReportLoader {
  type In

  def load(in: In): ReportT[InputStream]
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

  def runReport(in: In)(expression: Expression): EitherT[Id, Throwable, Array[Byte]] = {
    for {
      root   <- compileReport(in)
      values <- eval(expression)
      params =  toMap(values)
    } yield JasperRunManager.runReportToPdf(root, params)
  }

}
