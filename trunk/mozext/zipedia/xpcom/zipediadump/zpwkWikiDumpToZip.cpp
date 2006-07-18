#include <cassert>

#include <stdexcept>

#include <nscore.h>
#include <plstr.h>
#include <nsError.h>
#include <nsIServiceManager.h>
#include <nsServiceManagerUtils.h>
#include <nsIThread.h>
#include <nsIRunnable.h>
#include <nsIProxyObjectManager.h>

#include "zpwkWikiDumpToZip.h"

#include "DumpReader.h"
#include "WikiZipPageWriter.h"
#include "WikiDumpParser.h"
#include "MonitorBlock.h"

class myParseRunner : public nsIRunnable {
public:
  NS_DECL_ISUPPORTS

  myParseRunner(WikiDumpParser *parser)
   : m_parser(parser) {
  }

  virtual ~myParseRunner() {
  }

  NS_IMETHOD Run() {
    try {		
      m_parser->parse();

      //m_progListener->Done();
    } catch (...) {
      m_parser->error("...");

      assert(0 != "HDIGH!"); 
      // break point opportunity
      return NS_ERROR_FAILURE;
    }
    return NS_OK;
  }
private:
  WikiDumpParser *m_parser;
};

NS_IMPL_THREADSAFE_ISUPPORTS1(myParseRunner, nsIRunnable);
//NS_IMPL_ISUPPORTS1(myParseRunner, nsIRunnable);

/* Implementation file */
NS_IMPL_ISUPPORTS1(zpwkWikiDumpToZip, zpwkIWikiDumpToZip);

static NS_DEFINE_IID(kProxyObjectManagerCID, NS_PROXYEVENT_MANAGER_CID);

zpwkWikiDumpToZip::zpwkWikiDumpToZip()
  : m_skipFlags(SKIP_NONE), m_compressionLevel(DEFAULT_COMPRESSION)
{
  /* member initializers and constructor code */
}

zpwkWikiDumpToZip::~zpwkWikiDumpToZip()
{
  /* destructor code */
}


/* void wiki2zip (in string txt, in string location); */
NS_IMETHODIMP zpwkWikiDumpToZip::Wiki2zip(const char *dump, const char *outdir)
{
  if (!dump || !outdir)
    return NS_ERROR_NULL_POINTER;

  try {
    m_dumpParser.reset(new WikiDumpParser(dump, outdir));
    
    m_dumpParser->skipTalk(m_skipFlags & SKIP_TALK);
    m_dumpParser->skipUser(m_skipFlags & SKIP_USER);
    m_dumpParser->skipImage(m_skipFlags & SKIP_IMAGE);
    m_dumpParser->setCompression(m_compressionLevel);
  } catch (std::exception &ex) {
    m_errMsg = ex.what();

    return NS_ERROR_FAILURE;
  }

  m_errMsg = "";

  nsresult rv = NS_OK;
  
  if (NS_SUCCEEDED(rv)) {
    m_runnable = new myParseRunner(m_dumpParser.get());
    
    rv = NS_NewThread(getter_AddRefs(m_runner), m_runnable);
    
    //rv = run->Run();
    
    if (NS_SUCCEEDED(rv)) {
      return NS_OK;
    }
  }
  
  return NS_ERROR_FAILURE;
}

/* void abort (); */
NS_IMETHODIMP zpwkWikiDumpToZip::Abort()
{
  if (m_dumpParser.get()) {
    m_dumpParser->abort();
    return NS_OK;
  }
  return NS_ERROR_FAILURE;
}

/* readonly attribute string err; */
NS_IMETHODIMP zpwkWikiDumpToZip::GetErr(char * *aErr)
{

  NS_ENSURE_ARG(aErr);
  
  if (m_dumpParser.get()) {
    MonitorBlock sync(m_dumpParser.get());

    m_errMsg = m_dumpParser->errMsg();
  }

  *aErr = PL_strdup(m_errMsg.c_str());

  return NS_OK;
}

/* readonly attribute float progress; */
NS_IMETHODIMP zpwkWikiDumpToZip::GetProgress(float *aProgress)
{
  NS_ENSURE_ARG(aProgress);

  if (m_dumpParser.get()) {
    *aProgress = m_dumpParser->getProgress();
    return NS_OK;
  } else {
    return NS_ERROR_FAILURE;
  }
}

/* attribute boolean skipTalk; */
NS_IMETHODIMP zpwkWikiDumpToZip::GetSkipFlags(PRInt32 *aSkipFlags)
{
  NS_ENSURE_ARG(aSkipFlags);

  *aSkipFlags = m_skipFlags;
  
  return NS_OK;
}
NS_IMETHODIMP zpwkWikiDumpToZip::SetSkipFlags(PRInt32 aSkipFlags)
{
  m_skipFlags = aSkipFlags;
  return NS_OK;
}

NS_IMETHODIMP zpwkWikiDumpToZip::GetCompression(PRInt16 *aCompression)
{
  NS_ENSURE_ARG(aCompression);

  *aCompression = m_compressionLevel;

  return NS_OK;
}
NS_IMETHODIMP zpwkWikiDumpToZip::SetCompression(PRInt16 aCompression)
{
  m_compressionLevel = aCompression;
  
  return NS_OK;
}
