#Find the SimpleAmqpClient library

INCLUDE(LibFindMacros)

# Find the include directories
FIND_PATH(SimpleAmqpClient_INCLUDE_DIR
	NAMES SimpleAmqpClient/SimpleAmqpClient.h
    HINTS ${SimpleAmqpClient_DIR}/include
	)

FIND_LIBRARY(SimpleAmqpClient_LIBRARY
	NAMES SimpleAmqpClient
    HINTS ${SimpleAmqpClient_DIR}/lib
	)

SET(SimpleAmqpClient_PROCESS_INCLUDES SimpleAmqpClient_INCLUDE_DIR)
SET(SimpleAmqpClient_PROCESS_LIBS SimpleAmqpClient_LIBRARY)

LIBFIND_PROCESS(SimpleAmqpClient)
