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

	class RabbitRpcServer {
	private:
		std::string queue;
		std::string exchange;
		std::string routingKey;
		
		void runBlocking();
	protected:
		virtual void handleEnvelope(const AmqpClient::Envelope::ptr_t envelope, const AmqpClient::Channel::ptr_t channel);
		virtual std::string handleMessage(const AmqpClient::BasicMessage::ptr_t message, const AmqpClient::Channel::ptr_t channel) = 0;
	public:
		RabbitRpcServer(const std::string queue, const std::string exchange, const std::string routingKey);
		virtual ~RabbitRpcServer() { };
		
		void runAndJoin(const int threadCount);
	};
	
} }

#endif
