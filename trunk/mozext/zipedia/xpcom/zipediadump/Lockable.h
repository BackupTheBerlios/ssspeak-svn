#ifndef LOCKABLE_H
#define LOCKABLE_H


#include <prmon.h>

class Lockable {
 private:
  PRMonitor *m_mutex;
 public:	
  Lockable();
  virtual ~Lockable();

  friend class MonitorBlock;
};

#endif // LOCKABLE_H
