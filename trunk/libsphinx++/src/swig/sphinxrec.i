%module sphinxrec

%pragma(java) jniclasscode="
static {
	System.loadLibrary(\"sphinxrecJNI\");
}";

%{
#include "spnxRecState.h"
#include "spnxRecognizer.h"
  %}

%include <std_string.i>
%include <std_vector.i>

%include "ost.i"
%include "auto_ref.i"
%include "auto_ptr.i"

%rename(finish) final;

%include "spnxRecState.h"

%template(RecStateRef) auto_ref<spnx::RecState>;

%include "spnxRecognizer.h"

%template(StringVec) std::vector<std::string>;

%extend spnx::RecState {
  static spnx::RecState *newObject() {
    return new spnx::RecState();
  }
  const std::string &answer() {
    return self->result;
  }
};

%extend spnx::Recognizer {
  void addWords(const std::vector<std::string> &voc) {
    std::set<std::string> setvoc(voc.begin(), voc.end());
    self->addVocabulary(setvoc);
  }
  void removeWords(const std::vector<std::string> &voc) {
    std::set<std::string> setvoc(voc.begin(), voc.end());
    self->removeVocabulary(setvoc);
  }
};


