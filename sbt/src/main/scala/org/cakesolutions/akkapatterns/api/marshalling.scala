package org.cakesolutions.akkapatterns.api

import cc.spray.typeconversion._
import net.liftweb.json._
import cc.spray.http.{HttpContent, ContentType}
import cc.spray.http.MediaTypes._
import net.liftweb.json.Serialization._
import cc.spray.http.ContentTypeRange
import java.util.UUID

trait Marshallers extends DefaultMarshallers {
  this: LiftJSON =>

  implicit def liftJsonMarshaller[A <: AnyRef] = new SimpleMarshaller[A] {
    val canMarshalTo = ContentType(`application/json`) :: Nil
    def marshal(value: A, contentType: ContentType) = {
      val jsonSource = write(value.asInstanceOf[AnyRef])
      DefaultMarshallers.StringMarshaller.marshal(jsonSource, contentType)
    }
  }

}

trait Unmarshallers extends DefaultUnmarshallers {
  this: LiftJSON =>

  implicit def liftJsonUnmarshaller[A <: Product : Manifest] = new SimpleUnmarshaller[A] {
    val canUnmarshalFrom = ContentTypeRange(`application/json`) :: Nil
    def unmarshal(content: HttpContent) = protect {
      val jsonSource = DefaultUnmarshallers.StringUnmarshaller(content).right.get
      parse(jsonSource).extract[A]
    }
  }

}

trait LiftJSON {
  implicit def liftJsonFormats: Formats =
    DefaultFormats + new UUIDSerializer + FieldSerializer[AnyRef]()

  class UUIDSerializer extends Serializer[UUID] {
    private val UUIDClass = classOf[UUID]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), UUID] = {
      case (TypeInfo(UUIDClass, _), json) => json match {
        case JString(s) => UUID.fromString(s)
        case x => throw new MappingException("Can't convert " + x + " to UUID")
      }
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x: UUID => JString(x.toString)
    }
  }

  class StringBuilderMarshallingContent(sb: StringBuilder) extends MarshallingContext {

    def marshalTo(content: HttpContent) {
      if (sb.length > 0) sb.append(",")
      sb.append(new String(content.buffer))
    }

    def handleError(error: Throwable) {}

    def startChunkedMessage(contentType: ContentType) = throw new UnsupportedOperationException
  }

}