#include <iostream>
#include <stdexcept>
#include <cerrno>

#include "DumpReader.h"
#include "MonitorBlock.h"

namespace {
	void throwErrno() {
		char buff[1024];
		
		std::string err = strerror_r(errno, buff, sizeof(buff));

		throw std::logic_error(err);
	}
}
		
DumpReader::DumpReader(const std::string &dump)
 : m_dump(0), m_bz(0), m_dumpSize(0), m_bzStatus(BZ_OK)
{
	m_dump = fopen(dump.c_str(), "r");
	
	
	if (!m_dump) {
		throwErrno();
	}
	
	int status;
	
	m_bz = BZ2_bzReadOpen(&status, m_dump, 0, 0, 0, 0);
	
	if (status) {
		throw std::logic_error("bzip error");
	}
	
	if (fseek(m_dump, 0, SEEK_END) || 
		-1 == (m_dumpSize = ftell(m_dump)) ||
		fseek(m_dump, 0, SEEK_SET)) {
		throwErrno();
	}
}

DumpReader::~DumpReader()
{
  close();
}

int DumpReader::read(char *buff, int len) {	
  int res = -1;
	
  if (m_bzStatus >= 0) {
    if (m_bz) {
      MonitorBlock block(this);
      res = m_bz ? BZ2_bzRead(&m_bzStatus, m_bz, buff, len) : -1;
    }
  }

  MonitorBlock::yield();
  
  return res;
}


long DumpReader::getPos() {
  
  MonitorBlock block(this);

  long res = m_dump ? ftell(m_dump) : getSize(); // this will ensure 1.0 is returned if getProgress() is called after file is closed

  // std::cerr << __FUNCTION__ << ": " << res << std::endl;

  return res;
}

float DumpReader::getProgress() {
  float res = getSize() ? ((float)getPos() / getSize()) : 1.0; // prevent division by zero

  // std::cerr << __FUNCTION__ << ": " << res << std::endl;

  return res;	
}
bool DumpReader::isClosed() {
  return m_dump == 0;
}

void DumpReader::close() {
  if (m_bz) {
    MonitorBlock sync(this);

    int ignore;
      
    if (m_bz) BZ2_bzReadClose(&ignore, m_bz);
      
    m_bz = 0;
      
    if (m_dump) fclose(m_dump);
    m_dump = 0;
    sync.notifyAll();
  }
}
