package org.cakesolutions.akkapatterns.core.reporting

import net.sf.jasperreports.engine.{JRRewindableDataSource, JRField}
import reflect.ClassTag

private[reporting] abstract class JRAbstractProductDataSource extends JRRewindableDataSource {
  import scala.reflect.runtime.{ universe => ru }

  protected def getFieldValue[A <: Product : ClassTag](product: A, jrField: JRField): AnyRef = {
    val mirror = ru.runtimeMirror(product.getClass.getClassLoader)
    val reflect = mirror.reflect(product)
    val fieldSymbol = ru.typeOf[AnyRef].declaration(ru.newTermName(jrField.getName)).asTerm
    val fieldValue = reflect.reflectField(fieldSymbol).get
    fieldValue.asInstanceOf[AnyRef]
  }

}

class JRProductListDataSource[A <: Product : ClassTag](values: List[A]) extends JRAbstractProductDataSource {
  var index = 0

  def next() = if (values.isEmpty) false else values.size < index

  def getFieldValue(jrField: JRField) = getFieldValue(values(index), jrField)

  def moveFirst() { index = 0 }
}