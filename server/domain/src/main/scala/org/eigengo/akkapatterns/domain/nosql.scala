package org.eigengo.akkapatterns.domain

import _root_.me.prettyprint.hector.api.{HConsistencyLevel, ConsistencyLevelPolicy}
import com.mongodb.MongoOptions
import _root_.me.prettyprint.cassandra.service.{OperationType, CassandraHostConfigurator}
import me.prettyprint.hector.api.factory.HFactory
import Settings.{Mongo, Cassandra}
import scala.collection.JavaConversions._

trait NoSqlConfig {

  protected def cassandra(config: Cassandra) = {
    val configurator = new CassandraHostConfigurator()
    configurator.setHosts(config.hosts)
    configurator.setMaxActive(config.connections)
    HFactory.getOrCreateCluster(config.cluster, configurator)
  }


  protected def mongo(config: Mongo) = {
    val options = new MongoOptions
    options.connectionsPerHost = config.connections
    val m = new com.mongodb.Mongo(config.hosts, options)
    m.setWriteConcern(config.concern)
    m.getDB(config.name)
  }
}

object ConsistencyPolicy extends ConsistencyLevelPolicy {
  def get(op: OperationType): HConsistencyLevel = {
    HConsistencyLevel.LOCAL_QUORUM
  }

  def get(op: OperationType, cfName: String): HConsistencyLevel = {
    (op, cfName) match {
      case (OperationType.READ, "countIndex") => HConsistencyLevel.ONE
      case (OperationType.READ, "count") => HConsistencyLevel.ONE
      case (OperationType.WRITE, "countIndex") => HConsistencyLevel.LOCAL_QUORUM
      case (OperationType.WRITE, "count") => HConsistencyLevel.LOCAL_QUORUM
      case _ => HConsistencyLevel.LOCAL_QUORUM
    }
  }
}
