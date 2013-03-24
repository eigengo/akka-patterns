package patterns

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.excilys.ebi.gatling.http.Headers.Names._
import akka.util.duration._
import bootstrap._

// https://github.com/excilys/gatling/wiki/First-Steps-with-Gatling
// https://github.com/excilys/gatling/wiki/Advanced-Usage
class AkkaPatternsGatling extends Simulation {
	val httpConf = httpConfig.baseURL("http://localhost:8080").disableFollowRedirect
	// val headers_login = Map("Content-Type" -> "application/json")
	val exampleScn = scenario("Example Scenario")
					.exec(
							http("make_request")
							.get("/")
							// uncomment get() and swap for post() as needed
						    // .post("/").fileBody(test.xml")
				     )

	setUp(exampleScn.users(15000).ramp(100).protocolConfig(httpConf))
}

