package org.eigengo.akkapatterns.domain

import spray.json._
import java.util.{Locale, Date, UUID}
import java.text.SimpleDateFormat

trait UuidFormats {

  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }
}

trait LocaleFormats {

  implicit object LocaleFormat extends JsonFormat[Locale] {
    val LocalePattern = "(\\w+)-(\\w+)".r

    def write(obj: Locale) = JsString(obj.getLanguage + "-" + obj.getCountry)

    def read(json: JsValue) = json match {
      case JsString(LocalePattern(l, c)) => new Locale(l, c)
    }

  }

}

trait DateFormats {
  implicit object JavaUtilDateFormat extends JsonFormat[Date] {
    private val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    def write(obj: Date) = JsString(formatter.format(obj))

    def read(json: JsValue) = json match {
      case JsString(text) => formatter.parse(text)
      case x              => sys.error("Bad date format " + x)
    }
  }
}

trait NumericDateFormats {
  implicit object NumericDateFormat extends JsonFormat[Date] {
    def write(obj: Date) = JsNumber(obj.getTime)

    def read(json: JsValue) = json match {
      case JsNumber(number) => new Date(number.toLong)
      case x              => sys.error("Bad date format " + x)
    }
  }
}
