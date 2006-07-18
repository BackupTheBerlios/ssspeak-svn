#!/bin/sh
libtoolize --copy --force;
aclocal -I /usr/share/autoconf-archive
automake --add-missing --copy
autoconf
echo "Now cd to your build directory and run CFLAGS=-g CXXFLAGS=-g LDFLAGS=-g ./configure --prefix ~/packages/ssspeak --enable-maintainer-mode"
#$srcdir/configure --enable-maintainer-mode "$@"
