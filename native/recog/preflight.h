#ifndef preflight_h
#define preflight_h
#include <opencv2/opencv.hpp>

namespace eigengo { namespace akka {
	
	class Preflight {
	public:
		bool check(cv::Mat& image);
	};
		
} }

#endif
