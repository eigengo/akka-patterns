set(test_EXECUTABLE recog-test)
include_directories(${gtest_SOURCE_DIR}/include ${gtest_SOURCE_DIR} ${PROJECT_SOURCE_DIR} "${PROJECT_SOURCE_DIR}/..")

add_executable(${test_EXECUTABLE} recog.cpp)

target_link_libraries(${test_EXECUTABLE} ${Rabbitmqc_LIBRARY} ${Boost_LIBRARIES} ${SimpleAmqpClient_LIBRARY} ${OpenCV_LIBS} gtest gtest_main)

file(COPY ${PROJECT_SOURCE_DIR}/images DESTINATION ${CMAKE_CURRENT_BINARY_DIR})
