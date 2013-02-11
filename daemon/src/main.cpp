#include <SimpleAmqpClient/SimpleAmqpClient.h>

#include <boost/lexical_cast.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/exception/all.hpp>
#include <boost/thread.hpp>
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

void sounder(Channel::ptr_t channel, std::string replyTo, int frequency) {
  while (true) {
    try {
      BasicMessage::ptr_t response = BasicMessage::Create();
      response->Body("123");
      channel->BasicPublish("", replyTo, response);
      std::cout << "sounder: b" << std::endl;
      boost::this_thread::sleep_for(boost::chrono::milliseconds(frequency));
      std::cout << "sounder: in " << boost::this_thread::get_id() << std::endl;
    } catch(const std::runtime_error &e) {
      std::cerr << e.what() << std::endl;
      return;
    }
  }
}

void workerFunc() {
  Channel::ptr_t channel = Channel::Create();
  channel->BindQueue("sound", "amq.direct", "sound.key");
  std::string tag = channel->BasicConsume("sound", "", true, true, false, 1);
  // boost::thread *sounderThread = NULL;
  while (true) {
    Envelope::ptr_t env = channel->BasicConsumeMessage(tag);
    BasicMessage::ptr_t request = env->Message();
    std::string body = request->Body();
    std::string replyTo = request->ReplyTo();
    int frequency = boost::lexical_cast<int>(body);

    // if (sounderThread == NULL) sounderThread = new boost::thread(sounder, channel, request->ReplyTo(), frequency);
    boost::thread s(sounder, channel, replyTo, frequency);
    std::cout << "changing frequency to " << frequency << std::endl;
  }
  // if (sounderThread != NULL) delete sounderThread;
} 

int main() {
  int count = 16;
  try {
    for (int i = 0; i < count; i++) boost::thread workerThread(workerFunc);

    std::cout << "Quit?" << std::endl;
    int x;
    std::cin >> x;
  } catch (std::runtime_error &e) {
    std::cout << "Error " << e.what() << std::endl;
  }
  
}
