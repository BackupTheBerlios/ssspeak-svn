#! /bin/sh

CLASSPATH=./lib/aspectjrt.jar:./lib/aspectjtools.jar:lib/Tidy.jar:/usr/share/java/xalan2.jar:/usr/share/java/xercesImpl.jar:/usr/share/java/junit.jar:build/classes java org.aspectj.tools.ajc.Main -g $@