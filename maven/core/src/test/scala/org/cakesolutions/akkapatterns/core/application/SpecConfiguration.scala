package org.cakesolutions.akkapatterns.core.application

import org.cakesolutions.akkapatterns.core.Configuration
import org.apache.commons.dbcp.BasicDataSource
import org.hsqldb.jdbc.JDBCDriver

/**
 * @author janmachacek
 */
trait SpecConfiguration extends Configuration {

  configure {
    val ds = new BasicDataSource()
    ds.setDriverClassName(classOf[JDBCDriver].getCanonicalName)
    ds.setUsername("sa")
    ds.setUrl("jdbc:hsqldb:mem:test")

    ds
  }

}
