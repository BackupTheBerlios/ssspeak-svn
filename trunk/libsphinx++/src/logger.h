#ifndef LOGGER_H
#define LOGGER_H

#include <iostream>
#include <cstdarg>
#include <stdexcept>

#define BLOG(name) spnx::BlockLog blog(name)
#define BLOGF() spnx::BlockLog blog(__PRETTY_FUNCTION__)

#define spnx_error(msg, throwing) \
if (throwing) { \
	BlockLog::error("%s:%d: %s", __FILE__, __LINE__, msg); \
} else { \
	BlockLog::info("%s:%d: %s", __FILE__, __LINE__, msg); \
}
  
namespace spnx {
struct BlockLog {
  std::string m_s;
  BlockLog(const std::string &s) : m_s(s) {
    info("%s -->\n", m_s.c_str());
  }
  ~BlockLog() {
    info("%s <--\n", m_s.c_str());
  }

  static void error(const char *fmt, ...) {
    char buff[4096];

    va_list args;
    va_start(args, fmt);
    
    vsnprintf(buff, sizeof(buff), fmt, args);

    std::cerr << buff << std::endl;

    va_end(args);


    throw std::logic_error(std::string(buff));

  }

  static void info(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    
    vfprintf(stderr, fmt, args);

    fflush(stderr);

    va_end(args);
  }
};

}

#endif // LOGGER_H
