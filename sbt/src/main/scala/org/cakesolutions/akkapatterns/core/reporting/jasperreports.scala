package org.cakesolutions.akkapatterns.core.reporting

import net.sf.jasperreports.engine._
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

sealed trait DataSourceExpression extends Expression
case object EmptyDataSourceExpression extends DataSourceExpression
case class ProductParameterExpression[A <: Product](value: A, name: Option[String] = None) extends DataSourceExpression
case class ProductListParameterExpression[A <: Product](value: List[A], name: Option[String] = None) extends DataSourceExpression
case class JavaBeanParameterExpression(value: AnyRef, name: Option[String]) extends DataSourceExpression

/**
 * When evaluated, the ``Expressions`` become one of the ``ExpressionValue``s. The expression values match the expressions
 * defined above.
 */
sealed trait ExpressionValue
case object EmptyExpressionValue extends ExpressionValue
case class ReportExpressionValue(name: String, subreport: JasperReport, expressionValues: List[ExpressionValue]) extends ExpressionValue
case class ParametersExpressionValue(value: List[ExpressionValue]) extends ExpressionValue

sealed trait DataSourceExpressionValue extends ExpressionValue
case object EmptyDataSourceExpressionValue extends DataSourceExpressionValue
case class ProductParameterExpressionValue[A <: Product](value: A, name: Option[String]) extends DataSourceExpressionValue
case class ProductListParameterExpressionValue[A <: Product](value: List[A], name: Option[String]) extends DataSourceExpressionValue
case class JavaBeanParameterExpressionValue(value: AnyRef, name: Option[String]) extends DataSourceExpressionValue

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

  private def toDataSource(value: ExpressionValue): JRDataSource = value match {
    case ProductParameterExpressionValue(product, _)      => new JRProductListDataSource(product :: Nil)
    case ProductListParameterExpressionValue(products, _) => new JRProductListDataSource(products)
    case JavaBeanParameterExpressionValue(bean, _)        => new JRBeanArrayDataSource(Array(bean))
    case EmptyDataSourceExpressionValue                   => new JREmptyDataSource()
  }

  private def toMap(value: ExpressionValue): java.util.Map[String, AnyRef] = {
    def toMap0(value: ExpressionValue): Map[String, AnyRef] = value match {
      case ReportExpressionValue(name, subreport, values)            => Map(name -> subreport) ++ values.flatMap(toMap0)
      case ParametersExpressionValue(values)                         => values.flatMap(toMap0).toMap
      case ProductParameterExpressionValue(product, Some(name))      => Map(name -> new JRProductListDataSource(product :: Nil))
      case ProductListParameterExpressionValue(products, Some(name)) => Map(name -> new JRProductListDataSource(products))
      case JavaBeanParameterExpressionValue(bean, Some(name))        => Map(name -> new JRBeanArrayDataSource(Array(bean)))
      case EmptyExpressionValue                                      => Map.empty
      case _                                                         => Map.empty
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
      case ParametersExpression(expressions)                  =>
        for {
          values <- evalList(expressions)
        } yield ParametersExpressionValue(values)
      case ProductParameterExpression(value, name)            => ProductParameterExpressionValue(value, name).point[ReportT]
      case ProductListParameterExpression(value, name)        => ProductListParameterExpressionValue(value, name).point[ReportT]
      case JavaBeanParameterExpression(value, name)           => JavaBeanParameterExpressionValue(value, name).point[ReportT]
      case EmptyExpression                                    => EmptyExpressionValue.point[ReportT]
    }
  }

  /**
   * Runs the report from the input ``in`` with the evaluated ``expression``, giving the container of ``Array[Byte]``
   *
   * @param in the input (defined in the ``ReportLoader``)
   * @param parametersExpression the report parameters to be used
   * @param dataSourceExpression the data source to be used
   * @return the container of ``Array[Byte]`` of the output
   */
  def runReport(in: In)(parametersExpression: Expression = EmptyExpression,
                        dataSourceExpression: DataSourceExpression = EmptyDataSourceExpression): EitherT[Id, Throwable, Array[Byte]] = {
    for {
      root             <- compileReport(in)
      parametersValues <- eval(parametersExpression)
      parameters       =  toMap(parametersValues)
      dataSourceValue  <- eval(dataSourceExpression)
      dataSource       =  toDataSource(dataSourceValue)
    } yield JasperRunManager.runReportToPdf(root, parameters, dataSource)
  }

}
