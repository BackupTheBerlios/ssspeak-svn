#ifndef MYTHREAD_H
#define MYTHREAD_H

#include <cc++/thread.h>

#include "auto_ref.h"

#define SYNCHTHIS ost::MutexLock lock(*this);

class MyThread : protected ost::Thread, public ost::Mutex {
  ost::Event m_finish;
  ost::Event m_start;
  
  bool m_detached;
protected:
  ost::Event m_resume;
  volatile bool m_abort;
public:
  MyThread() : m_detached(false) {}
  virtual ~MyThread() {}
  void start(bool wait = false) {
    ost::Thread::start();

    if (wait) {
      m_start.wait();
    }
  }

  void detach(bool wait = false) {
    ost::Thread::detach();

    if (wait) {
      m_start.wait();
    }
  }

  void pause() {
    m_resume.reset();
  }

  void resume() {
    m_resume.signal();
  }

  bool isPaused() {
    return !m_resume.wait(0);
  }

  bool finish(bool wait = true) { 
    return wait ? m_finish.wait() : m_finish.wait(0);
  }
  
  void abort() {
    SYNCHTHIS; 
    m_abort = true;
    m_resume.signal();
  }
    
protected:
  virtual void myRun() throw() = 0;
  virtual void myFinal() throw() {}
private:
  virtual void run() throw() {
    m_resume.signal();
    m_start.signal();
    myRun();
    m_finish.signal();
  }
   
  virtual void final() throw() {
    myFinal();
    
    if (m_detached) {
      delete this;
    }
  }
};
  
#endif // MYTHREAD_H
