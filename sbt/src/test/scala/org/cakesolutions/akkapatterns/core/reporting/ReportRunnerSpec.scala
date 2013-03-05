package org.cakesolutions.akkapatterns.core.reporting

import org.specs2.mutable.Specification
import org.specs2.execute.Result
import java.io.{FileOutputStream, File}
import org.cakesolutions.akkapatterns.TestData

/**
 * @author janmachacek
 */
class ReportRunnerSpec extends Specification with TestData {

  val runner = new ReportRunner with JRXmlReportCompiler with ClasspathResourceReportLoader

  "x" in {
    runReport("empty.jrxml", EmptyExpression, ProductListParameterExpression(Users.newUser("janm") :: Users.newUser("anirvanc") :: Nil))
  }

  trait ClasspathResourceReportLoader extends ReportLoader {
    import scalaz.syntax.monad._

    type In = String

    /**
     * Loads the report from the input ``in`` and produces ``InputStream``
     *
     * @param in the input
     * @return the ReportT holding the ``InputStream`` for the given ``in``
     */
    def load(in: String) = ReportRunnerSpec.this.getClass.getResourceAsStream(in).point[ReportT]
  }

  def runReport(source: String, parametersExpression: Expression, dataSourceExpression: DataSourceExpression): Result = {
    runner.runReport(source)(parametersExpression, dataSourceExpression).run.toEither match {
      case Left(e)    => failure(e.getMessage)
      case Right(pdf) =>
        val fos = new FileOutputStream("/Users/janmachacek/x.pdf")
        fos.write(pdf)
        fos.close()
        success
    }
  }

}
