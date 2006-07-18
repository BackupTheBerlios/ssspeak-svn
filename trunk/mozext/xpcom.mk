ifndef COMPONENT
$(warning make variable COMPONENT is not defined)
endif

CXXFLAGS +=     -fpic \
		-Wall \
		$(INCLUDES) \
		$(GECKO_INCLUDES) \
		$(GECKO_DEFINES) \
		$(GECKO_CONFIG_INCLUDE) 

GECKO_SDK_PATH_win32 = /home/tshalif/tmp/gecko-sdk-win32
GECKO_SDK_PATH_linux = /home/tshalif/tmp/firefox-1.5/build/dist

GECKO_SDK_PATH = $(GECKO_SDK_PATH_$(SYSTEM))

#/home/tshalif/tmp/mozilla-1.7.3/build-tree/mozilla/dist

# GCC only define which allows us to not have to #include mozilla-config
# in every .cpp file.  If your not using GCC remove this line and add
# #include "mozilla-config.h" to each of your .cpp files.
GECKO_CONFIG_INCLUDE = -include mozilla-config.h

GECKO_DEFINES  = -DMOZILLA_STRICT_API
#-DXPCOM_GLUE 

GECKO_INCLUDES = -I$(GECKO_SDK_PATH)/include \
		-I$(GECKO_SDK_PATH)/include/nspr \
		-I$(GECKO_SDK_PATH)/include/xpcom \

GECKO_LDFLAGS =   -L$(GECKO_SDK_PATH)/lib -lxpcom -lnspr4 -lplc4 -lplds4 $(LIBS)

XPIDL = $(GECKO_SDK_PATH)/bin/xpidl

ifndef RELEASE
CXXFLAGS += -g3 -O0
LDFLAGS += -g3  -O0
else
CXXFLAGS += -O2
LDFLAGS += -O2
endif

ifeq ($(SYSTEM),win32)
XPIDL = $(GECKO_SDK_PATH_linux)/bin/xpidl
PATH := /usr/local/cross-tools/bin:$(PATH)
export PATH
CXX = i686-pc-cygwin-g++
CC = i686-pc-cygwin-gcc
LD = i686-pc-cygwin-ld
RANLIB = i686-pc-cygwin-ranlib
AR = i686-pc-cygwin-ar
EXE = .exe
endif


ifdef COMPONENT
all:: $(COMPONENT).xpt
ifneq ($(COMPONENT_OBJS),)
all:: lib$(COMPONENT).so
endif

$(COMPONENT_OBJS): $(COMPONENT).h

$(COMPONENT).h: $(COMPONENT).idl
	$(XPIDL) -I$(GECKO_SDK_PATH)/idl -m header $<

$(COMPONENT).xpt: $(COMPONENT).idl
	$(XPIDL) -I$(GECKO_SDK_PATH)/idl -m typelib $<

lib$(COMPONENT).so: $(COMPONENT).h $(COMPONENT_OBJS)
	$(LINK.C) -shared -o $@ $(COMPONENT_OBJS) $(GECKO_LDFLAGS) 

clean::
	rm -f $(COMPONENT).h
endif

clean::
	rm -f *~ *.o *.so *.xpt 