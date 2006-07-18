#!/usr/bin/env jython

from jssspeak.browser import *

from javax.swing import *
from java.awt.event import *
from java.net import URL
from string import split, join
from playtest import MyPlayer

class Browser(HtmlBrowser, ActionListener):
    def __init__(self):
        self.homeUrl = "file:/home/tshalif/src/ws/sspeak/jssspeak/test/data/html/wikipedia.org.Mossad.html"

        (self.b_stop, self.b_pause, self.b_resume, self.b_home,self.b_speak, self.b_go, self.b_back) = buttons = (JButton("Stop"), JButton("Pause"), JButton("Resume"), JButton("Home"), JButton("Speak"), JButton("Go"), JButton("Back"))

        
        self.e = JTextField(15)
        self.e.setText(self.homeUrl)
        self.l_desc = JLabel()
        self.l_location = JLabel()
        
        for b in buttons:
            b.addActionListener(self)
            pass

        f = JFrame()

        vb = Box.createVerticalBox()
        top = Box.createHorizontalBox()
        bottom = Box.createHorizontalBox()
        p1 = JPanel()
        top.add(self.e)
        top.add(self.b_speak)
        
        bottom.add(self.b_go)
        bottom.add(self.b_back)
        bottom.add(self.b_stop)
        bottom.add(self.b_pause)
        bottom.add(self.b_resume)
        bottom.add(self.b_home)
        
        vb.add(self.l_location)
        vb.add(top)
        vb.add(self.l_desc)
        vb.add(bottom)
        
        f.getContentPane().add(vb)


        f.pack()
        f.show()

        pass

    def setLocation(self,url):
        self.location = url

        if url:
            self.e.setText(url.toString())
        else:
            self.e.setText("")
            pass
        pass

    def speak(self):
        url = self.e.getText()

        if url:
            self.back()
            self.browse(url)
            pass
        pass
    
    def go(self):
        self.pause()
        url = self.l_location.getText()
        
        if url:
            self.browse(url)
            pass
        pass
    
    def browse(self, url):
        self.l_location.setText("")
        self.setLocation(URL(url))
        HtmlBrowser.go(self, url)
        pass
    
    def back(self):
        HtmlBrowser.back(self)
        url = self.getCurrentUrl()

        self.setLocation(url)
        pass
    
    def actionPerformed(self, event):
        src = event.getSource()
        
        if src == self.b_stop:
            self.abort()
        elif src == self.b_go:
            self.go()
        elif src == self.b_speak:
            self.speak()
        elif src == self.b_home:
            self.browse(self.homeUrl)
        elif src == self.b_pause:
            self.pause()
        elif src == self.b_resume:
            self.resume()
        elif src == self.b_back:
            self.back()
        pass

    def handle(self, mark):
        HtmlBrowser.handle(self, mark)
        self.l_location.setText(self.currentLink)
        print self.currentLink
        pass
    
    def fixLink0(self, url):
        if url[:5] == "http:" or url[:5] == "file:":
            return url
        
        
        if url[0] == '/':
            res = self.location.protocol + "://" + self.location.host
            if -1 != self.location.port:
                res += ":%d" % self.location.port
                pass
            
            return res + url

        tokens = self.location.toString().split("/")[:-1]
        res = join(tokens, "/") + "/" + url
        return res
    pass

class SessionPlayer(MyPlayer, SessionChangeListener):
    def __init__(self, browser):
        MyPlayer.__init__(self)
        
        browser.setSessionListener(self)
        pass

    def sessionChanged(self, newSession):
        player = None
        sessId = ""
        
        if newSession:
            player = newSession
            sessId = newSession.getId()
            pass

        self.setPlayer(player)

        print "sessionChanged(%s)" % sessId

        pass
    
if __name__ == '__main__':
    browser = Browser()
    player = SessionPlayer(browser)
    pass
