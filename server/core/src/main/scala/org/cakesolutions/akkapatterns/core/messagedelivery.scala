package org.cakesolutions.akkapatterns.core

import spray.http.{HttpResponse, HttpRequest}
import com.typesafe.config.Config
import org.cakesolutions.akkapatterns.domain._
import java.util.Properties
import akka.actor.Actor
import org.cakesolutions.akkapatterns.Nexmo
import javax.mail.internet.{MimeMessage, InternetAddress}
import javax.mail._
import scala.Some
import scala.concurrent.Future

/**
 * The address where the message should be delivered to. We currently support emailing the message or sending it
 * in a text message.
 *
 * @param mobile the mobile number (in international format, though this does not verify the formatting)
 * @param email the email address (again, without validation here)
 */
case class DeliveryAddress(mobile: Option[String], email: Option[String])

/**
 * Delivers the ``secret`` to the supplied ``address``.
 *
 * @param address the address to send the secret to
 * @param secret the secret to be delivered
 */
case class DeliverSecret(address: DeliveryAddress, secret: String)

/**
 * Delivers the ``activationLink`` to the supplied ``address``.
 *
 * @param address the address to send the secret to
 * @param userReference the user of the account needed to be activated
 * @param activationCode the activation code to activate the account
 */
case class DeliverActivationCode(address: DeliveryAddress, userReference: UserReference, activationCode: String)

/**
 * Nexmo messaging delivery system. Needs to be used with ``HttpIO`` trait, which gives access to the underlying
 * ``IOBridge`` and other Spray components.
 */
trait NexmoTextMessageDelivery {
  this: Actor =>

  import context.dispatcher

  /**
   * Returns the API key for Nexmo.
   * @return the API key
   */
  def apiKey: String

  /**
   * Returns the API secret for Nexmo
   * @return the API secret
   */
  def apiSecret: String

  /**
   * Delivers the text message ``secret`` to the phone number ``mobileNumber``. The ``mobileNumber`` needs to be in
   * full international format, without spaces, but without the leading "+", for example ``4477712345678`` for
   * a UK number ``0777 123 45678``
   *
   * @param mobileNumber the mobile number to send the message to
   * @param secret the secret to send
   */
  def deliverTextMessage(mobileNumber: String, secret: String) {
    // http://rest.nexmo.com/sms/json?api_key=3e08b948&api_secret=584f23de&from=Cake&to=*********&text=Hello
    val url = "/sms/json?api_key=%s&api_secret=%s&from=Zoetic&to=%s&text=%s" format (apiKey, apiSecret, mobileNumber, secret)
    val request = HttpRequest(spray.http.HttpMethods.POST, url)
    Nexmo.sendReceive(context.system)(request) onSuccess  {
      case response =>
      // Sort out the response. Maybe bang to health agent if we're out of credits or some such
    }
  }

}

/**
 * Configures the email session and gives the ``from`` address.
 */
trait EmailConfiguration {

  /**
   * Constructs fully initialised [[javax.mail.Session]].
   *
   * @return the properly configured session
   */
  def getMailSession: Session

  /**
   * Gets the "from" address for all e-mails
   *
   * @return the sender's address
   */
  def getFromAddress: InternetAddress
}

/**
 * The Typesafe [[com.typesafe.config.Config]] implementation of ``EmailConfiguration``
 */
trait ConfigEmailConfiguration extends EmailConfiguration {
  def config: Config

  def getFromAddress: InternetAddress = {
    new InternetAddress(config.getString("server.email.from"))
  }

  def getMailSession = {
    val props = getMailProperties
    val username = config.getString("server.email.username") // "auto@cakesolutions.net"
    val password = config.getString("server.email.password") //"dFvJGY86"

    Session.getInstance(props, new Authenticator {
      override def getPasswordAuthentication =
        new PasswordAuthentication(username, password)
    })

  }

  private def getMailProperties = {
    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.port", "587")

    props
  }

}


/**
 * Simple email message delivery system for temporary usage.
 */
trait SimpleEmailMessageDelivery {
  this: EmailConfiguration =>

  /**
   * Simple wrapper around java mail to send a text email message.
   *
   * @param messageText  - Content of the email message
   * @param recipient - email Recipient
   * @param subject - email subject
   */
  def deliverEmailMessage(messageText: String, recipient: String, subject: String): MimeMessage = {
    val session = getMailSession

    val message = new MimeMessage(session)
    message.setFrom(getFromAddress)
    // Use the built in Parse method on InternetAddress to validate recipient.
    val validatedAddress = InternetAddress.parse(recipient).map(a => a.asInstanceOf[javax.mail.Address])
    message.setRecipients(Message.RecipientType.TO, validatedAddress)
    message.setSubject(subject)
    //message.setText(messageText)
    message.setContent(messageText, "text/html")
    Transport.send(message)
    message
  }
}

/**
 * Delivers the secret to the address
 */
class MessageDeliveryActor extends Actor with NexmoTextMessageDelivery with SimpleEmailMessageDelivery
  with ConfigEmailConfiguration {

  def config = context.system.settings.config

  /**
   * Returns the API key for Nexmo.
   * @return the API key
   */
  def apiKey = "*******"

  /**
   * Returns the API secret for Nexmo
   * @return the API secret
   */
  def apiSecret = "********"

  def receive = {
    case DeliverSecret(DeliveryAddress(_, Some(email)), secret) =>
      println("Sending secret: " + secret)
      deliverEmailMessage(secret, email, "Secret delivery")
    case DeliverSecret(DeliveryAddress(Some(mobileNumber), _), secret) =>
      println("Sending activation link: ")
      deliverTextMessage(mobileNumber, secret)
    case DeliverActivationCode(DeliveryAddress(_, Some(email)), userId, activationCode) =>
      val activationLink = "<a href=\"http://localhost:3000/activate?UserReference="+userId+"&ActivationCode=" + activationCode + "\">Activate now.</a>"
      deliverEmailMessage(activationLink, email, "Activation Link")
  }
}