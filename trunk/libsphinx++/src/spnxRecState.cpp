#include "spnxRecState.h"
#include "mythread.h"
#include "logger.h"

spnx::RecState::RecState() : timeout(-1), m_state(INITIAL) {
  BLOGF();
}

void spnx::RecState::setState(state_t state) {
  SYNCHTHIS;
  m_state = state;

  switch (state) {
  case CANCELLED:
  case TIMEOUT:
  case FINISHED:
    ready.signal();
    listen.signal();
    finish.signal();
    break;
  case LISTEN:
    listen.signal();
    break;
  case READY:
    ready.signal();
    break;
  }

  onStateChange(state);
}

spnx::RecState::~RecState() {
  BLOGF();
}
