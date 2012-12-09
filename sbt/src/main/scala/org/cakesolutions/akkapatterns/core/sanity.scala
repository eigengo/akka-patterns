package org.cakesolutions.akkapatterns.core

import org.cakesolutions.akkapatterns.domain.UserFormats

/**
 * Initial system sanity checks
 */
trait SanityChecks extends TypedGraphDatabase with UserFormats with
  SprayJsonNodeMarshalling {

  private def ensureUserSanity: Boolean = {
    // TODO: complete me
    true
  }

  def ensureSanity: Boolean = synchronized {
    ensureUserSanity
  }

}
