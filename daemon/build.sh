#!/bin/sh

# Sanity checks for cmake, git and boost

LIB_DIR=/usr/local/lib
# Boost
if [ ! -f $LIB_DIR/libboost_context.a ] ; then
	echo "boost missing in $LIB_DIR"
	exit -1
fi

# cmake
cmake --version >/dev/null
if [[ $? != 0 ]] ; then
	echo "cmake not installed."
	exit -1
fi

# git
git --version >/dev/null
if [[ $? != 0 ]] ; then
	echo "git not installed or configured"
	exit -1
fi

# Clone the C and CPP repos
echo "Cloning the sources"
git clone git://github.com/alanxz/rabbitmq-c.git
git clone git://github.com/alanxz/SimpleAmqpClient.git rabbitmq-cpp


# Build the C code
cd rabbitmq-c
cmake . -DBUILD_STATIC_LIBS=true
cmake --build .
sudo cmake --build . --target install
cd ..

# Build the CPP code
cd rabbitmq-cpp
cmake . -DBUILD_SHARED_LIBS=false 
cmake --build .
sudo cmake --build . --target install
cd ..
