#include <sstream>
#include <cassert>
#include <set>

#include "nsSphinx.h"
#include "nsMemory.h"

#include "../spnxRecState.h"

namespace {
  std::set<std::string> split(const std::string &voc) {
    std::set<std::string> res;

    std::istringstream iss(voc.c_str());

    std::string l;

    while (!iss.eof()) {
      iss >> l;

      if (l == "") break;

      res.insert(l);
    }
    
    return res;
  }

  Recognizer *rec() {return Recognizer::getInstance();}
}

/* Implementation file */
NS_IMPL_ISUPPORTS1_CI(nsSphinx, nsISphinx)


  nsSphinx::nsSphinx()
{
  /* member initializers and constructor code */
}

nsSphinx::~nsSphinx()
{
  /* destructor code */
}

/* void addVocabulary (in string words); */
NS_IMETHODIMP nsSphinx::AddVocabulary(const char *words)
{
  NS_PRECONDITION(words != nsnull, "null ptr");
  
  if (!words) {
    return NS_ERROR_NULL_POINTER;
  }
  
  rec()->addVocabulary(split(words));

  return NS_OK;
}

/* void removeVocabulary (in string words); */
NS_IMETHODIMP nsSphinx::RemoveVocabulary(const char *words)
{
  NS_PRECONDITION(words != nsnull, "null ptr");
  
  if (!words) {
    return NS_ERROR_NULL_POINTER;
  }
  
  rec()->removeVocabulary(split(words));

  return NS_OK;
}

/* void recognize (); */
NS_IMETHODIMP nsSphinx::Recognize()
{
  if (m_recState.get()) {
    m_recState->setState(m_recState->CANCELLED);

    m_recState->finish.wait();
  }

  m_recState = auto_ref<RecState>(new RecState());

  rec()->recognize(m_recState);

  return NS_OK;
}

/* void ready (); */
NS_IMETHODIMP nsSphinx::Ready()
{
  if (!m_recState.get()) {
    NS_ERROR("recognize() must be called first");

    return NS_ERROR_FAILURE;
  }

  m_recState->ready.wait();

  return NS_OK;
}

/* void finish (); */
NS_IMETHODIMP nsSphinx::Finish()
{
  if (!m_recState.get()) {
    NS_ERROR("recognize() must be called first");

    return NS_ERROR_FAILURE;
  }

  m_recState->finish.wait();

  return NS_OK;
}

/* void listen (); */
NS_IMETHODIMP nsSphinx::Listen()
{
  if (!m_recState.get()) {
    NS_ERROR("recognize() must be called first");

    return NS_ERROR_FAILURE;
  }

  m_recState->listen.wait();

  return NS_OK;
}

/* void cancel (); */
NS_IMETHODIMP nsSphinx::Cancel()
{
  if (!m_recState.get()) {
    NS_ERROR("recognize() must be called first");

    return NS_ERROR_FAILURE;
  }

  m_recState->setState(RecState::CANCELLED);

  return NS_OK;
}

/* readonly attribute string result; */
NS_IMETHODIMP nsSphinx::GetResult(char * *aResult)
{
  NS_PRECONDITION(aResult != nsnull, "null ptr");

  if (!aResult) return NS_ERROR_NULL_POINTER;

  if (!m_recState.get()) {
    *aResult = (char*) nsMemory::Clone("", 1);
  } else {
    *aResult = (char*) nsMemory::Clone(m_recState->result.c_str(), m_recState->result.length() + 1);
  }

  return NS_OK;
}


/* readonly attribute unsigned long state; */
NS_IMETHODIMP nsSphinx::GetState(char **aState)
{
  NS_PRECONDITION(aState != nsnull, "null ptr");

  if (!aState) return NS_ERROR_NULL_POINTER;

  if (!m_recState.get()) {
    NS_ERROR("recognize() must be called first");

    return NS_ERROR_FAILURE;
  }

  switch (m_recState->getState()) {
  case RecState::INITIAL:
    *aState = (char*) nsMemory::Clone("STATE_INITIAL", strlen("STATE_INITIAL") + 1);
    break;
  case RecState::READY:
    *aState = (char*) nsMemory::Clone("STATE_READY", strlen("STATE_READY") + 1);
    break;
  case RecState::LISTEN:
    *aState = (char*) nsMemory::Clone("STATE_LISTEN", strlen("STATE_LISTEN") + 1);
    break;
  case RecState::PROCESS:
    *aState = (char*) nsMemory::Clone("STATE_PROCESS", strlen("STATE_PROCESS") + 1);
    break;
  case RecState::TIMEOUT:
    *aState = (char*) nsMemory::Clone("STATE_TIMEOUT", strlen("STATE_TIMEOUT") + 1);
    break;
  case RecState::CANCELLED:
    *aState = (char*) nsMemory::Clone("STATE_CANCELLED", strlen("STATE_CANCELLED") + 1);
    break;
  case RecState::FINISHED:
    *aState = (char*) nsMemory::Clone("STATE_FINISHED", strlen("STATE_FINISHED") + 1);
    break;
  default:
    assert("HDIGH!" == 0);
    break;
  }

  return NS_OK;
}


/* End of implementation class template. */
