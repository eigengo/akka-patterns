package org.eigengo.akkapatterns.domain

import akka.util.Timeout

trait DefaultTimeout {
  final val timeoutValue = 10000
  final implicit val timeout = Timeout(timeoutValue)

}
