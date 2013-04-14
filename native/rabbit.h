#ifndef rabbit_h
#define rabbit_h

#include <iostream>
#include <fstream>
#include <algorithm>
#include <iterator>
#include <vector>
#include <amqp.h>
#include <SimpleAmqpClient/SimpleAmqpClient.h>

namespace eigengo { namespace akka {

	/**
	 * Superclass for all RabbitMQ server components that attach to a queue, receive messages, perform some
	 * processing and reply with a std::string.
	 */
	class RabbitRpcServer {
	private:
		std::string queue;
		std::string exchange;
		std::string routingKey;
		
		void runBlocking();
	protected:
		virtual void handleEnvelope(const AmqpClient::Envelope::ptr_t envelope, const AmqpClient::Channel::ptr_t channel);
		/**
		 * Implement this method to handle the incoming message. The implementation must be reentrant
		 * and stateless
		 * @param message the incoming message
		 * @param channel the channel the message arrived on
		 */
		virtual std::string handleMessage(const AmqpClient::BasicMessage::ptr_t message, const AmqpClient::Channel::ptr_t channel) = 0;
	public:
		/**
		 * Constructs the instance of the ``RabbitRpcServer`` attaching to the given ``queue``,
		 * on the ``exchange`` and ``routingKey``
		 * @param queue the queue name; the queue must exist
		 * @param exchange the exchange name; the exchange must exist
		 * @param routingKey the routing key for the queue on the exchange
		 */
		RabbitRpcServer(const std::string queue, const std::string exchange, const std::string routingKey);
		virtual ~RabbitRpcServer() { };
		
		/**
		 * Receives the messsages from the AMQP broker using the specified number of threads
		 * @param threadCount the number of threads
		 */
		void runAndJoin(const int threadCount);
	};
	
} }

#endif
