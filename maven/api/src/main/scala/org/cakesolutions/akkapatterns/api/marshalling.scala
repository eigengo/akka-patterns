package org.cakesolutions.akkapatterns.api

import cc.spray.typeconversion._
import net.liftweb.json._
import cc.spray.http.{HttpContent, ContentType}
import cc.spray.http.MediaTypes._
import net.liftweb.json.Serialization._
import cc.spray.http.ContentTypeRange

trait Marshallers extends DefaultMarshallers {
  implicit def liftJsonFormats: Formats =
    DefaultFormats + FieldSerializer[AnyRef]()

  implicit def liftJsonMarshaller[A <: Product] = new SimpleMarshaller[A] {
    val canMarshalTo = ContentType(`application/json`) :: Nil
    def marshal(value: A, contentType: ContentType) = {
      val jsonSource = write(value)
      DefaultMarshallers.StringMarshaller.marshal(jsonSource, contentType)
    }
  }

}

trait Unmarshallers extends DefaultMarshallers {
  implicit def liftJsonFormats: Formats =
    DefaultFormats + FieldSerializer[AnyRef]()

  implicit def liftJsonUnmarshaller[A <: Product : Manifest] = new SimpleUnmarshaller[A] {
    val canUnmarshalFrom = ContentTypeRange(`application/json`) :: Nil
    def unmarshal(content: HttpContent) = protect {
      val jsonSource = DefaultUnmarshallers.StringUnmarshaller(content).right.get
      parse(jsonSource).extract[A]
    }
  }

}