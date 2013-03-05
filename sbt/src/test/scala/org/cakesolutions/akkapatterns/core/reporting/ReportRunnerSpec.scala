package org.cakesolutions.akkapatterns.core.reporting

import org.specs2.mutable.Specification
import org.specs2.execute.Result
import java.io.{FileOutputStream, File}

/**
 * @author janmachacek
 */
class ReportRunnerSpec extends Specification {

  val runner = new ReportRunner with JRXmlReportCompiler with InputStreamReportLoader

  "x" in {
    runReport("empty.jrxml", EmptyExpression)
  }

  def runReport(source: String, expression: Expression): Result = {
    runner.runReport(getClass.getResourceAsStream(source))(EmptyExpression).run.toEither match {
      case Left(e)    => failure(e.getMessage)
      case Right(pdf) =>
        val fos = new FileOutputStream("/Users/janmachacek/x.pdf")
        fos.write(pdf)
        fos.close()
        success
    }
  }

}
