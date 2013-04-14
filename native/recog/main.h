#ifndef main_h
#define main_h

#include <inttypes.h>
#include "rabbit.h"
#include "preflight.h"
#include "recog.h"

namespace eigengo { namespace akka {
	
	class Main : public RabbitRpcServer {
	private:
		Preflight preflight;
		Recogniser recogniser;
	protected:
		virtual std::string handleMessage(const AmqpClient::BasicMessage::ptr_t message, const AmqpClient::Channel::ptr_t channel);
	public:
		Main(const std::string queue, const std::string exchange, const std::string routingKey);
	};
  
} }
#endif
