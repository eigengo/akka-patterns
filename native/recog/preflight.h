#ifndef preflight_h
#define preflight_h
#include <opencv2/opencv.hpp>
#include <boost/optional.hpp>

namespace eigengo { namespace akka {
	
	struct HistorgramPreflightResult {
		bool tooLight;
		bool tooDark;
		bool tooSaturated;
		bool tooUnsaturated;
	};
	
	struct FocusPreflightResult {
		bool notInFocus;
	};
	
	struct PreflightResult {
		boost::optional<FocusPreflightResult> focus;
		boost::optional<HistorgramPreflightResult> histogram;
	};
	
	class Preflight {
	private:
		FocusPreflightResult focus(const cv::Mat& image);
		HistorgramPreflightResult histogram(const cv::Mat &image);
	public:
		PreflightResult run(const cv::Mat& image);
	};
		
} }

#endif
