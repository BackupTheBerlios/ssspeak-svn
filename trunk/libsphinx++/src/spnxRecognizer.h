#ifndef SPNXRECOGNIZER_H
#define SPNXRECOGNIZER_H

#include <set>
#include <string>

#include "auto_ref.h"

namespace spnx {

  class RecSession;
  class RecState;

class Recognizer {
  static Recognizer *m_instance;

  std::set<std::string> m_voc;

 private:
  Recognizer();
  ~Recognizer();
 public:
    
  void addVocabulary(const std::set<std::string> &voc);
  void removeVocabulary(const std::set<std::string> &voc);
  void recognize(auto_ref<RecState> &recState);
  std::string  recognize(long timeout = -1);
  static Recognizer *getInstance();
};


}

#endif // SPNXRECOGNIZER_H
