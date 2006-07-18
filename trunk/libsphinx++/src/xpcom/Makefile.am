noinst_LTLIBRARIES = libxpcomsphinx.la

MOZ_PREFIX =/home/tshalif/src/ws/mozilla-1.7.3/build-tree/mozilla/dist
MOZ_INC_DIR = $(MOZ_PREFIX)/include
XPIDL = $(MOZ_PREFIX)/bin/xpidl

AM_CXXFLAGS =  -DSSPEAK_NO_THROW_ERROR -include $(MOZ_INC_DIR)/mozilla-config.h -fno-rtti -fno-exceptions -Wall -Wconversion -Wpointer-arith -Wcast-align -Woverloaded-virtual -Wno-ctor-dtor-privacy -Wno-non-virtual-dtor -Wno-long-long -pedantic -fshort-wchar -DXPCOM_GLUE  -DDEBUG -D_DEBUG -DDEBUG_tshalif -DTRACING -DMOZILLA_CLIENT -g -fno-inline $(foreach i,. xpcom nspr string embedstring,-I$(MOZ_INC_DIR)/$(i))
AM_LDFLAGS = -L$(MOZ_PREFIX)/lib -Wl,-Bsymbolic 

libxpcomsphinx_la_SOURCES = nsSphinx.cpp  nsSphinxModule.cpp
libxpcomsphinx_la_LIBADD = ../libsphinx++.la -lxpcomglue  -lplds4 -lplc4 -lnspr4 
libxpcomsphinx_la_LDFLAGS = -rpath $(libdir)

nsISphinx.h: nsISphinx.idl
	$(XPIDL) -m header -w -I$(MOZ_PREFIX)/idl $<

xpcomsphinx.xpt:  nsISphinx.idl
	$(XPIDL) -m typelib -w -I$(MOZ_PREFIX)/idl -o xpcomsphinx $<

all-local: xpcomsphinx.xpt

test:
	$(prefix)/lib/mozilla/run-mozilla.sh $(prefix)/lib/mozilla/xpcshell test.js
test-xpcshell:
	$(prefix)/lib/mozilla/run-mozilla.sh $(prefix)/lib/mozilla/xpcshell
