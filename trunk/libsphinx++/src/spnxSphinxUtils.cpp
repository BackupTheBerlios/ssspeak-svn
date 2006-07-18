#define _BSD_SOURCE
#include <stdio.h>

#include <list>
#include <fstream>

#include "spnxSphinxUtils.h"
#include "spnxRecSession.h"
#include "logger.h"


#ifndef QUICK_LM_PATH
#define QUICK_LM_PATH "quick_lm.pl"
#endif

#define SPNX_RESOLVE_ENV_OVERRIDABLE_PATH(def) getenv(#def) ? getenv(#def) : def

ost::Mutex spnx::SphinxUtils::m_sphinxMutex;
spnx::SphinxUtils * spnx::SphinxUtils::m_instance = 0;

const std::string spnx::SphinxUtils::QUICK_LM = SPNX_RESOLVE_ENV_OVERRIDABLE_PATH(QUICK_LM_PATH);

 const std::string spnx::SphinxUtils::WORDIC = SPNX_RESOLVE_ENV_OVERRIDABLE_PATH(WORDS_DIC);





spnx::SphinxUtils::SphinxUtils() {
  sphinxHelper_sphinxInit(WORDIC.c_str());
}

std::string spnx::SphinxUtils::mkTmpFile(const std::string &suffix) {
  char buff[L_tmpnam];
    
  std::string res = tmpnam(buff);

  return res + suffix;
}

std::string spnx::SphinxUtils::makeLm(const std::set<std::string> &voc) {
  
  std::map<std::set<std::string>, std::string>::const_iterator i = m_lmMap.find(voc);
  
  if (i != m_lmMap.end()) {
  	return i->second;
  }
  	
  std::string wordfile = mkTmpFile(".voc");
  
  {
    std::ofstream outf(wordfile.c_str());
      
    for (std::set<std::string>::const_iterator i = voc.begin();
         i != voc.end();
         ++i) {
        
      if (!outf) {
	BlockLog::error("can not write to outfile %s", wordfile.c_str());
      }
      outf << "<s> " << *i << " </s>\n";
    }
      
    outf.close();
  }

  std::string lmfile = mkTmpFile(".lm");

  std::string cmd = QUICK_LM;
  cmd += " -s ";
  cmd += wordfile;
  cmd += " -o ";
  cmd += lmfile;

  int status = system(cmd.c_str());

  if (-1 == status ||  WEXITSTATUS(status) != 0) {
    BlockLog::error("quick_lm.pl failed: commadn was %s", cmd.c_str());
  }
  
  unlink(wordfile.c_str());
  
  m_lmMap[voc] = lmfile;
  
  return lmfile;
}

spnx::SphinxUtils *
spnx::SphinxUtils::getInstance() {
  static ost::Mutex mutex;

  ost::MutexLock lock(mutex);


  if (0 == m_instance) {
    m_instance = new SphinxUtils;
  }

  return m_instance;
}



void spnx::SphinxUtils::loadLm(const std::string &lm_path) {
  sphinxHelper_loadLm(lm_path.c_str());
}
void
spnx::SphinxUtils::startRec(const std::set<std::string>  &voc, auto_ref<RecState> &recState) {
  RecSession *session = new RecSession(voc, recState);

  session->detach();
}


