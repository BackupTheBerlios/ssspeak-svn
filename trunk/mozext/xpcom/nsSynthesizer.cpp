
#include <fstream>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>

#include "nsSynthesizer.h"
#include "nsSynthSrvListener.h"
#include "nsIThread.h"
#include "nsIRunnable.h"
#include "nspr.h"
#include "nsCOMPtr.h"
#include "nsIServiceManager.h"
#include "nsMemory.h"

#include "nsEmbedString.h"
////////////////////////////////////////////////////////////////////////


NS_IMPL_ISUPPORTS1(nsSynthesizer, nsISynthesizer)

  nsSynthesizer::nsSynthesizer() : m_svrListener(0), m_port(-1), m_fromserver(0)
{
  /* member initializers and constructor code */
}

nsSynthesizer::~nsSynthesizer()
{
  /* destructor code */
}


/* readonly attribute long state; */
NS_IMETHODIMP nsSynthesizer::GetState(PRInt32 *aState)
{
    NS_PRECONDITION(aState != nsnull, "null ptr");
    if (! aState)
        return NS_ERROR_NULL_POINTER;

    *aState = m_state;

    return NS_OK;
}

/* attribute string audioSaveDir; */
NS_IMETHODIMP nsSynthesizer::GetAudioSaveDir(char * *aAudioSaveDir)
{
    NS_PRECONDITION(aAudioSaveDir != nsnull, "null ptr");
    if (! aAudioSaveDir)
        return NS_ERROR_NULL_POINTER;

    *aAudioSaveDir = (char*) nsMemory::Clone(m_audioSaveDir.c_str(), m_audioSaveDir.length() + 1);

    return NS_OK;
}

NS_IMETHODIMP nsSynthesizer::SetAudioSaveDir(const char * aAudioSaveDir)
{
    NS_PRECONDITION(aAudioSaveDir != nsnull, "null ptr");
    if (! aAudioSaveDir)
        return NS_ERROR_NULL_POINTER;

    m_audioSaveDir = aAudioSaveDir;

    return NS_OK;
}

NS_IMETHODIMP nsSynthesizer::SetSvrListener(nsISynthSrvListener * aSvrListener)
{
    NS_PRECONDITION(aSvrListener != nsnull, "null ptr");
    if (! aSvrListener)
        return NS_ERROR_NULL_POINTER;

    m_svrListener = aSvrListener;

    return NS_OK;
}


/* void synth (in string ssmlpath); */
NS_IMETHODIMP nsSynthesizer::Synth(const char *ssmlpath)
{
    NS_PRECONDITION(ssmlpath != nsnull, "null ptr");
    if (! ssmlpath)
        return NS_ERROR_NULL_POINTER;

    if (!m_toserver.get()) {
      NS_ERROR("server connection not initialied");
      return NS_ERROR_FAILURE;
    }
    if (!m_toserver->good()) {
      NS_ERROR("server connection broken");
      return NS_ERROR_FAILURE;
    }
    
    *m_toserver << "synth:" << ssmlpath << std::endl;

    return NS_OK;
}

/* void dump (in string ssmlpath, in string outpath); */
NS_IMETHODIMP nsSynthesizer::Dump(const char *ssmlpath, const char *outpath)
{
  if (! ssmlpath || !outpath)
        return NS_ERROR_NULL_POINTER;

  if (!m_toserver.get()) {
    NS_ERROR("server connection not initialied");
    return NS_ERROR_FAILURE;
  }
  if (!m_toserver->good()) {
    NS_ERROR("server connection broken");
    return NS_ERROR_FAILURE;
  }
  
  *m_toserver << "dump:" << strlen(ssmlpath) << ":" << strlen(outpath) << ssmlpath << ":" << outpath << std::endl;
  
  return NS_OK;
}


