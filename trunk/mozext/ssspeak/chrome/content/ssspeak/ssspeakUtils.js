
var gBrowser = null;

function getBrowser()
{
  if (!gBrowser)
    gBrowser = document.getElementById("content");
  return gBrowser;
}

var ssspeak_file_counter = 0;

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

function readLine(stream) {
  var line = "";
  var c;


  while ((n = stream.read(1))) {
    dump("bula1\n");

    if (n == "\n") {
      return line;
    }

    line += n;
  }

  return line.length ? line : null;
}

function asyncRead(ins, handler) {
  var runnable = {
    run: function() {
      var line = null;
      
      try {
        while (null != (line = readLine(ins))) {
          handler.handle(line);
        }
      } catch (ex) {
        dump(ex);
      }
    }
  };

  return new Thread(runnable);
}
  
  
function toScriptableInputStream (i) {
    var si = Components.classes["@mozilla.org/scriptableinputstream;1"];
    
    si = si.createInstance();
    si = si.QueryInterface(Components.interfaces.nsIScriptableInputStream);
    si.init(i);

    return si;    
}

function MyStreamListener(accumulator)
{
  dump("MyStreamListener()\n");

  this.accumulator = accumulator;
}

MyStreamListener.prototype.onStartRequest =
function sl_startreq (request, ctxt)
{
  dump ("StreamListener::onStartRequest: " + request + ", " + ctxt);
}

MyStreamListener.prototype.onStopRequest =
function sl_stopreq (request, ctxt, status)
{
    dump ("StreamListener::onStopRequest: " + request + ", " + ctxt + ", " +
        status);
}

MyStreamListener.prototype.onDataAvailable =
function sl_dataavail (request, ctxt, inStr, sourceOffset, count)
{
    ctxt = ctxt.wrappedJSObject;
    if (!ctxt)
    {
        dump ("*** Can't get wrappedJSObject from ctxt in " +
            "StreamListener.onDataAvailable ***");
        return;
    }

    if (!("_scriptableInputStream" in ctxt))
        ctxt._scriptableInputStream = toScriptableInputStream (inStr);

    this.accumulator.value += ctxt._scriptableInputStream.read(count);
}


function myLocalFile(path) {
  const myFile = new Components.Constructor("@mozilla.org/file/local;1", 
                                            "nsILocalFile",
                                            "initWithPath");
  return new myFile(path);
}

var gSsspeakLastDir = buildPath(GetStringPref('dumpSavePath') ?  GetStringPref('dumpSavePath') : getHomeDir());


function myFileOutputStream(localFile) {
  var outstream = Components.classes["@mozilla.org/network/file-output-stream;1"].createInstance(Components.interfaces.nsIFileOutputStream);
  
  outstream.init(localFile, 0x04 | 0x08, 420, 0);
  
  return outstream;
}

function myFileInputStream(localFile) {
  var istream = Components.classes["@mozilla.org/network/file-input-stream;1"].createInstance(Components.interfaces.nsIFileInputStream);
  istream.init(localFile, -1, -1, false);
  return istream;
}


