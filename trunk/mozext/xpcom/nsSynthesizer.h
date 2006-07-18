#include <string>

#include "nsISynthesizer.h"
#include "nsISynthSrvListener.h"
#include "nsCOMPtr.h"


class nsSynthesizer : public nsISynthesizer
{
public:
    nsSynthesizer();

    /**
     * This macro expands into a declaration of the nsISupports interface.
     * Every XPCOM component needs to implement nsISupports, as it acts
     * as the gateway to other interfaces this component implements.  You
     * could manually declare QueryInterface, AddRef, and Release instead
     * of using this macro, but why?
     */
    // nsISupports interface
    NS_DECL_ISUPPORTS

    /**
     * This macro is defined in the nsISynthesizer.h file, and is generated
     * automatically by the xpidl compiler.  It expands to
     * declarations of all of the methods required to implement the
     * interface.  xpidl will generate a NS_DECL_[INTERFACENAME] macro
     * for each interface that it processes.
     *
     * The methods of nsISynthesizer are discussed individually below, but
     * commented out (because this macro already defines them.)
     */
    NS_DECL_NSISYNTHESIZER

    void readMarkEvents();

private:
    ~nsSynthesizer();
    int m_state;
    nsresult startListenLoop();
    nsCOMPtr<nsISynthSrvListener>   m_svrListener;
    std::string m_host;
    std::string m_audioSaveDir;
    int m_port;
    std::auto_ptr<std::ostream> m_toserver;
    FILE *m_fromserver;

};
