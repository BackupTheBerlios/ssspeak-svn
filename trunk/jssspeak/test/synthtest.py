from com.shalif.voicebrowser.ssmlsynth import *
from com.shalif.voicebrowser.player import *
from java.lang import Thread

from playtest import *

class MarkListener(SsmlMarkListener):
    def handle(self, mark):
        print mark
        pass
    pass

ssml1 = "/home/tshalif/src/mozilla/ssspeak/java/com/shalif/voicebrowser/ssmlsynth/test/test.ssml"
ssml2 = "/home/tshalif/src/mozilla/ssspeak/java/com/shalif/voicebrowser/ssmlsynth/test/test2.ssml"
ssml3 = "/home/tshalif/src/mozilla/ssspeak/java/com/shalif/voicebrowser/ssmlsynth/test/Mossad.ssml"

myplayer = MyPlayer()


synth = FestiSsmlSynth(MarkListener(), myplayer.player)

synth.synth(ssml1)


