package spray.can

import java.nio.ByteBuffer
import scala.annotation.tailrec
import akka.event.LoggingAdapter
import server.HttpServer
import server.RequestParsing.HttpMessageStartEvent
import spray.can.rendering.HttpResponsePartRenderingContext
import spray.util.ProtocolError
import spray.can.parsing._
import spray.io._
import spray.http._


object WSRequestParsing {

  lazy val continue = "HTTP/1.1 100 Continue\r\n\r\n".getBytes("ASCII")

  def apply(settings: ParserSettings, verboseErrorMessages: Boolean, log: LoggingAdapter): PipelineStage =
    new PipelineStage {
      val startParser = new EmptyRequestParser(settings)

      def build(context: PipelineContext, commandPL: CPL, eventPL: EPL): Pipelines =
        new Pipelines {
          var currentParsingState: ParsingState = startParser

          @tailrec
          final def parse(buffer: ByteBuffer) {
            currentParsingState match {
              case x: IntermediateState =>
                if (buffer.remaining > 0) {
                  currentParsingState = x.read(buffer)
                  parse(buffer)
                } // else wait for more input

              case x: HttpMessageStartCompletedState =>
                eventPL(HttpMessageStartEvent(x.toHttpMessagePart, x.connectionHeader))
                currentParsingState =
                  if (x.isInstanceOf[HttpMessageEndCompletedState]) startParser
                  else new ChunkParser(settings)
                parse(buffer)

              case x: HttpMessagePartCompletedState =>
                eventPL(HttpEvent(x.toHttpMessagePart))
                currentParsingState =
                  if (x.isInstanceOf[HttpMessageEndCompletedState]) startParser
                  else new ChunkParser(settings)
                parse(buffer)

              case Expect100ContinueState(nextState) =>
                commandPL(IOPeer.Send(ByteBuffer.wrap(continue)))
                currentParsingState = nextState
                parse(buffer)

              case ErrorState.Dead => // if we already handled the error state we ignore all further input

              case x: ErrorState =>
                handleParseError(x)
                currentParsingState = ErrorState.Dead // set to "special" ErrorState that ignores all further input
            }
          }

          def handleParseError(state: ErrorState) {
            log.warning("Illegal request, responding with status {} and '{}'", state.status, state.message)
            val msg = if (verboseErrorMessages) state.message else state.summary
            val response = HttpResponse(state.status, msg)

            // In case of a request parsing error we probably stopped reading the request somewhere in between,
            // where we cannot simply resume. Resetting to a known state is not easy either,
            // so we need to close the connection to do so.
            commandPL(HttpResponsePartRenderingContext(response))
            commandPL(HttpServer.Close(ProtocolError(state.message)))
          }

          val commandPipeline = commandPL

          val eventPipeline: EPL = {
            case x: IOPeer.Received =>
              println(x.handle.tag)
              parse(x.buffer)
            case ev => eventPL(ev)
          }
        }
    }

  ////////////// EVENTS //////////////

  // case class HttpMessageStartEvent(messagePart: HttpMessageStart, connectionHeader: Option[String]) extends Event
}