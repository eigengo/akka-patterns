#include "rtest.h"
#include "recog.h"

using namespace eigengo::akka;

class RecogniserTest : public OpenCVTest {
protected:
	Recogniser recogniser;
};

TEST_F(RecogniserTest, NoFace) {
	auto image = load("tbsit2.png");
	EXPECT_FALSE(recogniser.recognise(image, Face));
}

TEST_F(RecogniserTest, Face) {
	auto image = load("f.jpg");
	EXPECT_TRUE(recogniser.recognise(image, Face));
}