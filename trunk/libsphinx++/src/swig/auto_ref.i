%{
#include "auto_ref.h"
%}

%include exception.i

%exception auto_ref::auto_ref {
   $action

   if (!result->get()) {
      SWIG_exception(SWIG_ValueError, "auto_ref initialized with null pointer");
   }
}

template<class T> class auto_ref {
  // add generic typemaps here
public:
  auto_ref(T* p);
  T* get();
  int count();
};
%define specialize_auto_ref(T)
  template<> class vector<T> {
    // add specialized typemaps here
  public:
    auto_ref(T* p);
    T* get();
    int count();
  };
%enddef
