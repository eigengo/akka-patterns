package org.cakesolutions.akkapatterns.test

import org.cakesolutions.akkapatterns.domain.{Address, Customer}
import java.util.UUID


/**
 * @author janmachacek
 */
trait DefaultTestData {
  val janMachacek = Customer("Jan", "Machacek", "janm@cakesolutions.net",
    Address("Magdalen Centre", "Robert Robinson Avenue", "Oxford") ::
    Address("Houldsworth Mill", "Houldsworth Street", "Reddish") :: Nil,
    UUID.fromString("00000000-0000-0000-0000-000000000000"))

  val joeBloggs = Customer("Joe", "Bloggs", "joe@cakesolutions.net",
    Address("123 Winding Road", "Cowley", "Oxford") :: Nil,
    UUID.fromString("00000000-0000-0000-0100-000000000000"))

}
