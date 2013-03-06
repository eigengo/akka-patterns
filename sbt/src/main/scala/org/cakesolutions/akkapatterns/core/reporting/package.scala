package org.cakesolutions.akkapatterns.core

import scalaz.EitherT
import scalaz.Id._
import net.sf.jasperreports.engine.{JRDataSource, JasperReport}

/**
 * @author janmachacek
 */
package object reporting {

  /**
   * Wraps the reporting operations in some monad transformers. At the moment, we have only the ``EitherT``, but this
   * structure allows us to add logging, state, ... in the future
   * @tparam A the "right" output type
   */
  type ReportT[A] = EitherT[Id, Throwable, A]

  /**
   * Report formatting function that operates on a ``JRDataSource``, taking the compiled and prepared
   * ``JasperReport``, the report parameters and the data source; it returns the bytes.
   * It may throw any exception, which will be caught by ``EitherT.fromException``
   */
  type FormatDS = (JasperReport, java.util.Map[String, AnyRef], JRDataSource) => Array[Byte]

}
