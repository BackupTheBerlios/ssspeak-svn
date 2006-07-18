#! /usr/bin/env jython
from java.lang import *
from java.io import *

cmudic = "/home/tshalif/src/mozilla/ssspeak/helpers/tmp.ZQpyQl/current.dic"
reader = BufferedReader(InputStreamReader(System.in))
dicreader = BufferedReader(InputStreamReader(FileInputStream(cmudic)))


l = reader.readLine()
words = {}

while l != None:
    words[String(l).toUpperCase()] = None
    l = reader.readLine()
    pass

dicmap = {}

l = dicreader.readLine()

while l != None:
    key = l.split()[0]

    if not key:
        continue
    
    dicmap[key] = l
    
    l = dicreader.readLine()
    pass

lines = []

for l in words.keys():
    
    if dicmap.has_key(l):
        lines.append(dicmap[l])
        for i in range(1,4):
            key = "%s(%d)" % (l, i)
            if dicmap.has_key(key):
                lines.append(dicmap[key])
                pass
            pass
        pass
    pass

import sys

for l in lines:
    sys.stdout.write("%s\n" % l)
    pass
