#include "rabbit.h"
#include <boost/thread.hpp>

using namespace eigengo::akka;
using namespace AmqpClient;
using namespace boost;

RabbitRpcServer::RabbitRpcServer(const std::string _queue, const std::string _exchange, const std::string _routingKey):
	queue(_queue), exchange(_exchange), routingKey(_routingKey) {

}

void RabbitRpcServer::handleEnvelope(const AmqpClient::Envelope::ptr_t envelope, const AmqpClient::Channel::ptr_t channel) {
	BasicMessage::ptr_t request = envelope->Message();
	std::string replyTo = request->ReplyTo();
	
	std::string body = handleMessage(request, channel);
	
	// send the response
	BasicMessage::ptr_t response = BasicMessage::Create(body);
	response->CorrelationId(request->CorrelationId());
	channel->BasicPublish("", replyTo, response, true);
}

void RabbitRpcServer::runBlocking() {
	while (true) {
		// create a channel and bind it to a queue
		Channel::ptr_t channel = Channel::Create();
		channel->BindQueue(queue, exchange, routingKey);
		std::string tag = channel->BasicConsume(queue, "", true, true, false, 1);
			
			try {
		  // consume the request message
		  Envelope::ptr_t env = channel->BasicConsumeMessage(tag);
				handleEnvelope(env, channel);
			} catch (const std::runtime_error&) {
		  break;
		}
	}
}

void RabbitRpcServer::runAndJoin(const int threadCount) {
	thread_group group;
	for (int i = 0; i < threadCount; i++) group.create_thread(bind(&RabbitRpcServer::runBlocking, this));
	
	group.join_all();
}
