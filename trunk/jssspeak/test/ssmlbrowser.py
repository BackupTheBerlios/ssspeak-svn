from com.shalif.voicebrowser.browser import *
from com.shalif.voicebrowser.ssmlsynth import SsmlMarkListener
from java.lang import Thread

class MarkListener(SsmlMarkListener):
    def handle(self, mark):
        print mark
        pass
    pass


ssml1 = "/home/tshalif/src/mozilla/ssspeak/java/com/shalif/voicebrowser/ssmlsynth/test/test.ssml"
ssml2 = "/home/tshalif/src/mozilla/ssspeak/java/com/shalif/voicebrowser/ssmlsynth/test/test2.ssml"
ssml3 = "/home/tshalif/src/mozilla/ssspeak/java/com/shalif/voicebrowser/ssmlsynth/test/Mossad.ssml"

browser = SsmlBrowser(MarkListener())

browser.go(ssml3, "Feynman")

Thread.currentThread().sleep(3000)

browser.go(ssml2, "Mossad")

Thread.currentThread().sleep(3000)

browser.back()




