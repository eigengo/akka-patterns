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
using namespace boost;

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

void sounder(std::string replyTo, int frequency) {
  Channel::ptr_t channel = Channel::Create();
  channel->BindQueue("sound", "amq.direct", "sound.key");
  while (true) {
    try {
      BasicMessage::ptr_t response = BasicMessage::Create();
      response->Body("123");
      channel->BasicPublish("", replyTo, response);
      this_thread::sleep_for(chrono::milliseconds(frequency));
      std::cout << "sounder " << this_thread::get_id() << std::endl;
    } catch(const std::runtime_error &e) {
      std::cerr << e.what() << std::endl;
      break;
    } catch(const thread_interrupted&) {
      std::cerr << "interrupted" << std::endl;
      break;
    }
  }
  std::cout << "end" << std::endl;
}

void workerFunc() {
  Channel::ptr_t channel = Channel::Create();
  channel->BindQueue("sound", "amq.direct", "sound.key");
  std::string tag = channel->BasicConsume("sound", "", true, true, false, 1);
  thread *sounderThread = NULL;
  while (true) {
    Envelope::ptr_t env = channel->BasicConsumeMessage(tag);
    BasicMessage::ptr_t request = env->Message();
    std::string body = request->Body();
    std::string replyTo = request->ReplyTo();
    int frequency = lexical_cast<int>(body);
    
    if (sounderThread != NULL) {
      sounderThread->interrupt();
      sounderThread->join();
      delete sounderThread;
    }
    sounderThread = new thread(sounder, replyTo, frequency);
  }
  if (sounderThread != NULL) delete sounderThread;
} 

int main() {
  int count = 16;
  try {
    thread_group group;
    for (int i = 0; i < count; i++) group.add_thread(new thread(workerFunc));

    std::cout << "Quit?" << std::endl;
    group.join_all();
  } catch (std::runtime_error &e) {
    std::cout << "Error " << e.what() << std::endl;
  }
  
}
