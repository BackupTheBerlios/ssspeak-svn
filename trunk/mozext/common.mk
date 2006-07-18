ifndef EXTENTION
$(error error make variable EXTENTION is not defined)
endif

ifndef SYSTEM
$(error error make variable SYSTEM is not defined - 'win32' or 'linux')
endif

ifeq ($(SYSTEM),linux)
EXCLUDE_SYSTEM = win32
else
EXCLUDE_SYSTEM = linux
endif

$(EXTENTION)-$(SYSTEM).xpi: clean .build .dist-$(SYSTEM)
	(cd .dist-$(SYSTEM) && zip -R ../$@ '*')

.dist-$(SYSTEM):: .filelist
	mkdir $@
	for f in `cat .filelist`; do \
		d=`dirname $$f`; \
		mkdir -p $@/$$d; \
		ln -s $(PWD)/$$f $@/$$d; \
	done

	for i in `find $@ -type d -name $(SYSTEM)`; do \
		mv $$i/* $$i/.. && rmdir $$i; \
	done

.dist:: .filelist
	mkdir .dist
	cat .filelist | xargs tar rhf .dist.tar
	tar -C .dist -xvf .dist.tar
	for i in `find .dist -type d -name $(SYSTEM)`; do \
		mv $$i/* $$i/.. && rmdir $$i; \
	done

clean::
	rm -rf $(EXTENTION)-$(SYSTEM).xpi .filelist .dist-$(SYSTEM)
	find -name "*~" | xargs rm -f

XPCOM_DIRS = $(shell if test -d xpcom; then find xpcom -mindepth 1 -maxdepth 1 -type d \! -name "\.*"; fi)

.filelist: 
	find  -mindepth 1 -type f -follow \! -name Makefile \! -name "*~" \! -path '*/$(EXCLUDE_SYSTEM)/*' \! -path '*/\.*' \! -path '*/tmp/*' \! -path '*/xpcom/*' \! -name "*\.xpi" > $@

.build:
ifneq ($(XPCOM_DIRS),)
	for i in $(XPCOM_DIRS); do \
		$(MAKE) -C $$i RELEASE=1 clean all; \
	done
endif

.force:
