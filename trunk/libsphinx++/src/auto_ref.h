#ifndef AUTO_REF_H
#define AUTO_REF_H

#include <cassert>
#include <cc++/thread.h>

class auto_refTest;


template <class X> class auto_lock {
  ost::Mutex &m_mutex;
  ost::MutexLock m_lock;
  X *m_p;

 public:
  auto_lock(X *p, ost::Mutex &mutex) : m_mutex(mutex), m_lock(mutex), m_p(p) {}
    auto_lock(const auto_lock &ot) : m_mutex(ot.m_mutex), m_lock(ot.m_mutex), m_p(ot.m_p) {}
      X &operator*() {return m_p;}
      X *operator->() {return m_p;}
      operator X*() {return m_p;}
      const X &operator*() const {return m_p;}
      const X *operator->() const {return m_p;}

 private:
      auto_lock &operator=(const auto_lock &ot);
};

template <class X> class auto_ref {
 private:
  friend class auto_refTest;
    
  struct data
  {
    friend class auto_ref<X>;
    data(X* p=0): ptr(p), ref_count(1) {}
    ~data() {if (ptr) delete ptr;}
    X* ptr;
    volatile int ref_count;
    ost::Mutex mutex;
  } *d;
  auto_ref& copy(const auto_ref& a) throw() {
    if (&a != this) {
      assert(d->ref_count > 0); // it is always initializes to 1 at first, deleted when ref count reach 0

      {
	ost::MutexLock lock1(d->mutex);
	ost::MutexLock lock2(a.d->mutex);

	if (--d->ref_count == 0)
	  delete d;

	d = a.d;
	d->ref_count++;
      }
    }

    return *this;
  }
    
 public:
  typedef X element_type;
  explicit auto_ref(X* p = 0) throw() : 
    d(new data(p)) {}
    auto_ref(const auto_ref& a) throw() : 
      d(new data) {*this = a;}
      auto_ref& operator=(const auto_ref& a) throw() {
	return copy(a);
      }
      ~auto_ref() {
	if (--d->ref_count <= 0)
	  delete d;
      }
      operator X *() throw() { return d->ptr; }
      operator const X *() const throw() { return d->ptr; }
      const X& operator*() const throw() { ost::MutexLock lock(d->mutex); return auto_lock<X>(d->ptr, d->mutex); }
      X& operator*() throw() { ost::MutexLock lock(d->mutex); return auto_lock<X>(d->ptr, d->mutex); }
      const X* operator->() const throw() { ost::MutexLock lock(d->mutex); return auto_lock<X>(d->ptr, d->mutex); }
      X* operator->() throw() { ost::MutexLock lock(d->mutex); return auto_lock<X>(d->ptr, d->mutex); }
      X* get() const throw() { return d->ptr; }
      int count() {return d->ref_count;}
};

#endif //AUTO_REF_H
