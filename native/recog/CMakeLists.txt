cmake_minimum_required (VERSION 2.6) 
project (recog) 

set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/Modules)
set(CMAKE_CXX_FLAGS "-Wall -std=c++11")
enable_testing()

find_package(Boost REQUIRED COMPONENTS system chrono thread)
find_package(Rabbitmqc REQUIRED)
find_package(SimpleAmqpClient REQUIRED)
find_package(OpenCV REQUIRED)

include_directories(SYSTEM ${Rabbitmqc_INCLUDE_DIRS})
include_directories(SYSTEM ${SimpleAmqpClient_INCLUDE_DIRS})
include_directories(..)
include_directories(${Boost_INCLUDE_DIRS})
link_directories(${Boost_LIBRARY_DIRS})

add_executable(recog main.cpp recog.cpp ../rabbit.cpp ../jzon.cpp ../im.cpp)
target_link_libraries(recog ${Rabbitmqc_LIBRARY} ${Boost_LIBRARIES} ${SimpleAmqpClient_LIBRARY} ${OpenCV_LIBS})

set(GTEST_ROOT "${PROJECT_SOURCE_DIR}/../gtest-1.6.0")
add_subdirectory("${PROJECT_SOURCE_DIR}/../gtest-1.6.0" "${CMAKE_CURRENT_BINARY_DIR}/gtest")
add_subdirectory(test)

add_test(recog-test test/recog-test)