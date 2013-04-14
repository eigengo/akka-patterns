#ifndef recog_h
#define recog_h
#include <opencv2/opencv.hpp>

namespace eigengo { namespace akka {

	class Recogniser {
	private:
		cv::CascadeClassifier faceClassifier;
	public:
		Recogniser();
		
		bool recognise(cv::Mat& image);
	};
  
} }

#endif
