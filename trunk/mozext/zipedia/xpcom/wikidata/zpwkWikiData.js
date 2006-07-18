const kZIPREADER_CONTRACTID = "@mozilla.org/libjar/zip-reader;1";
const nsIZipReader = Components.interfaces.nsIZipReader;
const kPREFERENCES_SERVICE = "@mozilla.org/preferences-service;1";
const nsIPrefService = Components.interfaces.nsIPrefService;
const nsILocalFile = Components.interfaces.nsILocalFile;
const kCONSOLESERVICE_CONTRACTID = "@mozilla.org/consoleservice;1";
const nsIConsoleService = Components.interfaces.nsIConsoleService;


var tracingEnabled = true;

function trace(msg) {
  if (tracingEnabled) {
    Components.classes[kCONSOLESERVICE_CONTRACTID].getService(nsIConsoleService).logStringMessage(msg);
  }
};

var gWikiData = null;

var WikiDataModule = {
  myCID : Components.ID("{8ae4ce15-b49e-4a97-a145-ec209b39ab3f}"),
  myProgID : "@nargila.org/wiki-data;1",
  
  registerSelf : function(compMgr, fileSpec, location, type) {
    compMgr = compMgr.QueryInterface(Components.interfaces.nsIComponentRegistrar);
    compMgr.registerFactoryLocation(
				    this.myCID,
				    "Zipedia Wiki Data",
				    this.myProgID,
				    fileSpec,
				    location,
				    type
				    );
  },
  
  unregisterSelf : function(compMgr, fileSpec, location) {
    compMgr = compMgr.QueryInterface(nsIComponentRegistrar);
    compMgr.unregisterFactoryLocation(this.myCID, fileSpec);
  },
  
  getClassObject : function(compMgr, cid, iid) {
    if (!cid.equals(this.myCID)) {
      throw Components.results.NS_ERROR_NO_INTERFACE;
    }
    if (!iid.equals(Components.interfaces.nsIFactory)) {
      throw Components.results.NS_ERROR_NOT_IMPLEMENTED;
    }
    return this.myFactory;
  },

  canUnload : function(compMgr) {
    return true;
  },

  myFactory : {
    createInstance : function(outer, iid) {
      if (outer != null) {
	throw Components.results.NS_ERROR_NO_AGGREGATION;
      }

      if (!gWikiData) {
	//var synchQueue = makeEventQueue();
	//var wikiData = new WikiData(synchQueue);
	var wikiData = new WikiData(null);

	gWikiData = wikiData;
	//gWikiData = makeEventQueueProxy(synchQueue, wikiData, Components.interfaces.zpwkIWikiData);
	//gWikiData.load();
      }


      var dummy = {
	QueryInterface: function(iid) {
	  if (!iid.equals(Components.interfaces.zpwkIWikiData) &&
	      !iid.equals(Components.interfaces.nsISupports)) {
	    throw Components.results.NS_ERROR_NO_INTERFACE;
	  }
	  return this;
	}
      };

      return gWikiData;

      //return dummy;
    }
  }
}


function NSGetModule(compMgr, fileSpec) {
    return WikiDataModule;
}


/*----------------------------------------------------------------------
 * The ChromeExtension Handler
 *----------------------------------------------------------------------
 */

function openZipFile(file) {
  var zip = Components.classes[kZIPREADER_CONTRACTID].createInstance(nsIZipReader);

  zip.init(file);
  
  zip.open();

  return zip;
}

function getStringPref(branch, name) {
  var prefs = Components.classes[kPREFERENCES_SERVICE].getService(nsIPrefService);

  var branch = prefs.getBranch(branch + ".");

  return branch.getCharPref(name);
}


function WikiData(synchQueue) {
  this.synchQueue = synchQueue;
  this.wrappedJSObject = this;
}


var gMonitors = 0;
var gReaders = 0;
var gWriters = 0;

function dummy() {}

