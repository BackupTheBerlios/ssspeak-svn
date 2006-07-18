
INCLUDES = -I/usr/include/libxml2 -I/usr/include/cc++2
LIBS = -Wl,-Bstatic -lzipios -lxml2  -lbz2 -Wl,-Bdynamic -lz

COMPONENT = zpwkIWikiDumpToZip

all:: zipediadump

HEADERS = DumpReader.h \
	WikiDumpParser.h \
	WikiZipPageWriter.h \
	zpwkWikiDumpToZip.h \
	Lockable.h \
	MonitorBlock.h

OBJS = DumpReader.o Lockable.o MonitorBlock.o WikiDumpParser.o WikiZipPageWriter.o 

COMPONENT_OBJS = $(OBJS) zpwkWikiDumpToZip.o zpwkWikiDumpToZipModule.o 

zipediadump: $(OBJS) zipediadump.o
	$(LINK.C) -v -o $@ $(OBJS) zipediadump.o   $(LIBS) -lplc4

clean::
	rm -f zipediadump

test:
	echo $(GECKO_SDK_PATH)/bin/run-mozilla.sh $(GECKO_SDK_PATH)/bin/xpcshell
# test.js

include ../../../xpcom.mk

