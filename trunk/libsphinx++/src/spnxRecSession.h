#ifndef SPHINXRECSESSION_H
#define SPHINXRECSESSION_H

#include <string>
#include <set>

#include "mythread.h"
#include "sphinxhelper.h"
#include "auto_ref.h"

namespace spnx {
  class SphinxUtils;
  class RecState;

  class RecSession : public MyThread {
  
  private:
    RecSession(const std::set<std::string>  &voc, auto_ref<RecState> &recState);
  public:
    virtual ~RecSession();
    virtual void myRun() throw();
    virtual void myFinal() throw();
  private:
    friend class SphinxUtils;
    
    auto_ref<RecState> m_recState;

    sphinxData_t *m_sphinxData;

    const std::set<std::string>  m_voc;

  private:
    bool runPrep();
    void runFinish(bool has_utterance);
    void runBody();
  };
}

#endif // SPHINXRECSESSION_H
