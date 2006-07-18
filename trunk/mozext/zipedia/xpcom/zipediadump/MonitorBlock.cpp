#include <prthread.h>

#include "MonitorBlock.h"
#include "Lockable.h"

MonitorBlock::MonitorBlock(Lockable *lockable)
  : m_mon(0)
{
  m_mon = lockable->m_mutex;
  PR_EnterMonitor(m_mon);
}

MonitorBlock::~MonitorBlock()
{
  PR_ExitMonitor(m_mon);
}

void MonitorBlock::wait()
{
  PR_Wait(m_mon, PR_INTERVAL_NO_TIMEOUT);
}

void MonitorBlock::notifyAll() {
  PR_NotifyAll(m_mon);
}

void MonitorBlock::yield() {
  PR_Sleep(PR_INTERVAL_NO_WAIT);
}

