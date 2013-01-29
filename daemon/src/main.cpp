#include <SimpleAmqpClient/SimpleAmqpClient.h>

#include <boost/lexical_cast.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/exception/all.hpp>
#include <iostream>
#include <stdlib.h>
#include <amqp.h>

#include "messages.h"

using namespace AmqpClient;
using namespace akkapatterns::daemon;

std::string process(BasicMessage::ptr_t request) {
  const amqp_bytes_t& bytes = request->getAmqpBody();
  if (bytes.len < sizeof(message_header_t)) throw MessageError() << errinfo_message("message too small");
  const message_header_t* header = static_cast<message_header_t*>(bytes.bytes);
  if (header->signature != message_signature) throw MessageError() << errinfo_message("bad signature");
  
  // we're good.
  size_t totalSize = sizeof(message_header_t) + header->size1 + header->size2;
  if (bytes.len != totalSize) throw MessageError() << errinfo_message("bad message size");

  // do the processing
  return "{\"success\":true}";
}

int main() {
  try {
    Channel::ptr_t channel = Channel::Create();
    
    //channel->DeclareQueue("faceverify");
    channel->BindQueue("faceverify", "amq.direct", "identity.f2f");
    
    std::string tag;
    tag = channel->BasicConsume("faceverify", "", true, true, false, 2);
    
    while (true) {
      // consume the message
      Envelope::ptr_t env = channel->BasicConsumeMessage(tag);
      BasicMessage::ptr_t request = env->Message();
      try {
        // process it
        std::string body = process(request);
        // then reply to this message
        BasicMessage::ptr_t response = BasicMessage::Create();
        response->Body(body);
        channel->BasicPublish("amq.direct", request->ReplyTo(), response);
      } catch (MessageError &e) {
        const std::string* msg = boost::get_error_info<errinfo_message>(e);
        std::cerr << (*msg) << std::endl;
      }
    }
  } catch (std::runtime_error &e) {
    std::cout << "Error " << e.what() << std::endl;
  }
  
}
