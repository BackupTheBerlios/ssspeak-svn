#include <stdexcept>

#include "Lockable.h"


Lockable::Lockable() : m_mutex(0) {
  m_mutex = PR_NewMonitor();

  if (!m_mutex) {
    throw std::logic_error("Lockable::Lockable(): can't create monitor");
  }
}

Lockable::~Lockable() {
  if (m_mutex) PR_DestroyMonitor(m_mutex);
}
