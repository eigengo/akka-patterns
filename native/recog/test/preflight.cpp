#include "rtest.h"
#include "preflight.h"

using namespace eigengo::akka;

class PreflightTest : public OpenCVTest {
protected:
	Preflight preflight;
};

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
