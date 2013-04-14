#ifndef imheader_h
#define imheader_h
#include <vector>
#include <inttypes.h>
#include <SimpleAmqpClient/Envelope.h>
#include <boost/exception/all.hpp>
#include <amqp.h>

namespace eigengo { namespace akka {

	/**
	* Image is actually a vector of its bytes
	*/
	typedef std::vector<unsigned char> Segment;

	/**
	 * Useful type aliases
	 */
	typedef Segment Image;

	/**
	 * Indicates problems with the message structure.
 	 */
	struct ImageMessageException: virtual boost::exception, virtual std::exception { };
	
	/**
	 * Wrapper around our message structure. The messages contain a header, followed by the number of 
	 * images contained within the message, followed by the same number of image sizes, followed finally
	 * by the bytes that make up the images.
	 *
	 * So, a message with one image containing just the byte 0x0a will be ``0xface0007 0x00000001 0x00000001 0a``.
	 * More realistic messages will contain images that are bigger, or will contain multiple messages. Consider these
	 * 
	 * - ``0xface0007 0x00000002 0x00000001 0x00000001 0x0a 0x0b`` is a message with two 1-byte images, 0x0a and 0x0b
	 * - ``0xface0007 0x00000003 0x00000001 0x00000002 0x00000003 0x0a 0xb1 0xb2 0xc1 0xc2 0xc3`` is a message with three images.
	 *
	 * Realistically, the image sizes will be significantly bigger than several bytes; the sum of all images cannot exceed
	 * 32bit integers.
	 */
	class ImageMessage {
	private:
		std::vector<Image> m_images;

		uint32_t read(std::vector<unsigned char>& buffer, int offset);
		void assertMagic(std::vector<unsigned char>& buffer);
		Image readImage(std::vector<unsigned char>& buffer, int* sizes);
		
		void writeBEInt32(int value, std::vector<unsigned char>& vec);
	public:
		/**
		 * Constructs the ``ImageMessage`` from the underlying AMQP message
		*/
		explicit ImageMessage(AmqpClient::BasicMessage::ptr_t message) throw (ImageMessageException);
		
		/**
		 * Writes the images to the ``body``
		 */
		void writeAmqpBytes(std::vector<unsigned char>& body) throw ();
		
		/**
		 * Replaces the image at the index ``index`` with the ``newImage``, returns the reference to the
		 * updated instance.
		 */
		ImageMessage& replaceImage(int index, Image newImage);
			
		/**
		 * Returns the vector of images contained in the message
		 */
		std::vector<Image> images() throw ();
			
		/**
		 * Returns the first image in the images vector. It is roughly the equivalent of calling ``images().get(0)``.
		 */
		Image headImage() throw (std::out_of_range);
	};

} }

#endif