from com.shalif.voicebrowser.browser import *
from com.shalif.voicebrowser.voicecontrol import *

from htmlbrowser import Browser

class VoiceControledBrowser(VoiceCmdListener):
    def __init__(self):

        self.browser = Browser()

        self.sphinx = VCSphinxProc(self)

        pass

    def voiceStart(self):
        print "vc start:"
        self.browser.pause()
        pass
    def voiceCmd(self, cmd):
        print "vc cmd: %s" % cmd

        if cmd:
            eval("self.%s()" % cmd)
            pass
        pass

    def SPEAK(self):
        self.browser.speak()
        pass
    def PAUSE(self):
        self.browser.pause()
        pass
    def RESUME(self):
        self.browser.resume()
        pass
    def GO(self):
        self.browser.go()
        pass
    def STOP(self):
        self.browser.stop()
        pass
    def BACK(self):
        self.browser.back()
        pass
    def HOME(self):
        self.browser.browse(self.browser.homeUrl)
        pass
    pass

if __name__ == '__main__':
    browser = VoiceControledBrowser()
    pass

