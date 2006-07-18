#ifndef NSSPHINX_H
#define NSSPHINX_H

#include "nsISphinx.h"

#include "../spnxRecognizer.h"

#define NS_SPHINX_CID NS_ISPHINX_IID

#define NS_SPHINX_CONTRACTID  "@mozilla.org/sphinx;1"


using spnx::RecState;
using spnx::Recognizer;

/* Header file */
class nsSphinx : public nsISphinx
{
public:
  NS_DECL_ISUPPORTS
  NS_DECL_NSISPHINX

  nsSphinx();

private:
  ~nsSphinx();

  auto_ref<RecState> m_recState;
protected:
  /* additional members */
};


#endif // NSSPHINX_H
