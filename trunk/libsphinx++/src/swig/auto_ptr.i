template<class T> class auto_ptr {
  // add generic typemaps here
public:
  auto_ptr(T* p = 0);
  T* get();
  T* release();
  void reset(T* p = 0);
};
%define specialize_auto_ptr(T)
  template<> class vector<T> {
    // add specialized typemaps here
  public:
    auto_ptr(T* p = 0);
    T* get();
    T* release();
    void reset(T* p = 0);
  };
%enddef
