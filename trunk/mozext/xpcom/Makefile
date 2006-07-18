#
# The contents of this file are subject to the Netscape Public
# License Version 1.1 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a copy of
# the License at http://www.mozilla.org/NPL/
#
# Software distributed under the License is distributed on an "AS
# IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
# implied. See the License for the specific language governing
# rights and limitations under the License.
#
# The Original Code is mozilla.org code.
#
# The Initial Developer of the Original Code is Netscape
# Communications Corporation.  Portions created by Netscape are
# Copyright (C) 1998 Netscape Communications Corporation. All
# Rights Reserved.
#
# Contributor(s): 
#

MOZILLA_SRCDIR = /home/tshalif/scratch/mozilla-1.7.3/build-tree/mozilla

DEPTH		= $(MOZILLA_SRCDIR)
topsrcdir	= $(MOZILLA_SRCDIR)
srcdir		= .
VPATH		= .

include $(DEPTH)/config/autoconf.mk

MODULE		= xpcomssspeak
XPIDL_MODULE	= xpcomssspeak
LIBRARY_NAME	= xpcomssspeak
SHORT_LIBNAME	= xpcomspk

IS_COMPONENT	= 1
MODULE_NAME	= nsSsspeakModule

# Ensure that the xpcom classes that we build
# do not export themselves
DEFINES		+= -DXPCOM_GLUE 


REQUIRES	= string \
		  xpcom \
		  $(NULL)

CPPSRCS		=		   \
		nsSynthesizer.cpp	   \
		nsSynthSrvListener.cpp	   \
		nsSsspeakModule.cpp \
		$(NULL)

XPIDLSRCS	= nsISynthesizer.idl nsISynthSrvListener.idl

TESTCPPSRCS	= nsTestSsspeak.cpp 

CPPSRCS		+= $(TESTCPPSRCS)

SIMPLE_PROGRAMS	= $(TESTCPPSRCS:.cpp=$(BIN_SUFFIX))

include $(topsrcdir)/config/config.mk

#EXTRA_COMPONENTS = nsSsspeak.js

# separate libraries linked in.
EXTRA_DSO_LDOPTS = \
		$(DIST)/lib/$(LIB_PREFIX)xpcomglue.$(LIB_SUFFIX) \
		$(NSPR_LIBS) \
		$(NULL)

LIBS            = \
		$(DIST)/lib/$(LIB_PREFIX)xpcomglue.$(LIB_SUFFIX) \
		$(NSPR_LIBS) \
		$(NULL)

# Needed to resolve __yylex (?)
ifeq ($(OS_ARCH)$(OS_RELEASE),FreeBSD2)
LIBS		+= -lpcap
endif

# Need to link with CoreFoundation on Mac
ifneq (,$(filter mac cocoa,$(MOZ_WIDGET_TOOLKIT)))
EXTRA_DSO_LDOPTS += \
		$(TK_LIBS) \
		$(NULL)

LIBS += \
		$(TK_LIBS) \
		$(NULL)
endif

include $(topsrcdir)/config/rules.mk

#libs:: $(TARGETS)
#	$(INSTALL) $(srcdir)/xpconnect-ssspeak.html $(DIST)/bin/res/ssspeaks

#install:: $(TARGETS)
#	$(SYSINSTALL) $(IFLAGS1) $(srcdir)/xpconnect-ssspeak.html $(DESTDIR)$(mozappdir)/res/ssspeak

test:
	/usr/local/lib/mozilla-1.7.3/run-mozilla.sh /usr/local/lib/mozilla-1.7.3/./xpcshell test.js