function makeOutputFile(ask) {
  var url = window._content.document.location.toString();
  
  var saveName = url.split(/\//).pop();

  var title = window._content.document.title;

  if (title) {
    saveName = title;
  }

  if (!ask) {
    return buildPath(gSsspeakLastDir.path, [saveName + ".mp3"]).path;
  }

  var nsIFilePicker = Components.interfaces.nsIFilePicker;
  var fp = Components.classes["@mozilla.org/filepicker;1"].createInstance( nsIFilePicker );
  fp.init(null, "Choose Output File Name", nsIFilePicker.modeSave );
  
  
  fp.defaultString = saveName + ".mp3";
  
  fp.displayDirectory =  gSsspeakLastDir;
  
  
  fp.appendFilters( nsIFilePicker.filterAll );
  fp.appendFilter("MP3 Files", "mp3");
  
  if ( fp.show( ) == nsIFilePicker.returnOK) {
    gSsspeakLastDir = fp.displayDirectory;
    
    if (fp.file) {
      return fp.file.path;
    } else {
      return buildPath(gSsspeakLastDir.path, [fp.defaultString]).path;
    }
  }
  
  return null;
}

function xPathSearch(aXmlDoc, aXpath)
{
  var pXPE = new XPathEvaluator();
  var foundNodes = new Array();
  var res;

  var result = pXPE.evaluate(aXpath, aXmlDoc, null, 0, null);
  while ((res = result.iterateNext())) {
    foundNodes.push(res);
  }
  return foundNodes;
}

function xslt(styleSheetName, node, params) {
  var xsltProcessor = new XSLTProcessor();
  var dataXSL = document.implementation.createDocument("", "", null);

  dataXSL.async = false;

  dataXSL.load("chrome://ssspeak/content/xsl/" + styleSheetName + ".xml");

  xsltProcessor.importStylesheet(dataXSL);

  
  for (i in params) {
    var pair = params[i].split("=");
    
    xsltProcessor.setParameter(null, pair[0], pair[1]);
  }

  var res = xsltProcessor.transformToDocument(node);

  return res;
}

function serializeNode(outpath, node) {
  var outfile = new myLocalFile(outpath);
  
  try {
    outfile.remove(false);
  } catch (ex) {}

  var serializer = new XMLSerializer();

  var outstream =  new myFileOutputStream(outfile);

  serializer.serializeToStream(node, outstream, "utf-8");

  outstream.close();
}

function saveHtml(doc, outpath) {
  const nsIWBP = Components.interfaces.nsIWebBrowserPersist;
  var persist = Components.classes["@mozilla.org/embedding/browser/nsWebBrowserPersist;1"].createInstance(nsIWBP);
  persist.persistFlags = nsIWBP.PERSIST_FLAGS_FROM_CACHE | nsIWBP.PERSIST_FLAGS_REPLACE_EXISTING_FILES | nsIWBP.PERSIST_FLAGS_FIXUP_LINKS_TO_DESTINATION | nsIWBP.FLAGS_DONT_CHANGE_FILENAMES | nsIWBP.PERSIST_FLAGS_FIXUP_ORIGINAL_DOM;

  var f = new myLocalFile(outpath);

var encodingFlags = nsIWBP.ENCODE_FLAGS_RAW | nsIWBP.ENCODE_FLAGS_ENCODE_W3C_ENTITIES;
 persist.saveDocument(doc, f, null, "text/html", encodingFlags, 0);

}

function setAsynchRead(stream) {
  var accumulator = {value:""};

  var cls = Components.classes["@mozilla.org/network/input-stream-pump;1"];
  var pump = cls.createInstance(Components.interfaces.nsIInputStreamPump);
  pump.init(stream, 0, 0, 0, 0, false);
  pump.asyncRead(new MyStreamListener(accumulator), null);

  return accumulator;
  
}
  

function Connection(host, port) {
  var transportService =
    Components.classes["@mozilla.org/network/socket-transport-service;1"]
    .getService(Components.interfaces.nsISocketTransportService);
  var transport = transportService.createTransport(null,0,host,port,null);
  
  this.outs = transport.openOutputStream(0,0,0);

  this.istreamHandle = transport.openInputStream(0,0,0);
  
  this.ins = Components.classes["@mozilla.org/scriptableinputstream;1"]
    .createInstance(Components.interfaces.nsIScriptableInputStream);
  this.ins.init(this.istreamHandle);

  this.close = function() {
    this.ins.close();
    this.outs.close();
  }
}
 
function timerRun(when, runnable) {
  const kITimer = Components.interfaces.nsITimer;
  this.mTimer = Components.classes["@mozilla.org/timer;1"].
    createInstance(kITimer);

  var observer = {
    observe: function(aSubject, aTopic, aData) {
      try {
        runnable.run();
      } catch (ex) {
        dump(ex);
      }
    }
  };

  this.mTimer.init(observer, when, kITimer.TYPE_ONE_SHOT);
}

function makeThread(runnable) {
  var thread = Components.classes["@mozilla.org/thread;1"];
  thread = thread.createInstance(Components.interfaces.nsIThread);
  thread.init(runnable, 10000, 
              thread.PRIORITY_NORMAL, 
              thread.SCOPE_GLOBAL, 
              thread.STATE_JOINABLE);

  return thread;
}


function getJavaProps() {
  var doc = window._content.document;

  doc.write("<ul>\n");

  var System = Packages.java.lang.System;

  props = System.getProperties().toString().split(",");

  for (var i in props) {
    doc.write("<li>" + props[i]  + "</li>\n");
  }
  doc.write("</ul>\n");

  try {
    var a = new Packages.aha.Aha();
    alert(a.a);
  } catch (ex) {
    alert(ex);
  }
}

function asyncTest() {
  var file = myLocalFile("/tmp/bula.fifo");
  var stream = myFileInputStream(file);
  var ins = toScriptableInputStream(stream);


  var thread = asyncRead(ins, {handle: function(line) {alert(line);}});
}

function getSelectionAsHtmlDoc() {
  
  var selection = null;

  try {
    selection = window._content.getSelection().getRangeAt(0);

    if (selection == "") {
      return null;
    }
  } catch (e) {
    return null;
  }

  var doc = window._content.document;
  var span = doc.createElement("span");

  var newdoc = doc.implementation.createDocument("", "", null);

  var newroot = doc.documentElement.cloneNode(true);

  newdoc.appendChild(newroot);

  var body = newdoc.getElementsByTagName("body").item(0);

  var newbody = newdoc.createElement("body");

  var selectFragment = selection.cloneContents();

  newbody.appendChild(selectFragment);

  newroot.replaceChild(newbody, body);

  { // remove title from selected - so synthesizer does not have to speak it for only a few selected words
    var titles = newdoc.getElementsByTagName("title");
    
    if (titles.length) {
      var title = titles.item(0);
      
      title.parentNode.removeChild(title);
    }
  }

  return newdoc;
}

if (GetBoolPref("debug.showJavaConsole")) {
  var jvm = Components.classes["@mozilla.org/oji/jvm-mgr;1"].getService(Components.interfaces.nsIJVMManager);
  jvm.showJavaConsole();
 }


var ssspeak = Components.classes["@nargila.org/ssspeak;1"].createInstance(Components.interfaces.nsISsspeak)

var ssspeakDir = GetStringPref("ssspeakDir");

ssspeak.init({wrappedJSObject:java}, ssspeakDir);

function setPrefs(prefs) {
  var voice = ssspeak.voice.wrappedJSObject;

  for (var name in prefs) {
    if (prefs[name]) voice.setProperty(name, prefs[name]);
  }
}

setPrefs(getPrefValues());


function getVoice() {
  var ssspeak = Components.classes["@nargila.org/ssspeak;1"].createInstance(Components.interfaces.nsISsspeak);


  return ssspeak.voice.wrappedJSObject;
}
  
 
