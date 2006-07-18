#include "nsIGenericFactory.h"

#include "nsSynthesizer.h"

#define NS_SSSPEAK_CONTRACTID "@mozilla.org/ssspeak;1"

NS_GENERIC_FACTORY_CONSTRUCTOR(nsSynthesizer)

NS_DECL_CLASSINFO(nsSynthesizer)

static const nsModuleComponentInfo components[] =
{
  { "nsSynthesizer", NS_ISYNTHESIZER_IID, NS_SSSPEAK_CONTRACTID, nsSynthesizerConstructor}
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
NS_IMPL_NSGETMODULE(nsSynthesizerModule, components)
