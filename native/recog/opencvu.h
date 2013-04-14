#ifndef opencvu_h
#define opencvu_h

#include <vector>
#include <fstream>

std::vector<unsigned char> load(std::string fileName) {
  std::ifstream testFile(fileName.data(), std::ios::binary);
  std::vector<unsigned char> fileContents((std::istreambuf_iterator<char>(testFile)),
                                           std::istreambuf_iterator<char>());
  return fileContents;
}

void save(const std::vector<unsigned char>& data, const std::string fileName) {
  std::ofstream file(fileName.data(), std::ofstream::binary);
  std::copy(data.begin(), data.end(), std::ostreambuf_iterator<char>(file));
}

#endif