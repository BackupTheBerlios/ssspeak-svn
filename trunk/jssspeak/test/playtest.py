from jssspeak.player import *
from java.awt.event import ActionListener
from java.io import *
from javax.swing import *


class AListener(ActionListener):
    def __init__(self, myplayer):
        self.myplayer = myplayer
        pass
    def actionPerformed(self,event):
        self.myplayer.actionPerformed(event.getSource())
        pass
    pass

class MyPlayer(MyWavPlayer):
    def __init__(self):
        
        self.b_start = JButton("Start")
        self.b_forward = JButton("Forward")
        self.b_reverse = JButton("Back")
        self.b_pause = JButton("Pause")
        self.b_resume = JButton("Resume")
        self.b_clear = JButton("Clear")
        self.b_abort = JButton("Abort")
        self.b_finish = JButton("Finish")
        
        buttons = [self.b_abort, self.b_finish, self.b_clear, self.b_pause, self.b_resume, self.b_reverse, self.b_forward, self.b_start]
        
        al = AListener(self)
        
        for b in buttons:
            b.addActionListener(al)
            pass


        f_frame = JFrame()
        
        
        pane = f_frame.getContentPane()
        
        box = BoxLayout(pane, BoxLayout.X_AXIS)
        
        pane.setLayout(box)
        
        for b in buttons:
            pane.add(b)
            pass
        
        f_frame.pack()
        f_frame.show()

        pass

    def setPlayer(self, player):
        self.player = player

    def actionPerformed(self,src):
        if src == self.b_start:
            self.start()
        elif src == self.b_forward:
            self.skip(16000 * 10)
        elif src == self.b_reverse:
            self.skip(16000 * -10)
        elif src == self.b_pause:
            self.pause()
        elif src == self.b_resume:
            self.resume()
        elif src == self.b_clear:
            self.clear()
        elif src == self.b_abort:
            self.abort()
        elif src == self.b_finish:
            self.finish()
            pass
        pass
    
    pass

player = None

def test():
    global player
    
    player = MyPlayer()

    player.setPlayListener(Listener())

    dir = File("data/raw-wav")

    files = dir.listFiles(HlnkFilter())
        
    for f in files:
        player.add(f)
        pass
    pass


if __name__ == '__main__':
    test()
    pass
