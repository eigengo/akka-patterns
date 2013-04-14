#include <iostream>
#include "gtest/gtest.h"
#include "preflight.h"

using namespace eigengo::akka;

class PreflightTest : public testing::Test {
protected:
	Preflight preflight;

	cv::Mat load(std::string fileName);
};

cv::Mat PreflightTest::load(std::string fileName) {
	auto fullFileName = "../images/" + fileName;
	auto result = cv::imread(fullFileName);
	if (result.empty()) throw "Cannot load " + fullFileName;
	return result;
}

TEST_F(PreflightTest, NotInFocus) {
	auto blurryImage = load("xb.jpg");
	auto blurry = preflight.run(blurryImage);
	
	EXPECT_TRUE(blurry.focus->notInFocus);
}

TEST_F(PreflightTest, InFocus) {
	auto sharpImage  = load("x.jpg");
	auto sharp  = preflight.run(sharpImage);
	
	EXPECT_FALSE(sharp.focus->notInFocus);
}
