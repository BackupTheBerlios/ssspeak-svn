const nsIThread = Components.interfaces.nsIThread;
const ThreadContractID = "@mozilla.org/thread;1";
const ThreadIfc = new Components.Constructor(ThreadContractID, "nsIThread", "init");

function Thread(runable) {
  var th = new ThreadIfc(runable, 0, 
                         nsIThread.PRIORITY_NORMAL,
                         nsIThread.SCOPE_LOCAL,
                         nsIThread.STATE_JOINABLE);
  return th;
}

var th = new Thread({run: function() {},});

var SII = Components.classes["@mozilla.org/sphinx;1"];
var si = SII.createInstance();
si.addVocabulary("cat dog chicken");

si.recognize();

// var runnable = {
//   run: function() {
//     for (var mark = si.nextEvent(); mark != ""; mark = si.nextEvent()) {
//       dump("javascript:mark:" + mark + "\n");
//     }
//   },
// };

//var th = new Thread(runnable);

si.ready();

dump("ready\n");

si.listen();

dump("listening\n");
dump("state: " + si.state + "\n");

si.finish();

dump("finished recognitions\n");
dump("state: " + si.state + "\n");

dump("result: " + si.result + "\n");

si.recognize();

var state = si.state;

while (state != "STATE_FINISHED" && state !=  "STATE_TIMEOUT" && state != "STATE_CANCELLED") {
  switch (si.state) {
  case "STATE_FINISHED":
    dump("finished: result=" + si.result + "\n");
    break;
  case "STATE_CANCELLED":
    dump("canceled\n");
    break;
  case "STATE_LISTEN":
    dump("listening\n");
    break;
  }
  th.currentThread.sleep(100);
  dump("state: " + state + "\n");

  state = si.state;
 }

dump("result: " + si.result + "\n");

