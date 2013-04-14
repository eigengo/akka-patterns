#include "im.h"
#include <amqp.h>

using namespace eigengo::akka;

ImageMessage::ImageMessage(AmqpClient::BasicMessage::ptr_t message) throw (ImageMessageException) {
	amqp_bytes_t body = message->getAmqpBody();
	unsigned char* bodyChars = static_cast<unsigned char*>(body.bytes);
	std::vector<unsigned char> bodyVector(bodyChars, bodyChars + body.len);

	assertMagic(bodyVector);            // 4
	int count = read(bodyVector, 4);    // 8

	int dataStart = 8 + 4 * count;
	for (int i = 0; i < count; i++) {
		int size = read(bodyVector, 8 + i * 4);

		std::vector<unsigned char> image(bodyVector.begin() + dataStart, bodyVector.begin() + dataStart + size);
		m_images.push_back(image);

		dataStart += size; 
	}

}

std::vector<Image> ImageMessage::images() throw () {
	return m_images;
}

Image ImageMessage::headImage() throw (std::out_of_range) {
	return m_images.at(0);
}

ImageMessage& ImageMessage::replaceImage(int index, Image newImage) {
	m_images[index] = newImage;

	return *this;
}

void ImageMessage::writeAmqpBytes(std::vector<unsigned char>& body) throw () {
	writeBEInt32(0xface0007, body);
	writeBEInt32(m_images.size(), body);
	for (auto i = m_images.begin(); i != m_images.end(); ++i) {
		writeBEInt32((*i).size(), body);
	}
	for (auto i = m_images.begin(); i != m_images.end(); ++i) {
		body.insert(body.end(), (*i).begin(), (*i).end());
	}
}

void ImageMessage::writeBEInt32(int value, std::vector<unsigned char> &vec) {
	unsigned char b0 = (value & 0xff000000) >> 24;
	unsigned char b1 = (value & 0x00ff0000) >> 16;
	unsigned char b2 = (value & 0x0000ff00) >> 8;
	unsigned char b3 = (value & 0x000000ff);

	vec.push_back(b0);
	vec.push_back(b1);
	vec.push_back(b2);
	vec.push_back(b3);
}

void ImageMessage::assertMagic(std::vector<unsigned char> &buffer) {
	uint32_t magic = read(buffer, 0);
	if (magic != 0xface0007) throw ImageMessageException();
}

uint32_t ImageMessage::read(std::vector<unsigned char>& buffer, int offset) {
	if (buffer.size() < offset + 4) throw ImageMessageException();

	uint32_t b0 = (buffer.at(offset) & 0x000000ff)     << 24;
	uint32_t b1 = (buffer.at(offset + 1) & 0x000000ff) << 16;
	uint32_t b2 = (buffer.at(offset + 2) & 0x000000ff) <<  8;
	uint32_t b3 = (buffer.at(offset + 3) & 0x000000ff);

	return b0 + b1 + b2 + b3;
}