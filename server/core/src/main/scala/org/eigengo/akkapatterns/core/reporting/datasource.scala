package org.eigengo.akkapatterns.core.reporting

import net.sf.jasperreports.engine.{JRRewindableDataSource, JRField}

private[reporting] abstract class JRAbstractProductDataSource extends JRRewindableDataSource {

  protected def getFieldValue[A <: Product](product: A, jrField: JRField): AnyRef = {
    product.getClass.getDeclaredMethod(jrField.getName).invoke(product)
  }

}

class JRProductListDataSource[A <: Product](values: List[A]) extends JRAbstractProductDataSource {
  var index = -1

  def next() = {
    index = index + 1
    if (values.isEmpty) false
    else index < values.size
  }

  def getFieldValue(jrField: JRField) = getFieldValue(values(index), jrField)

  def moveFirst() { index = -1 }
}
