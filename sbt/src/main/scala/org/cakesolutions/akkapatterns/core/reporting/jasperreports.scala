package org.cakesolutions.akkapatterns.core.reporting

import net.sf.jasperreports.engine._
import design.JasperDesign
import export.{JRXlsExporter, JRPdfExporter}
import scalaz.EitherT
import scalaz.Id._
import java.io.{ByteArrayOutputStream, InputStream}
import net.sf.jasperreports.engine.xml.JRXmlLoader
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource
import java.util

/**
 * Report expressions are evaluated and put into the parameters for the report execution. There is a special-case
 * ``EmptyExpression`` that means _no expression_. We then have the sub-report expression and parameter expression
 * to allow us to pass _products_ as well as old-school _Java Beans_ from our Scala code to the JasperReport.
 */
sealed trait Expression

/**
 * Empty expression carrying no useful value
 */
case object EmptyExpression extends Expression

/**
 * A sub-report expression that, when evaluated, will load & compile the sub-report and merge the parameters
 * @param name the name of the parameter
 * @param subreport the subreport
 * @param expressions the parameters for the sub-report
 * @tparam A the type of the sub-report source (must be loadable from the mixed-in ``ReportLoader``)
 */
case class ReportExpression[A](name: String, subreport: A, expressions: List[Expression]) extends Expression

/**
 * The parameters expression that holds the list of expressions that, when evaluated, will be passed in to the report
 * as _parameters_
 *
 * @param expressions the parameter expressions
 */
case class ParametersExpression(expressions: List[Expression]) extends Expression

/**
 * Expression that can be ultimately evaluated to ``JRDataSource``
 */
sealed trait DataSourceExpression extends Expression

/**
 * Empty data source with no rows and no columns
 */
case object EmptyDataSourceExpression extends DataSourceExpression

/**
 * Data source holding one instance of a product--i.e. a case class
 *
 * @param value the product instance
 * @param name optionally, the name
 * @tparam A the value type
 */
case class ProductParameterExpression[A <: Product](value: A, name: Option[String] = None) extends DataSourceExpression
/**
 * Data source holding list of instances of a product--i.e. a case class
 *
 * @param value the product instances
 * @param name optionally, the name
 * @tparam A the value type
 */
case class ProductListParameterExpression[A <: Product](value: List[A], name: Option[String] = None) extends DataSourceExpression

/**
 * Data source holding one instance of a Java Bean
 *
 * @param value the JavaBean instance
 * @param name optionally, the name
 */
case class JavaBeanParameterExpression(value: AnyRef, name: Option[String]) extends DataSourceExpression

/**
 * When evaluated, the ``Expressions`` become one of the ``ExpressionValue``s. The expression values match the expressions
 * defined above. They are intended to be used only from within the ``reporting`` package.
 */
private[reporting] sealed trait ExpressionValue
private[reporting] case object EmptyExpressionValue extends ExpressionValue
private[reporting] case class ReportExpressionValue(name: String, subreport: JasperReport, expressionValues: List[ExpressionValue]) extends ExpressionValue
private[reporting] case class ParametersExpressionValue(value: List[ExpressionValue]) extends ExpressionValue

private[reporting] sealed trait DataSourceExpressionValue extends ExpressionValue
private[reporting] case object EmptyDataSourceExpressionValue extends DataSourceExpressionValue
private[reporting] case class ProductParameterExpressionValue[A <: Product](value: A, name: Option[String]) extends DataSourceExpressionValue
private[reporting] case class ProductListParameterExpressionValue[A <: Product](value: List[A], name: Option[String]) extends DataSourceExpressionValue
private[reporting] case class JavaBeanParameterExpressionValue(value: AnyRef, name: Option[String]) extends DataSourceExpressionValue

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

  /**
   * Compiles the report to produce the ``JasperReport`` that can be merged in with parameters and data source
   *
   * @param in the input type (see ``ReportLoader#In``)
   * @return the container of ``JasperReport``
   */
  def compileReport(in: In): ReportT[JasperReport]
}

case class NullInputStreamException() extends RuntimeException

/**
 * Pass-through loader that simply takes the given ``InputStream`` as its output
 */
trait InputStreamReportLoader extends ReportLoader {
  type In = InputStream
  import scalaz.syntax.monad._

  def load(in: InputStream) =
    if (in == null) EitherT.left[Id, Throwable, InputStream](NullInputStreamException())
    else in.point[ReportT]

}

case class MissingClasspathResourceException(resourceName: String) extends RuntimeException

/**
 * Loader that loads the report using the current ``ClassLoader``, where the ``in`` is the classpath resource name
 */
trait ClasspathResourceReportLoader extends ReportLoader {
  import scalaz.syntax.monad._

  type In = String

  def load(in: String) = {
    val is = getClass.getResourceAsStream(in)
    if (is == null) EitherT.left[Id, Throwable, InputStream](MissingClasspathResourceException(in))
    else is.point[ReportT]
  }
}

/**
 * Compiles the reports by taking the ``JRXML`` format and running it through the ``JasperCompilerManager``
 */
trait JRXmlReportCompiler extends ReportCompiler {
  this: ReportLoader =>

