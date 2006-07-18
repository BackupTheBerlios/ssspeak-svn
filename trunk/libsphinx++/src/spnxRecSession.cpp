
#include <stdio.h>
#include <signal.h>
#include <string.h>

#include "logger.h"
#include "spnxRecSession.h"
#include "spnxRecState.h"
#include "spnxSphinxUtils.h"     // must place before including sphinx headers



namespace {
  struct TimeOutTimer : public ost::Thread {
    ost::Event &m_timeout;
    long m_timer;

    virtual ~TimeOutTimer() {}
    TimeOutTimer(long timer, ost::Event &timeout) : m_timeout(timeout), m_timer(timer) {}
    virtual void run() throw() {
      BLOG("TimeOutTimer::run()");
      if (m_timer > 0) {
        m_timeout.wait(m_timer);
        m_timeout.signal();
      }
    }
  };
  struct lower {
    void operator()(char &c) {c = tolower(c);}
  };
}

spnx::RecSession::RecSession(const std::set<std::string>  &voc, auto_ref<RecState> &recState) : m_voc(voc), m_recState(recState)
{
  BLOGF();
}

spnx::RecSession::~RecSession() 
{
  BLOGF();
}

bool spnx::RecSession::runPrep() {
  BLOGF();

  std::string model_path = SphinxUtils::getInstance()->makeLm(m_voc);
  
  SphinxUtils::getInstance()->loadLm(model_path);
  
  unlink(model_path.c_str());

  if (!(m_sphinxData = sphinxHelper_init(SphinxUtils::SAMPLE_RATE))) {
    BlockLog::info(sphinx_error);
    return false;
  }

  m_recState->setState(RecState::READY);

  ost::Event timeout;

  TimeOutTimer timeoutTimer(m_recState->timeout, timeout);

  timeoutTimer.start();
    
  int loopStatus = 0;
  /* Await data for next utterance */
  while (!timeout.wait(0) && m_recState->getState() != RecState::CANCELLED && !loopStatus) {
    loopStatus = sphinxHelper_read1st(m_sphinxData);
    
    if (0 == loopStatus) {
      sleep(200);
    }
  }

  if (-1 != loopStatus) {
    if (timeout.wait(0)) {
      m_recState->setState(RecState::TIMEOUT);
    } else {
      m_recState->setState(RecState::LISTEN);
    }
  } else {
    BlockLog::info(sphinx_error);
  }

  timeout.signal();

  timeoutTimer.join();

  if (RecState::LISTEN != m_recState->getState()) {
    return false;
  }
	
  return true;
}
  

void spnx::RecSession::runFinish(bool has_utterance) {
    BLOGF();

#if 0
  /* Power histogram dump (FYI) */
  cont_ad_powhist_dump (stdout, cont);
#endif

  char *res = sphinxHelper_finish(m_sphinxData, has_utterance);

  if (res) {
    m_recState->result = res;


    if (m_recState->result.length() && m_recState->result[m_recState->result.length() - 1] == ' ') {
      m_recState->result.resize(m_recState->result.length() - 1);
    }

    if (m_recState->result == "") {
      m_recState->result = "?"; // '?' signifies not recognition, as oposed to "" which is due to abort or timeout event
    }
  } else {
    BlockLog::info(sphinx_error);
    m_recState->result = ""; // abort, or timeout, return ""
  } 
}

void spnx::RecSession::myFinal() throw() {
  m_recState->setState(RecState::FINISHED);
}

void spnx::RecSession::myRun() throw() {
  BLOGF();

  ost::MutexLock lock(SphinxUtils::m_sphinxMutex);

  bool has_utterance = runPrep();
  
  if (!m_sphinxData) {
    return;
  }

  if (has_utterance) {
    runBody();
  }
  
  runFinish(has_utterance);

  for_each(m_recState->result.begin(), m_recState->result.end(), lower());
}

void spnx::RecSession::runBody() {
  BLOGF();

  sphinxHelper_read(m_sphinxData);
  m_recState->setState(RecState::PROCESS);
}

