SUBDIRS = . xpcom

javadir = $(datadir)/java
firefoxdir = /usr/lib/firefox
lib_LTLIBRARIES = libsphinx++.la libsphinxrecJNI.la
check_PROGRAMS = RecognizerTest
pkginclude_HEADERS = spnxRecognizer.h spnxRecState.h
java_DATA = sphinxrec.jar
TESTS =  RecognizerTest.sh 


SWIG_FLAGS = -v -c++   -shadow  -java -package sphinxrec -MMD $(AM_CXXFLAGS)
SWIG = swig

AM_CFLAGS =  -I/usr/include/sphinx2 -DWORDS_DIC='"$(pkgdatadir)/words.dic"'
AM_CXXFLAGS =  -DQUICK_LM_PATH='"$(bindir)/quick_lm.pl"' -DWORDS_DIC='"$(pkgdatadir)/words.dic"' -I@JAVA_HOME@/include -I@JAVA_HOME@/include/linux

libsphinx___la_SOURCES = spnxRecSession.cpp  spnxRecState.cpp  spnxRecognizer.cpp  spnxSphinxUtils.cpp sphinxhelper.c
libsphinx___la_LIBADD = $(SPHINX_LIB_DIR)/libsphinx2.la \
	$(SPHINX_LIB_DIR)/libsphinx2fe.la \
	$(SPHINX_LIB_DIR)/libsphinx2ad.la 


nodist_libsphinxrecJNI_la_SOURCES = swig/sphinxrec_wrap.cxx
libsphinxrecJNI_la_LIBADD = libsphinx++.la

RecognizerTest_SOURCES = RecognizerTest.cpp
RecognizerTest_LDFLAGS = -static
RecognizerTest_LDADD = libsphinx++.la

sphinxrec.jar: swig/sphinxrec_wrap.cxx
	mkdir -p swig/class
	$(JAVA_HOME)/bin/javac -d swig/class swig/*.java
	$(JAVA_HOME)/bin/jar -cvf $@ -C swig/class .

swig/sphinxrec_wrap.cxx: swig/sphinxrec.i
	$(SWIG) $(SWIG_FLAGS) swig/sphinxrec.i

-include swig/sphinxrec_wrap.d

clean-local:
	rm -rf swig/*.java swig/class swig/*_wrap.* *.jar

install-exec-hook:
	mkdir -p  $(DESTDIR)$(firefoxdir)
	cd $(DESTDIR)$(firefoxdir) && rm -f libsphinxrecJNI.so && \
               $(LN_S) $(libdir)/libsphinxrecJNI.so .

