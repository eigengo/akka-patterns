package org.cakesolutions.akkapatterns.core

import scalaz.EitherT
import scalaz.Id._

/**
 * @author janmachacek
 */
package object reporting {

  type ReportT[A] = EitherT[Id, Throwable, A]

}
