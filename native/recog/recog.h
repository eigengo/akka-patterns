#ifndef recog_h
#define recog_h
#include <opencv2/opencv.hpp>

namespace eigengo { namespace akka {
	
	enum Feature {
		Circle,
		Square,
		Face
	};

	class Recogniser {
	private:
		cv::CascadeClassifier faceClassifier;
		
		bool recogniseFace(const cv::Mat &image);
	public:
		Recogniser();
		
		bool recognise(const cv::Mat &image, const Feature feature);
	};
  
} }

#endif
