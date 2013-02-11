package spray.can

import spray.io._
import java.net.InetSocketAddress
import akka.actor.{Props, ActorSystem, ActorRef}
import akka.pattern.ask
import java.util.concurrent.TimeUnit._
import scala.Some
import concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author janmachacek
 */
class WebSocketServer(ioBridge: IOBridge) extends IOServer(ioBridge) {

  override protected def createConnectionHandle(key: Key, remoteAddress: InetSocketAddress, localAddress: InetSocketAddress, commander: ActorRef, tag: Any) =
    super.createConnectionHandle(key, remoteAddress, localAddress, commander, key.channel)

  override def receive = super.receive orElse {
    case IOBridge.Received(handle, buffer) =>
      println("Channel: " + handle.tag)

      new String(buffer.array).trim match {
        case "STOP" =>
          ioBridge ! IOBridge.Send(handle, BufferBuilder("Shutting down...").toByteBuffer)
          log.info("Shutting down")
          context.system.shutdown()
        case x =>
          println(x)
          ioBridge ! IOBridge.Send(handle, buffer, Some('SentOk))
      }

    case 'SentOk =>
      log.debug("Send completed")

    case IOBridge.Closed(_, reason) =>
      log.debug("Connection closed: {}", reason)
  }

}
/*
class WebSocketService(root: String) extends Actor {
  private final val WebSocket13AcceptGuid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

  def receive = {
    case HttpRequest(GET, _root, requestHeaders, entity, protocol) if (root == _root) =>
      val key = requestHeaders.find(_.name == Names.SecWebSocketKey).get.value
      val acceptSeed = key + WebSocket13AcceptGuid
      val sha1 = sha1(acceptSeed.getBytes(Charset.forName("US-ASCII")))
      val accept = Base64.encode(sha1)

      HttpHeaders.RawHeader(Names.Upgrade, "websocket") ::
      HttpHeaders.RawHeader(Names.Connection, Names.Upgrade) ::
      HttpHeaders.RawHeader(Names.WebSocketAccept, accept)

      res.addHeader(Names.UPGRADE, WEBSOCKET.toLowerCase());
      res.addHeader(Names.CONNECTION, Names.UPGRADE);
      res.addHeader(Names.SEC_WEBSOCKET_ACCEPT, accept);

      //val responseHeaders = HttpHeaders.RawHeader(Names.Upgrade)
      //HttpResponse(StatusCodes.SwitchingProtocols,)
  }

  def sha1(data: Array[Byte]): Array[Byte] = {
    try {
      //Attempt to get a MessageDigest that uses SHA1
      val md = MessageDigest.getInstance("SHA1")
      //Hash the data
      md.digest(data)
    } catch {
      case e: NoSuchAlgorithmException =>
        //Alright, you might have an old system.
        throw new InternalError("SHA-1 is not supported on this platform - Outdated?")
    }
  }

}
*/
private[can] object Names {

  val Upgrade = "Upgrade"
  val Connection = "Connection"

  val SecWebSocketKey = "Sec-WebSocket-Key"

  val WebSocketAccept = "Sec-WebSocket-Accept"

}

object Base64 {
  val encodeTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

  def encode(fromBytes: Seq[Byte]) : String = {
    val encoded =
      group6Bits(fromBytes)
      .map(x => encodeChar(binaryToDecimal(x)))
      .mkString

    encoded + "=" * ((4 - encoded.length % 4) % 4) grouped(76) mkString "\n"
  }

  def encodeChar(i: Int) :Char = encodeTable(i)

  def binaryToDecimal(from: Seq[Int]): Int = {
    val len = from.length
    var sum = 0
    var i = 0
    while (i < len) {
      sum += from(len - i - 1) * math.pow(2, i).toInt
      i += 1
    }
    sum
  }

  def group6Bits(fromBytes: Seq[Byte]) :List[List[Int]] = {
    val BIT_LENGTH = 6
    val src = toBinarySeq(8)(fromBytes)
    trimList[Int](src.toList.grouped(BIT_LENGTH).toList, BIT_LENGTH, 0)
  }

  def toBinarySeq(bitLength: Int)(from: Seq[Byte]): Seq[Int] = {
    val result = scala.collection.mutable.Seq.fill(bitLength * from.length)(0)
    var i = 0
    while (i < bitLength * from.length) {
      result((i / bitLength) * bitLength + bitLength - (i % 8) - 1) = from(i / bitLength) >> (i % bitLength) & 1
      i += 1
    }
    result
  }

  def deleteEqual(src: String) :String = src.filter(_ != '=')

  def getEncodeTableIndexList(s: String): Seq[Int]= {
    deleteEqual(s).map(x => encodeTable.indexOf(x))
  }

  def decode(src: String) :Seq[Byte] = {
    val BIT_LENGTH = 8

    val indexSeq =
      getEncodeTableIndexList(src.filterNot(_ == '\n'))
      .map(x => toBinarySeq(6)(Seq.fill(1)(x.toByte)))

    deleteExtraZero(indexSeq.flatMap(s => s))
    .grouped(BIT_LENGTH)
    .map(binaryToDecimal(_).toByte).toSeq
  }

  def deleteExtraZero(s: Seq[Int]): Seq[Int] = {
    val BIT_LENGTH = 8
    s.take((s.length / BIT_LENGTH)  * BIT_LENGTH)
  }

  def trim[A](xs: List[A], n: Int, c: A): List[A] = {
    xs.length match {
      case l if l == n => xs
      case l if l < n  => xs ::: List.fill(n - l)(c)
      case l if l > n  => xs.take(n)
    }
  }

  def trimList[A](xss: List[List[A]], n: Int, c: A) :List[List[A]] = xss.map(xs => trim[A](xs, n, c))
}

object WSMain extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("echo-server")

  // create and start an IOBridge
  val ioBridge = new IOBridge(system).start()

  // and our actual server "service" actor
  val server = system.actorOf(
    Props(new WebSocketServer(ioBridge)),
    name = "echo-server"
  )

  // we bind the server to a port on localhost and hook
  // in a continuation that informs us when bound
  server
    .ask(IOServer.Bind("localhost", 8080))(Duration(1, SECONDS))
    .onSuccess { case IOServer.Bound(endpoint, _) =>
    println("\nBound echo-server to " + endpoint)
  }

  // finally we drop the main thread but hook the shutdown of
  // our IOBridge into the shutdown of the applications ActorSystem
  system.registerOnTermination {
    ioBridge.stop()
  }
}
