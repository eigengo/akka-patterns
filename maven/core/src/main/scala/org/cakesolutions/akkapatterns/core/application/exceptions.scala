package org.cakesolutions.akkapatterns.core.application

case class CannotUnloadException(path: String) extends Exception

case class CannotLoadException(path: String) extends Exception