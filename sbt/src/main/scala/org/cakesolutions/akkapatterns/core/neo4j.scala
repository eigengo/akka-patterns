package org.cakesolutions.akkapatterns.core

import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.{ GraphDatabaseService, Node }
import java.util.UUID
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.index.{IndexHits, Index}
import spray.json.{JsonParser, JsonFormat}
import org.cakesolutions.akkapatterns.domain.ApplicationFailure

case class Neo4jOperationFailure(cause: String) extends ApplicationFailure

case class NoSuchNodeFailure(id: UUID) extends ApplicationFailure

private[core] object GraphDatabaseHolder {
  val DB_PATH = "akka-patterns-store"

  val graphDatabase: GraphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH)
  /*
  val graphDatabase: GraphDatabaseService = new HighlyAvailableGraphDatabaseFactory()
                                              .newHighlyAvailableDatabaseBuilder(DB_PATH)
                                              .setConfig(Settings.setting( "org.neo4j.server.webserver.address", Settings.STRING, ""), "0.0.0.0")
                                              .setConfig(Settings.setting( "org.neo4j.server.database.mode", Settings.STRING, ""), "HA")
                                              .setConfig(Settings.setting( "online_backup_enabled", Settings.BOOLEAN, ""), Settings.TRUE )
                                              .setConfig(Settings.setting( "online_backup_port", Settings.INTEGER, Settings.MANDATORY ), "6364" )
                                              .setConfig(HaSettings.server_id, "3")
                                              .setConfig(HaSettings.ha_server, "127.0.0.1:6003")
                                              .setConfig(Settings.setting("ha.cluster_server", Settings.HOSTNAME_PORT, ""), "127.0.0.1:5003")
                                              .setConfig(Settings.setting("ha.initial_hosts", Settings.list( ",", Settings.HOSTNAME_PORT ), "" ), "127.0.0.1:5001,127.0.0.1:5002,127.0.0.1:5003")
                                              .setConfig(HaSettings.pull_interval, "5ms")
                                              .newGraphDatabase()
  */
  /**
   * Shut down net4j whenever the jvm is stopped
   */
  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run() {
      graphDatabase.shutdown()
    }
  })

}

/**
 * Provides low-level access to the underlying Neo4j graph database
 */
trait GraphDatabase {

  lazy val graphDatabase = GraphDatabaseHolder.graphDatabase

  /**
   * Performs block ``f`` within a transaction
   *
   * @param f the block to be performed
   * @tparam T the type the inner block returns
   * @return either the right of ``f``'s result or left of DB failure
   */
  def withTransaction[T](f: => T): T = {
    val tx = graphDatabase.beginTx
    try {
      val result = f
      tx.success()
      result
    } catch {
      case e: Throwable =>
        tx.failure()
        e.printStackTrace()
        throw e
    } finally {
      tx.finish()
    }
  }

  /**
   * Creates a new and empty node
   *
   * @return the newly created node
   */
  def newNode(): Node = graphDatabase.createNode()

}

/**
 * Modifies the given ``Node``s with values in the instances of ``A``
 *
 * @tparam A the A
 */
trait NodeMarshaller[A] {
  def marshal(node: Node)(a: A): Node
}

/**
 * Unmarshals given ``Node``s to create instances of ``A``
 *
 * @tparam A the A
 */
trait NodeUnmarshaller[A] {
  def unmarshal(node: Node): A
}

/**
 * Provides index for the ``A``s
 *
 * @tparam A the A
 */
trait IndexSource[A] {

  def getIndex(graphDatabase: GraphDatabaseService): Index[Node]

}

/**
 * Packages the low-level access to the ``GraphDatabase`` with richer type structures
 */
trait TypedGraphDatabase extends GraphDatabase {

  /**
   * By convention, we say that identifiable instance is a thing that contains the ``def id: UUID`` accessor
   */
  type Identifiable = { def id: UUID }

  import language.reflectiveCalls

  private def createNode[A](a: A)(implicit ma: NodeMarshaller[A]): Node = ma.marshal(newNode())(a)

  private def find[A](indexOperation: Index[Node] => IndexHits[Node])(implicit is: IndexSource[A], uma: NodeUnmarshaller[A]): Option[(A, Node)] = {
    val index = is.getIndex(graphDatabase)
    val hits = indexOperation(index)
    val result = if (hits.size() == 1) {
      val node = hits.getSingle
      Some((uma.unmarshal(node), node))
    } else {
      None
    }
    hits.close()

    result
  }

  private def findAll[A](indexOperation: Index[Node] => IndexHits[Node])(implicit is: IndexSource[A], uma: NodeUnmarshaller[A]): List[(A, Node)] = {
    import collection.JavaConversions._
    val index = is.getIndex(graphDatabase)
    val hits = indexOperation(index)
    val result = hits.iterator().map(node => (uma.unmarshal(node), node)).toList

    hits.close()

    result
  }

  private def byIdIndexOpertaion(id: UUID): Index[Node] => IndexHits[Node] = index => index.get("id", id.toString)

  /**
   * Finds one entity by its ``id`` by looking at the index returned by the ``IndexSource`` for the entity of type ``A``,
   * and unmarshalling the node pointed to in the index using the ``NodeUnmarshaller``. It returns both the unmarshalled
   * ``A`` and the ``Node`` it was pulled out from.
   *
   * @param id the entity identity
   * @param is the ``IndexSource`` for ``A``
   * @param uma the ``NodeUnmarshaller`` for ``A``
   * @tparam A the type of the entity
   * @return if found, ``Some((a: A, node: Node))``, otherwise ``None``
   */
  def findOne[A <: Identifiable](id: UUID)(implicit is: IndexSource[A], uma: NodeUnmarshaller[A]): Option[(A, Node)] =
      find(byIdIndexOpertaion(id))

