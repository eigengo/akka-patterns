package org.eigengo.akkapatterns.core.recog

import spray.json.{JsString, JsValue, JsonFormat, DefaultJsonProtocol}

sealed trait Feature
case object SquareFeature extends Feature
case object CircleFeature extends Feature
case object FaceFeature extends Feature

case class SessionConfiguration(requiredFeatures: List[Feature])

case class RecogResult(accepted: Boolean)

case class RecogSessionAccepted(result: RecogResult)

case class RecogSessionRejected(result: RecogResult)

case class RecogSessionCompleted(token: String)

trait RecogFormats extends DefaultJsonProtocol {

  implicit object FeatureFormat extends JsonFormat[Feature] {
    private val Square = JsString("square")
    private val Circle = JsString("circle")
    private val Face = JsString("face")

    def write(obj: Feature) = obj match {
      case SquareFeature => Square
      case CircleFeature => Circle
      case FaceFeature   => Face
    }

    def read(json: JsValue) = json match {
      case Square => SquareFeature
      case Circle => CircleFeature
      case Face   => FaceFeature
      case _      => sys.error("Bad json")
    }
  }

  implicit val SessionConfigurationFormat = jsonFormat1(SessionConfiguration)
  implicit val RecogResultFormat = jsonFormat1(RecogResult)
  implicit val RecogSessionAcceptedFormat = jsonFormat1(RecogSessionAccepted)
  implicit val RecogSessionRejectedFormat = jsonFormat1(RecogSessionRejected)
  implicit val RecogSessionCompletedFormat = jsonFormat1(RecogSessionCompleted)

}