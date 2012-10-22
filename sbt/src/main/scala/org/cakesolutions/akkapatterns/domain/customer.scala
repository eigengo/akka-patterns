package org.cakesolutions.akkapatterns.domain

import java.util.UUID

case class Customer(firstName: String, lastName: String,
                    email: String, addresses: Seq[Address],
                    id: UUID)

case class Address(line1: String, line2: String, line3: String)