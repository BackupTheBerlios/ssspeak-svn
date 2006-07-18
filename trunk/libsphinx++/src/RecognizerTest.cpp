#include <iostream>
#include "spnxRecognizer.h"
#include "spnxRecState.h"


const char *voc1_init[] = {"HELLO", "WORLD", "NAVIGATE"};
const char *voc2_init[] = {"ZERO", "STOP", "RESUME", "JERUSALEM"};

struct Print {
  void operator()(const std::string &s) {
    std::cout << " " << s << " ";
  }
};

void printVoc(const std::set<std::string> &voc) {
  for_each(voc.begin(), voc.end(), Print());
}

namespace {
  class RecStateImplTest : public spnx::RecState {
  public:

    virtual ~RecStateImplTest() {}
  protected:
    void onStateChange(state_t state) {
      switch (state) {
      case READY:
        std::cout << "############### READY #############" << std::endl;
        break;
      case LISTEN:
        std::cout << "############### LISTEN #############" << std::endl;
        break;
      case PROCESS:
        std::cout << "############### PROCESS #############" << std::endl;
        break;
      case TIMEOUT:
        std::cout << "############### TIMEOUT #############" << std::endl;
        break;
      case CANCELLED:
        std::cout << "############### CANCELLED #############" << std::endl;
        break;
      case FINISHED:
        std::cout << "############### FINISHED #############" << std::endl;
        break;
      }
    }
  };
}

bool recSession(spnx::Recognizer *rec, const std::set<std::string> &voc, const std::string &target) {
  bool status = true;

  std::cout << "going to recognize on of: ";
  printVoc(voc);
  std::cout << std::endl;

  rec->addVocabulary(voc);

  std::string res;

  long seconds = 5;

  auto_ref<spnx::RecState>  recState(new RecStateImplTest());

  recState->timeout = seconds * 1000;

  rec->recognize(recState);


  recState->ready.wait();

  std::cout << "############### SAY (within " << seconds << " seconds): " << target << " ##################" << std::endl;

  recState->finish.wait();
    
  std::cout << "result: " << recState->result << std::endl;

  if (recState->result != target) {
    status = false;
  }

  rec->removeVocabulary(voc);


  return status;
}

static void usage(const char *argv0) {
  std::cerr << "usage: " << argv0 << " <word..>" << std::endl;
  exit(1);
}

int main(int argc, char **argv) {
  if (argc < 2) {
    usage(argv[0]);
  }

  int status = 0;
  spnx::Recognizer *rec = 0;

  try {
    rec = spnx::Recognizer::getInstance();
    
    
    std::set<std::string> voc(&argv[1], &argv[argc]);
    
    if (!recSession(rec, voc, argv[1])) {
      return -1;
    }

    return 0;
  } catch (std::exception &ex) {
    std::cerr << ex.what() << std::endl;
  } catch (...) {
      std::cerr << "exception ..." << std::endl;
  }   

  return 1;
}




  
  