  def compileReport(in: In): ReportT[JasperReport] = {
    for {
      loaded <- load(in)
      input  <- EitherT.fromTryCatch[Id, JasperDesign] { JRXmlLoader.load(loaded) }
      design <- EitherT.fromTryCatch[Id, JasperReport] { JasperCompileManager.compileReport(input) }
    } yield design
  }
}

trait ReportRunnerVirtualizer {

  /*
  final JRVirtualizer virtualizer = new JRSwapFileVirtualizer(10000, new JRConcurrentSwapFile("/tmp", 1024, 1024));
  Map<String, Object> paramMap = buildInitialPramMap();
  paramMap.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
   */

}

/**
 * Expression could not be evaluated
 *
 * @param e the failing expression
 */
case class BadExpressionException(e: Expression) extends RuntimeException

/**
 * Runs the report by first loading it, then compiling it, evaluating the report expression and then executing it.
 */
trait ReportRunner {
  this: ReportCompiler with ReportLoader =>

  import scalaz.syntax.monad._

  private def toDataSource(value: ExpressionValue): JRDataSource = value match {
    case ProductParameterExpressionValue(product, _)      => new JRProductListDataSource(product :: Nil)
    case ProductListParameterExpressionValue(products, _) => new JRProductListDataSource(products)
    case JavaBeanParameterExpressionValue(bean, _)        => new JRBeanArrayDataSource(Array(bean))
    case EmptyDataSourceExpressionValue                   => new JREmptyDataSource()
    case _                                                => sys.error("Bad match") // OK
  }

  private def toMap(value: ExpressionValue): ReportParameters = {
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

  /**
   * Evaluates the ``expression``, returning the evaluation result.
   *
   * @param expression the expression to be evaluated
   * @return the evaluation result
   */
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
      case e                                                  => EitherT.left[Id, Throwable, ExpressionValue](BadExpressionException(e))
    }
  }

  /**
   * A strict version of the ``runReportT``, which attempts to run the report and return the array of bytes that
   * make up the output; throws the error in case of failures
   *
   * @param in the input (defined in ``ReportLoader``)
   * @param format the report output format
   * @param parametersExpression the report parameters to be used
   * @param dataSourceExpression the data source to be used
   * @return the ``Array[Byte]`` of the output
   */
  def runReport(in: In)
               (format: FormatDS,
                parametersExpression: Expression = EmptyExpression,
                dataSourceExpression: DataSourceExpression = EmptyDataSourceExpression): Array[Byte] =
    runReportT(in)(format, parametersExpression, dataSourceExpression).run.toEither match {
      case Left(ex)   => throw ex
      case Right(out) => out
    }

  /**
   * Runs the report from the input ``in`` with the evaluated ``expression``, giving the container of ``Array[Byte]``
   *
   * @param in the input (defined in the ``ReportLoader``)
   * @param format the report output format
   * @param parametersExpression the report parameters to be used
   * @param dataSourceExpression the data source to be used
   * @return the container of ``Array[Byte]`` of the output
   */
  def runReportT(in: In)
                (format: FormatDS,
                 parametersExpression: Expression = EmptyExpression,
                 dataSourceExpression: DataSourceExpression = EmptyDataSourceExpression): EitherT[Id, Throwable, Array[Byte]] = {
    for {
      root             <- compileReport(in)
      parametersValues <- eval(parametersExpression)
      parameters       =  toMap(parametersValues)
      dataSourceValue  <- eval(dataSourceExpression)
      dataSource       =  toDataSource(dataSourceValue)
      out              <- EitherT.fromTryCatch[Id, Array[Byte]] { format(root, parameters, dataSource) }
    } yield out
  }

}

/**
 * Excel report format
 */
trait ExcelReportFormat {

  private def fill(print: JasperPrint): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    val exporter = new JRXlsExporter
    exporter.setParameter(JRExporterParameter.JASPER_PRINT, print)
    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output)
    exporter.exportReport()

    output.toByteArray
  }

  val ExcelDS: FormatDS = (report, parameters, dataSource) =>
    fill(JasperFillManager.fillReport(report, parameters, dataSource))

  val ExcelC: FormatC = (report, parameters, connection) =>
    fill(JasperFillManager.fillReport(report, parameters, connection))



}

/**
 * PDF report format
 */
trait PdfReportFormat {

  private def fill(print: JasperPrint): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    val exporter = new JRPdfExporter()
    exporter.setParameter(JRExporterParameter.JASPER_PRINT, print)
    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output)
    exporter.exportReport()

    output.toByteArray
  }

  val PdfC: FormatC = (report, parameters, connection) =>
    fill(JasperFillManager.fillReport(report, parameters, connection))

  /**
   * The PDF format that outputs the report as PDFs
   */
  val PdfDS: FormatDS = (report, parameters, dataSource) =>
    fill(JasperFillManager.fillReport(report, parameters, dataSource))

}

/**
 * Holds the various report formats that are ultimately responsible for taking the report, the parameters and the
 * data source to ultimately produce the array of bytes that represents the desired output.
 */
trait ReportFormats extends PdfReportFormat with ExcelReportFormat