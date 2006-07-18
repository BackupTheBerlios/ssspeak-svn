var ssspeak = Components.classes["@mozilla.org/ssspeak;1"].createInstance();
var synth = ssspeak.QueryInterface(Components.interfaces.nsISynthesizer);
var listener = {
  onMark: function(mark) {
    dump("onMark(" + mark + ")\n");
  },
  onSrvStateChange: function(state) {
    dump("onSrvStateChange(" + state + ")\n");
  },
};
var tofifo = "/tmp/ssmlToServer.fifo";
var fromfifo = "/tmp/ssmlFromServer.fifo";

synth.initFifos(tofifo, fromfifo, listener);
