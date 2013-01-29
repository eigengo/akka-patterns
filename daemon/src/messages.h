#ifndef messages_h
#define messages_h

#include <inttypes.h>

namespace akkapatterns {
namespace daemon {

  const int32_t message_signature = 0x1000face;

  typedef struct {
    int32_t signature;
    int32_t size1;
    int32_t size2;
    
  } message_header_t;

  struct MessageError: virtual boost::exception { };
  typedef boost::error_info<struct errinfo_message_, std::string const> errinfo_message;
  
}
}
#endif
