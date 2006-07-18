#include "nsSynthSrvListener.h"
#include "nsMemory.h"

/* ementation file */
//NS_IMPL_ISUPPORTS1_CI(nsSynthSrvListener, nsISynthSrvListener)
//NS_IMPL_ISUPPORTS1(nsSynthSrvListener, nsISynthSrvListener)
NS_IMPL_THREADSAFE_ISUPPORTS1(nsSynthSrvListener, nsISynthSrvListener)

nsSynthSrvListener::nsSynthSrvListener()
{
  /* member initializers and constructor code */
}

nsSynthSrvListener::~nsSynthSrvListener()
{
  /* destructor code */
}

/* void onMark (in string mark); */
NS_IMETHODIMP nsSynthSrvListener::OnMark(const char *mark)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void onSrvStateChange (in long state); */
NS_IMETHODIMP nsSynthSrvListener::OnSrvStateChange(PRInt32 state)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}
/* End of implementation class template. */
