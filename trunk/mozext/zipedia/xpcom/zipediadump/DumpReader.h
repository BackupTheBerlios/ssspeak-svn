#ifndef DUMPREADER_H_
#define DUMPREADER_H_

#include <prmon.h>
#include <string>
#include <fstream>
#include <libbz2/bzlib.h>

#include "Lockable.h"

class DumpReader : virtual public Lockable
{
	FILE *m_dump;
	BZFILE *m_bz;
	long m_dumpSize;
	int m_bzStatus;
public:
	DumpReader(const std::string &dump);
	virtual ~DumpReader();
	
	int read(char *buff, int len);
	
	void close();
	
	float getProgress();
	
	long getPos();
	
	long getSize() {return m_dumpSize;}

  bool isClosed();
};

#endif /*DUMPREADER_H_*/