  /**
   * Finds one entity by its ``id`` by looking at the index returned by the ``IndexSource`` for the entity of type ``A``,
   * and unmarshalling the node pointed to in the index using the ``NodeUnmarshaller``. It returns just the unmarshalled
   * ``A``. See the ``findOne`` if you also need the ``Node`` the entity came from.
   *
   * @param id the entity identity
   * @param is the ``IndexSource`` for ``A``
   * @param uma the ``NodeUnmarshaller`` for ``A``
   * @tparam A the type of the entity
   * @return if found, ``Some(a: A)``, otherwise ``None``
   */
  def findOneEntity[A <: Identifiable](id: UUID)(implicit is: IndexSource[A], uma: NodeUnmarshaller[A]): Option[A] =
    find(byIdIndexOpertaion(id)).map(_._1)

  /**
   * Finds one entity by applying the ``indexOperation`` to the ``Index[Node]`` returned from the ``IndexSource`` for
   * the entity of type ``A``, and unmarshalling the node pointed to in the index using the ``NodeUnmarshaller``.
   * It returns just the unmarshalled ``A``.
   *
   * @param indexOperation the operation that will find one node
   * @param is the ``IndexSource`` for ``A``
   * @param uma the ``NodeUnmarshaller`` for ``A``
   * @tparam A the type of the entity
   * @return if found, ``Some(a: A)``, otherwise ``None``
   */
  def findOneEntityWithIndex[A](indexOperation: Index[Node] => IndexHits[Node])(implicit is: IndexSource[A], uma: NodeUnmarshaller[A]): Option[A] =
    find(indexOperation).map(_._1)

  /**
   * Finds all entitites by applying the ``indexOperation`` to the ``Index[Node]`` returned from the ``IndexSource`` for
   * the entity of type ``A``, and unmarshalling the node pointed to in the index using the ``NodeUnmarshaller``.
   * It returns just the unmarshalled ``A``.
   *
   * @param indexOperation the operation that will find the required nodes
   * @param is the ``IndexSource`` for ``A``
   * @param uma the ``NodeUnmarshaller`` for ``A``
   * @tparam A the type of the entity
   * @return the ``List[A]`` of found entitites
   */
  def findAllEntitiesWithIndex[A](indexOperation: Index[Node] => IndexHits[Node])(implicit is: IndexSource[A], uma: NodeUnmarshaller[A]): List[A] =
    findAll(indexOperation).map(_._1)

  def findNodesAtEndOfRelationships[A](node: Node)(relationshipType: RelationshipType)(implicit uma: NodeUnmarshaller[A]): List[A] = {
    import collection.JavaConversions._
    import scala.language.implicitConversions

    val ri: Iterator[Relationship] = node.getRelationships(relationshipType).iterator()
    ri.map(rel => uma.unmarshal(rel.getEndNode)).toList
  }

  def updateOne[A <: Identifiable](a: A)(implicit is: IndexSource[A], ma: NodeMarshaller[A]): A = {
    val hits = is.getIndex(graphDatabase).get("id", a.id.toString)
    if (hits.size() == 1) {
      val node = hits.getSingle
      ma.marshal(node)(a)
      a
    } else {
      throw new RuntimeException("Could not find node identified by " + a.id)
    }
  }

  def addOne[A <: Identifiable](a: A)(implicit is: IndexSource[A], ma: NodeMarshaller[A]): Node = {
    val node = createNode(a)
    is.getIndex(graphDatabase).putIfAbsent(node, "id", a.id.toString)
    node
  }

  def addOneWithIndex[A <: Identifiable](a: A)(indexOperation: (Node, Index[Node]) => Unit)(implicit is: IndexSource[A], ma: NodeMarshaller[A]): Node = {
    val node = createNode(a)
    val index: Index[Node] = is.getIndex(graphDatabase)
    index.putIfAbsent(node, "id", a.id.toString)
    indexOperation(node, index)
    node
  }

}

trait SprayJsonNodeMarshalling {

  implicit def sprayJsonNodeMarshaller[A : JsonFormat] = new SprayJsonStringNodeMarshaller[A]

  implicit def sprayJsonNodeUnmarshaller[A : JsonFormat] = new SprayJsonStringNodeUnmarshaller[A]

  /**
   * Uses `spray-json` to serialise instances into JSON strings and set in Neo4j. Its dual is
   * [[org.cakesolutions.akkapatterns.core.SprayJsonNodeMarshalling.SprayJsonStringNodeUnmarshaller]]
   */
  class SprayJsonStringNodeMarshaller[A : JsonFormat] extends NodeMarshaller[A] {

    def marshal(node: Node)(a: A) = {
      val formatter = implicitly[JsonFormat[A]]
      val json = formatter.write(a).compactPrint
      node.setProperty("json", json)
      node
    }

  }

  /**
   * Uses `spray-json` to deserialise JSON strings in Neo4j. It is the dual of
   * [[org.cakesolutions.akkapatterns.core.SprayJsonNodeMarshalling.SprayJsonStringNodeMarshaller]]
   */
  class SprayJsonStringNodeUnmarshaller[A : JsonFormat] extends NodeUnmarshaller[A] {

    def unmarshal(node: Node) = {
      val json = node.getProperty("json").toString
      val parsed = JsonParser.apply(json)
      val formatter = implicitly[JsonFormat[A]]
      formatter.read(parsed)
    }
  }

}
