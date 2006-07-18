#ifndef MONITORBLOCK_H_
#define MONITORBLOCK_H_

#include <prmon.h>

class Lockable;

class MonitorBlock
{
  PRMonitor *m_mon;
public:
  MonitorBlock(Lockable *lockable);
  virtual ~MonitorBlock();
  void wait();
  void notifyAll();
  static void yield();
};

#endif /*MONITORBLOCK_H_*/
