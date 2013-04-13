package org.eigengo.akkapatterns.core.reporting

import org.specs2.mutable.Specification
import org.specs2.execute.Result
import java.io.FileOutputStream

/**
 * @author janmachacek
 */
class ReportRunnerSpec extends Specification with ReportFormats {

  val runner = new ReportRunner with JRXmlReportCompiler with ClasspathResourceReportLoader

  "failure collection" should {
    "report errors in loader" in {
      runner.runReportT("foo")(PdfDS).run.isLeft mustEqual true
    }

    "report errors in compiler" in {
      runner.runReportT("broken.jrxml")(PdfDS).run.isLeft mustEqual true
    }
  }

  "simple report" in {
    runReport("empty.jrxml", EmptyExpression, ProductListParameterExpression(
//      Users.newUser("janm") :: Users.newUser("anirvanc") :: Nil
      Nil
    ))
  }

  def runReport(source: String, parametersExpression: Expression, dataSourceExpression: DataSourceExpression): Result = {
    runner.runReportT(source)(PdfDS, parametersExpression, dataSourceExpression).run.toEither match {
      case Left(e)    => failure(e.getMessage)
      case Right(pdf) =>
        val fos = new FileOutputStream("/Users/janmachacek/x.pdf")
        fos.write(pdf)
        fos.close()
        success
    }
  }

}
