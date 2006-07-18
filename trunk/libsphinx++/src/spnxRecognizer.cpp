#include "spnxRecState.h"
#include "spnxRecSession.h"
#include "spnxRecognizer.h"
#include "spnxSphinxUtils.h"

#include "logger.h"


spnx::Recognizer *spnx::Recognizer::m_instance = 0;

spnx::Recognizer *
spnx::Recognizer::getInstance() {
  static ost::Mutex singleLock;

  {
    ost::MutexLock lock(singleLock);

    if (0 == m_instance) {
      m_instance = new Recognizer;
    }
  }

  return m_instance;
}

std::string 
spnx::Recognizer::recognize(long timeout) {
  BLOG("Recognizer::recognize()");

  auto_ref<RecState> recState(new RecState);

  recState->timeout = timeout;

  recognize(recState);

  recState->finish.wait();

  return recState->result;
}

void spnx::Recognizer::addVocabulary(const std::set<std::string> &voc) {
  m_voc.insert(voc.begin(), voc.end());
  m_voc.erase("");              // make sure no empty words - sphinx crashes
}

void spnx::Recognizer::removeVocabulary(const std::set<std::string> &voc) {
  for (std::set<std::string>::const_iterator i = voc.begin(); i != voc.end(); ++i) {
    m_voc.erase(*i);
  }
}

spnx::Recognizer::Recognizer() {
    spnx::SphinxUtils::getInstance(); // force load of sphinx
}

spnx::Recognizer::~Recognizer() {
  BLOG("~Recognizer()");
  //if (m_recSession.get() && m_recSession->isRunning()) {
//     BLOG("~Recognizer()->m_recSession->join()");
//     m_recSession->join();
//}
}

void
spnx::Recognizer::recognize(auto_ref<RecState> &recState) {
  BLOG("Recognizer::recognize()");

  SphinxUtils::getInstance()->startRec(m_voc, recState);
}

