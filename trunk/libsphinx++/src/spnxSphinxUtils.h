#ifndef SSSSPHINXUTILS_H
#define SSSSPHINXUTILS_H

#include <string>
#include <cc++/thread.h>
#include <set>
#include <map>

#include "auto_ref.h"

namespace spnx {
    class RecState;
    class RecSession;


class SphinxUtils {
public:

  enum {
    SAMPLE_RATE = 16000
  };
  
  ~SphinxUtils();
  void startRec(const std::set<std::string>  &voc, auto_ref<RecState> &recState);
  static SphinxUtils *getInstance();
private:
  friend class RecSession;
  SphinxUtils();
  std::string mkTmpFile(const std::string &suffix = "");
  std::string makeLm(const std::set<std::string> &voc);
  void loadLm(const std::string &lm_path);
  bool sphinxInit();
private:
  static SphinxUtils *m_instance;
  static ost::Mutex m_sphinxMutex;
  static const std::string WORDIC;
  static const std::string QUICK_LM;
  
  std::map<std::set<std::string>, std::string> m_lmMap;
  
};

}

#endif // SSSSPHINXUTILS_H
