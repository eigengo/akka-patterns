package org.cakesolutions.akkapatterns.api

import cc.spray.Directives
import akka.actor.ActorSystem
import cc.spray.http._
import cc.spray.http.MediaTypes._
import cc.spray.RequestContext

class DummyService(path: String)(implicit val actorSystem: ActorSystem) extends Directives {

  val route = {
    pathPrefix(path) {
      x =>
        x.complete(
          HttpResponse(StatusCodes.OK, getContent(x, `application/json`)))
    }
  }

  private def getContent(ctx: RequestContext, contentType: ContentType): HttpContent = {

    var filename = ctx.request.path

    if (filename.startsWith("/")) filename = filename.drop(1)
    if (filename.endsWith("/")) filename = filename.dropRight(1)
    filename = filename.replace("/", "-")
    filename = filename + "-" + ctx.request.method.toString().toLowerCase + ".json"

    val uidRegex = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r
    val fileContent = uidRegex.findFirstIn(filename) match {
      case Some(uid) =>
        getFileAsString(uidRegex.replaceAllIn(filename, "UUID"))
      case None =>
        getFileAsString(filename)
    }

    HttpContent(contentType, fileContent)
  }

  private def getFileAsString(filename: String): String = {
    try {
      scala.io.Source.fromInputStream(getClass.getResourceAsStream(filename)).mkString
    }
    catch {
      case _ => "{body of file " + filename + " -- Missing File!}"
    }
  }
}