/* void stop (); */
NS_IMETHODIMP nsSynthesizer::Stop()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void shutdown (); */
NS_IMETHODIMP nsSynthesizer::Shutdown()
{
    if (m_state) {             // not running

        m_toserver.reset(0);
        if (m_fromserver) {
          fclose(m_fromserver);
          m_fromserver = 0;
        }
        

        m_state = 0;
    }
    return NS_OK;
}

/* void pause (); */
NS_IMETHODIMP nsSynthesizer::Pause()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void resume (); */
NS_IMETHODIMP nsSynthesizer::Resume()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void initSocket (in string host, in long port, in nsISynthSrvListener svrListener); */
NS_IMETHODIMP nsSynthesizer::InitSocket(const char *host, PRInt32 port, nsISynthSrvListener *svrListener)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}


class nsListenRunner : public nsIRunnable {
public:
  NS_DECL_ISUPPORTS

  NS_IMETHOD Run() {
    m_synthesizer.readMarkEvents();
    return NS_OK;
  }

  nsListenRunner(nsSynthesizer &synthesizer) : m_synthesizer(synthesizer) {
  }

private:
  nsSynthesizer &m_synthesizer;
};

NS_IMPL_THREADSAFE_ISUPPORTS1(nsListenRunner, nsIRunnable)

/* void initFifos (in string toserver, in string fromserver); */
NS_IMETHODIMP nsSynthesizer::InitFifos(const char *toserver, const char *fromserver, nsISynthSrvListener *svrListener)
{
    NS_PRECONDITION(toserver != nsnull && fromserver != nsnull, "null ptr");

  m_svrListener = svrListener;
  
  if (!toserver || !fromserver) {
    return NS_ERROR_NULL_POINTER;
  }

  m_toserver.reset(new std::ofstream(toserver));

  m_fromserver = fopen(fromserver, "r");

  if (!m_fromserver || !m_toserver->good()) {
    if (m_fromserver) {
      fclose(m_fromserver);
    }
    return NS_ERROR_FILE_NOT_FOUND;
  }
  
  nsCOMPtr<nsIThread> runner;
  nsresult rv = NS_NewThread(getter_AddRefs(runner), new nsListenRunner(*this));
    
  if (NS_FAILED(rv)) {
    printf("failed to create thread\n");
  }
  return rv;
}

namespace {
  bool getline(FILE *ins, std::string &line) {
    line = "";
    char c;
    
    while (EOF != (c = fgetc(ins))) {
      if (c == '\n') {
        break;
      }
      line += c;
    }
    
    return c != EOF;
  }
}
    
void nsSynthesizer::readMarkEvents() {

  std::string line;

  while (getline(m_fromserver, line)) {
    
    if (m_svrListener) {
      m_svrListener->OnMark(line.c_str());
    }
  }
}


bool getHostAddress(const char *hostname, u_long *res) 
{
  struct hostent *hostptr;
  if((hostptr = gethostbyname(hostname)) == 0)
    {
      return false;
    }
  *res = *(u_long *)hostptr->h_addr_list[0]; 
  return  true;
}

int
socketInit(const char *hostname, int _port)
{

  unsigned long host_address;
  int port = htons(_port);

  int sockfd = ::socket(AF_INET, SOCK_STREAM, 0);

  if (socket < 0) {
    printf("socket() failed\n");
    return -1;;
  }    
  if(0 == hostname || !getHostAddress(hostname,&host_address))
  {
    printf("can not get address of host \"%s\"\n", hostname);
    return -1;;
  }

  struct sockaddr_in	serv_addr;
  /*
   * Fill in the structure "serv_addr" with the address of the
   * server that we want to connect with.
   */
  std::memset(&serv_addr, 0, sizeof(serv_addr));
  serv_addr.sin_family      = AF_INET;
  serv_addr.sin_addr.s_addr = host_address; 
  serv_addr.sin_port        = port; 
  /*
   * Connect to the server.
   */
  if (connect(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) 
  {
    printf("%s:%d: error in connect\n", __FILE__, __LINE__);
    return -1;
  }
  return sockfd;
}
