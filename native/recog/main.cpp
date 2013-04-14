#include "main.h"
#include "im.h"

using namespace eigengo::akka;

Main::Main(const std::string queue, const std::string exchange, const std::string routingKey) :
RabbitRpcServer::RabbitRpcServer(queue, exchange, routingKey) {
	
}

std::string Main::handleMessage(const AmqpClient::BasicMessage::ptr_t message, const AmqpClient::Channel::ptr_t channel) {
	ImageMessage imageMessage(message);
	
	auto image = imageMessage.headImage();
	
	return "foo";
}

int main(int argc, char** argv) {
	Main main("recog", "amq.direct", "recog.key");
	main.runAndJoin(8);
	return 0;
}