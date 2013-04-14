#include "recog.h"

using namespace eigengo::akka;
using namespace cv;

Recogniser::Recogniser() {
	if (!faceClassifier.load("face_cascade.xml")) throw "misconfigured";
}

bool Recogniser::recogniseFace(const cv::Mat &image) {
	std::vector<Rect> objects;
	faceClassifier.detectMultiScale(image, objects, 1.1, 2, CV_HAAR_FIND_BIGGEST_OBJECT | CV_HAAR_DO_CANNY_PRUNING);
	return objects.size() > 0;
}

bool Recogniser::recognise(const cv::Mat &image, const Feature feature) {
	switch (feature) {
		case Face:
			return recogniseFace(image);
		default:
			return false;
	}
}