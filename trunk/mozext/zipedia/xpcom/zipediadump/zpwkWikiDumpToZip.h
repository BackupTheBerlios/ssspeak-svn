#ifndef ZPWKWIKIDUMPTOZIP_H_
#define ZPWKWIKIDUMPTOZIP_H_

#include <string>
#include <memory>
#include <nsCOMPtr.h>
#include <mozilla-config.h>

#include "zpwkIWikiDumpToZip.h"

class WikiDumpParser;
class nsIThread;
class myParseRunner;

class zpwkWikiDumpToZip : public zpwkIWikiDumpToZip
{
public:
  NS_DECL_ISUPPORTS
  NS_DECL_ZPWKIWIKIDUMPTOZIP

  zpwkWikiDumpToZip();
  virtual ~zpwkWikiDumpToZip();
private:
	std::auto_ptr<WikiDumpParser> m_dumpParser;
	nsCOMPtr<nsIThread> m_runner;
	nsCOMPtr<myParseRunner> m_runnable;
	long m_skipFlags;
	short m_compressionLevel;
	std::string m_errMsg;
};

#endif /*ZPWKWIKIDUMPTOZIP_H_*/
