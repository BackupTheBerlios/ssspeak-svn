#ifndef SPNXRECSTATE_H
#define SPNXRECSTATE_H

#include <cc++/thread.h>
#include <string>

namespace spnx {

class RecState : public ost::Mutex {
public:
    ost::Event finish;
    ost::Event ready;
    ost::Event listen;

  std::string result;
  long timeout;

  typedef enum {
    INITIAL,
    READY,
    LISTEN,
    PROCESS,
    TIMEOUT,
    CANCELLED,
    FINISHED
  } state_t;
  
  RecState();
  virtual ~RecState();
  void setState(state_t state);
  state_t getState() const {return m_state;}
protected:
  virtual void onStateChange(state_t state) {}
private:
  volatile state_t m_state;
};

}

#endif // SPNXRECSTATE_H
