#include "preflight.h"

using namespace eigengo::akka;
using namespace cv;

FocusPreflightResult Preflight::focus(const cv::Mat& image) {
	FocusPreflightResult fr;

	Mat freqs;
	Mat dst;
	int ddepth = CV_16S;
	equalizeHist(image, dst);
	medianBlur(dst, dst, 3);
	Sobel(dst, freqs, ddepth, 1, 1);
	
	const int size = 10;
	short max[size];
	for (int i = 0; i < size; i++) max[i] = -32768;
	for(int row = 0; row < freqs.rows; ++row) {
		for(int col = 0; col < freqs.cols; ++col) {
			short v = freqs.at<short>(row, col);
			for (int i = 0; i < size; i++) {
				if (v > max[i]) {
					max[i] = v;
					break;
				}
			}
		}
	}
	int sum = 0;
	for (int i = 0; i < size; i++) sum += max[i];
		
	fr.notInFocus = sum < 1100;
	
	return fr;
}

HistorgramPreflightResult Preflight::histogram(const cv::Mat &image) {
	HistorgramPreflightResult r;
	
	return r;
}

PreflightResult Preflight::run(const cv::Mat& image) {
	PreflightResult r;
	Mat grayImage;
	cv::cvtColor(image, grayImage, CV_RGB2GRAY);
	
	r.histogram = histogram(image);
	r.focus = focus(grayImage);
	
	return r;
}

