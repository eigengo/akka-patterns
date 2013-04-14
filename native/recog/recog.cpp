#include "recog.h"

using namespace eigengo::akka;
using namespace cv;

Recogniser::Recogniser() {
	if (!faceClassifier.load("face_cascade.xml")) throw "misconfigured";
}

bool Recogniser::recognise(cv::Mat &image) {
	return true;
}