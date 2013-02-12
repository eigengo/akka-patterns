#include <SimpleAmqpClient/SimpleAmqpClient.h>

#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

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
using namespace cv;

void worker() {
	// create the channel and then...
	while (true) {
		// create a channel and bind it to a queue
		Channel::ptr_t channel = Channel::Create();
		channel->BindQueue("sound", "amq.direct", "sound.key");
		std::string tag = channel->BasicConsume("sound", "", true, true, false, 1);
		
		// consume the request message
		std::cout << "Waiting..." << std::endl;
		Envelope::ptr_t env = channel->BasicConsumeMessage(tag);
		BasicMessage::ptr_t request = env->Message();
		std::string fileName = request->Body();
		std::string replyTo = request->ReplyTo();
		
		// do the processing
		while (true) {
			try {
				Mat grad_x, grad_y;
				Mat sourceGray;
				Mat grad;
				Mat abs_grad_x, abs_grad_y;
				int scale = 1;
				int delta = 0;
				int ddepth = CV_16S;
				// source -> grayscale
				Mat source = imread(fileName);
				cvtColor(source, sourceGray, CV_RGB2GRAY);
				
				// Gradient X
				Sobel(sourceGray, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
				convertScaleAbs(grad_x, abs_grad_x);
				
				// Gradient Y
				Sobel(sourceGray, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);
				convertScaleAbs(grad_y, abs_grad_y);
				
				// Total Gradient (approximate)
				addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);
								
				BasicMessage::ptr_t response = BasicMessage::Create();
				response->Body("123");
				channel->BasicPublish("", replyTo, response, true);
			} catch (const std::runtime_error&) {
				// The reply queue is gone.
				// The server has disconnected. We stop sending and go back to waiting for a new request.
				std::cout << "Disconnected" << std::endl;
				break;
			}
		}
	}
}

int main() {
  int count = 16;
  try {
    thread_group group;
    for (int i = 0; i < count; i++) group.create_thread(worker);

    std::cout << "Ready..." << std::endl;
    group.join_all();
  } catch (std::runtime_error &e) {
    std::cout << "Error " << e.what() << std::endl;
  }
  
}