WikiData.prototype = {
  zipFiles: null,
  synchLock: {
    writeLock: dummy,
    readLock: dummy,
    enterMonitor: dummy,
    exitMonitor: dummy,
    writeUnlock: dummy,
    readUnlock: dummy
  },

  loading: false,
  QueryInterface : function(iid) {

    if (!iid.equals(Components.interfaces.zpwkIWikiData) &&
	!iid.equals(Components.interfaces.nsISupports)) {
      throw Components.results.NS_ERROR_NO_INTERFACE;
    }


    return this;
  },
  initJava: function(wrappedJava, extensionPathUrl) {
    if (false && !this.synchLock) {
      var java = wrappedJava.wrappedJSObject;

      var classLoaderURL = new java.net.URL(extensionPathUrl + "components/java/class/");

      var cl = java.net.URLClassLoader.newInstance([classLoaderURL]);
      
      this.synchLock = java.lang.Class.forName("LockObj", true, cl).newInstance();

//       var jvm = Components.classes["@mozilla.org/oji/jvm-mgr;1"].getService(Components.interfaces.nsIJVMManager);
//       jvm.showJavaConsole();

    }
  },
  load: function() {
    //this.synchLock.writeLock();
    if (this.loading) {
      return;
    }

    this.loading = true;

    try {
      var dir;

      var dirpath = getStringPref("zipedia", "zip.path");

      if (dirpath) {
	dir = Components.classes["@mozilla.org/file/local;1"].createInstance(nsILocalFile);
	
	dir.initWithPath(dirpath);
	
	if (!dir.isDirectory()) {
	  throw dirpath + ": given zipedia dir path is not a directory";
	}
      } else { // try to use zip data under extension directory - if any
	dir = getExtensionPath("zipedia");
	
	dir.appendRelativePath("zipdata");
	
	trace("zipdata: " + dir.path + "\n");

	if (!dir.exists() || !dir.isDirectory()) {
	  throw "no zipedia data configuration found";
	}
      } 

      for (var cat in this.zipFiles) {
	if (this.zipFiles[cat]) {
	  this.zipFiles[cat].close();
	}
      }

      this.zipFiles = {};

      var i = dir.directoryEntries;

      while (i.hasMoreElements()) {
	var file = i.getNext().QueryInterface(nsILocalFile);

	if (file.isFile() && file.isReadable()) {
	  var parts = file.leafName.split(".");

	  if (parts.length == 2 && parts[1] == "zip") {
	    var cat = parts[0];

	    this.zipFiles[cat] = openZipFile(file);
	  }
	}
      }
    } finally {
      this.loading = false;
      //this.synchLock.writeUnlock();
    }
  },
  getData: function(category) {
    this.synchLock.enterMonitor();
    this.synchLock.readLock();

    var zip = null;

    trace("getData() -->!\n");
    try {
      if (!this.zipFiles) {

	trace("getData(): load()\n");
	this.load();
      }
      
      zip = this.zipFiles[category];

    } finally {
      if (!zip) {
	this.synchLock.readUnlock();
      }

      this.synchLock.exitMonitor();
    }

    return zip;
  },

  releaseData: function() {
    this.synchLock.readUnlock();
  },

  get categories() {
    this.synchLock.enterMonitor();
    this.synchLock.readLock();

    var keys = [];

    try {
      for (var key in this.zipFiles) {
	keys.push(key);
      }
    } finally {
      this.synchLock.readUnlock();
      this.synchLock.exitMonitor();
    }
    return keys.join(' ');
  }
}


function makeEventQueueProxy(queue, obj, iid) {

  var proxyCID = "@mozilla.org/xpcomproxy;1";
  var nsIProxyObjectManager = Components.interfaces.nsIProxyObjectManager;

  var proxyManager = Components.classes[proxyCID].createInstance(nsIProxyObjectManager);
  var proxy = 
    proxyManager.getProxyForObject(queue, iid, obj, 5);

  return proxy;
}

function makeEventQueue() {
  var nsIThread = Components.interfaces.nsIThread;

  var thread = Components.classes["@mozilla.org/thread;1"].createInstance(nsIThread);

  var nsIEventQueueService = Components.interfaces.nsIEventQueueService;

  var eventservice = 
    Components.classes["@mozilla.org/event-queue-service;1"].createInstance(nsIEventQueueService);

  const nsIEventQueue = Components.interfaces.nsIEventQueue;

   var eventQueue = Components.classes["@mozilla.org/event-queue;1"].createInstance(nsIEventQueue);

   eventQueue.init(false);

//   var eventQueue = eventservice.createFromIThread(thread, true);

//   thread.init({
//     queue: eventQueue,
//     run: function() {
// 	//this.queue.eventLoop();

// 	while (true) {

// 	  trace("bulaC\n");

// 	  this.queue.processPendingEvents();
// 	}
//       }
//     }, 0, nsIThread.PRIORITY_NORMAL, nsIThread.SCOPE_GLOBAL, nsIThread.STATE_UNJOINABLE);
  
  return eventQueue;
}



function getExtensionPath(extensionName) {
  var chromeRegistry = 
    Components.classes["@mozilla.org/chrome/chrome-registry;1"].getService(Components.interfaces.nsIChromeRegistry);

  var uri = Components.classes["@mozilla.org/network/standard-url;1"].createInstance(Components.interfaces.nsIURI);

  uri.spec = "chrome://" + extensionName + "/content/";

  var path = chromeRegistry.convertChromeURL(uri);

  var file = path.QueryInterface(Components.interfaces.nsIFileURL).file;

  while (!file.path.match(/chrome$/)) {
    file = file.parent;
  }

  file = file.parent;

  return file.QueryInterface(Components.interfaces.nsILocalFile);
}
