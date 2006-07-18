#include "nsIGenericFactory.h"

#include "zpwkWikiDumpToZip.h"

#define ZIPDUMP_CONTRACTID "@nargila.org/wiki-dump-tozip;1"

NS_GENERIC_FACTORY_CONSTRUCTOR(zpwkWikiDumpToZip);

NS_DECL_CLASSINFO(zpwkWikiDumpToZip);

static const nsModuleComponentInfo components[] =
{
  { 
  	"zpwkWikiDumpToZip", 
  	ZPWKIWIKIDUMPTOZIP_IID, 
  	ZIPDUMP_CONTRACTID, 
  	zpwkWikiDumpToZipConstructor
  }
};

////////////////////////////////////////////////////////////////////////
// ement the NSGetModule() exported function for your module
// and the entire implementation of the module object.
//
// NOTE: If you want to use the module shutdown to release any
//		module specific resources, use the macro
//		NS_IMPL_NSGETMODULE_WITH_DTOR() instead of the vanilla
//		NS_IMPL_NSGETMODULE()
//
NS_IMPL_NSGETMODULE(nzpwkWikiDumpToZipModule, components)
