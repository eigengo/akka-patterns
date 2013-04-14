#ifndef rtest_h
#define rtest_h
#include <iostream>
#include "gtest/gtest.h"
#include <opencv2/opencv.hpp>

class OpenCVTest : public testing::Test {
protected:
	cv::Mat load(std::string fileName) {
		auto fullFileName = "../images/" + fileName;
		auto result = cv::imread(fullFileName);
		if (result.empty()) throw "Cannot load " + fullFileName;
		return result;
	}
};

#endif