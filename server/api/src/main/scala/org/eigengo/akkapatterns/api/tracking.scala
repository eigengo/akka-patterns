package org.cakesolutions.akkapatterns.api

import java.util.{UUID, Date}
import spray.json.DefaultJsonProtocol
import org.cakesolutions.scalad.mongo.sprayjson.{SprayMongo, SprayMongoCollection, DateMarshalling, UuidMarshalling}
import org.cakesolutions.akkapatterns.domain.Configured
import com.mongodb.DB
import spray.http._
import spray.routing._
import scala.concurrent.Future

case class TrackingStat(path: String,
                        ip: Option[String],
                        auth: Option[UUID],
                        kind: String,
                        timestamp: Date = new Date,
                        id: UUID = UUID.randomUUID())

trait TrackingFormats extends DefaultJsonProtocol
    with UuidMarshalling with DateMarshalling {
  protected implicit val TrackingStatFormat = jsonFormat6(TrackingStat)
}

trait TrackingMongo extends TrackingFormats with Configured {
  protected implicit val EndpointHitStartProvider = new SprayMongoCollection[TrackingStat](configured[DB], "tracking")
}

trait Tracking extends TrackingMongo {
  this: AuthenticationDirectives with HttpService =>

  private val trackingMongo = new SprayMongo

  def trackRequestT(request: HttpRequest): Any => Unit = {
    val path = request.uri.split('?')(0) // not ideal for parameters in the path, e.g. uuids.
    val ip = request.headers.find(_.name == "Remote-Address").map { _.value }
    val auth = getToken(request)
    val stat = TrackingStat(path, ip, auth, "request")

    // the HttpService dispatcher is used to execute these inserts
    Future{trackingMongo.insertFast(stat)}

    // the code is executed when called, so the date is calculated when the response is ready
    (r:Any) => (Future{trackingMongo.insertFast(stat.copy(kind = "response", timestamp = new Date))})
  }

  def trackRequestResponse: Directive0 = {
    mapRequestContext { ctx =>
      val logResponse = trackRequestT(ctx.request)
      ctx.mapRouteResponse { response => logResponse(response); response}
    }
  }
}