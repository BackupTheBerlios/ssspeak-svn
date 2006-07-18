/*----------------------------------------------------------------------
 * nsChromeExtensionHandler
 * By Ed Anuff <ed@anuff.com>
 *
 * Last modified: 04/13/2005 15:49 PST
 *
 * DESCRIPTION:
 *
 * This component implements an extension mechanism to the chrome
 * protocol handler for mapping in dynamically generated content
 * into chrome URIs.  This content will have the same system
 * permissions as regular chrome content, making it possible to
 * create scripts which programmatically generate XUL pages and
 * stylesheets.  Remote chrome can also be implemented.
 *
 * This protocol handler could be installed with the same
 * contract ID as the chrome protocol handler so that all chrome
 * requests pass through it, but it has not been sufficiently tested
 * for that to be recommended.
 *
 *
 * EXAMPLE USAGE:
 *
 * To register an extension, use code like the following within privileged
 * Javascript running in your chrome:
 *
 *   var my_extension = {
 *
 *    pkg : "myext",
 *
 *    path : "myext.xul",
 *
 *    newChannel : function(uri) {
 *
 *      var ioService = Components.classes["@mozilla.org/network/io-service;1"].getService();
 *      ioService = ioService.QueryInterface(Components.interfaces.nsIIOService);
 *
 *      var uri_str = "data:,My%20extension%20content";
 *
 *      var ext_uri = ioService.newURI(uri_str, null, null);
 *      var ext_channel = ioService.newChannelFromURI(ext_uri);
 *
 *      return ext_channel;
 *
 *    }  
 *  };
 *
 *  var chrome_ext = Components.classes["@mozilla.org/network/protocol;1?name=xchrome"].getService();
 *  chrome_ext.wrappedJSObject.registerExtension(my_extension);
 *
 * The above example will register an extension at the following URL:
 *
 *  xchrome://myext/content/ext/myext.xul
 *
 *
 * For many extensions, using data: URLs to pass content back through
 * the ChromeExtensionHandler is the easiest mechanism.  See the following page
 * for more information on constructing data: URLs:
 *
 *  http://www.mozilla.org/quality/networking/testing/datatests.html
 *
 *
 * Protocol handler code based on techniques from:
 *
 *  http://www.nexgenmedia.net/docs/protocol/
 *  http://simile.mit.edu/piggy-bank/
 *
 *----------------------------------------------------------------------
 */

/*----------------------------------------------------------------------
 * The Zipedia Module
 *----------------------------------------------------------------------
 */

// Custom protocol related
const kSCHEME = "wikipedia";
const kPROTOCOL_CID = Components.ID("{C7715470-4AE3-4F14-B7E2-FB6974B5D25B}");
const kPROTOCOL_CONTRACTID = "@mozilla.org/network/protocol;1?name=" + kSCHEME;
const kPROTOCOL_NAME = "Embeded Wikipedia Protocol";


// Mozilla defined
const kPREFERENCES_SERVICE = "@mozilla.org/preferences-service;1";
const kCONSOLESERVICE_CONTRACTID = "@mozilla.org/consoleservice;1";
const kIOSERVICE_CID_STR = "{9ac9e770-18bc-11d3-9337-00104ba0fd40}";
const kIOSERVICE_CONTRACTID = "@mozilla.org/network/io-service;1";
const kURI_CONTRACTID = "@mozilla.org/network/simple-uri;1";
const kSTANDARDURL_CONTRACTID = "@mozilla.org/network/standard-url;1";
const kURLTYPE_STANDARD = 1;
const kZIPREADER_CONTRACTID = "@mozilla.org/libjar/zip-reader;1";
const nsIComponentRegistrar = Components.interfaces.nsIComponentRegistrar;
const nsIConsoleService = Components.interfaces.nsIConsoleService;
const nsIFactory = Components.interfaces.nsIFactory;
const nsIIOService = Components.interfaces.nsIIOService;
const nsIProtocolHandler = Components.interfaces.nsIProtocolHandler;
const nsIRequest = Components.interfaces.nsIRequest;
const nsIStandardURL = Components.interfaces.nsIStandardURL;
const nsISupports = Components.interfaces.nsISupports;
const nsIURI = Components.interfaces.nsIURI;
const nsIZipReader = Components.interfaces.nsIZipReader;
const nsIFile = Components.interfaces.nsIFile;
const nsIPrefService = Components.interfaces.nsIPrefService;
const zpwkIWikiData = Components.interfaces.zpwkIWikiData;
const WIKIDATACID = "@nargila.org/wiki-data;1";

const WIKI_SEARCH_FORM = '<form name="searchform" id="searchform" method="get" action="wikipedia://wiki/search:">\n      <input accesskey="f" id="searchInput" name="id" type="text" />\n      <input value="Search" type="submit" class="searchButton" />\n    </form>\n';

const HTML_HEAD = '<html><head>\n		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />\n		<title>%TITLE%</title>\n</head>\n<body class="ns-0 ltr">\n<div id="globalWrapper"><div id="search" class="search">' + WIKI_SEARCH_FORM + '</div>\n<div id="column-content">\n<div id="content">\n<a name="top" id="top"></a><h1>%TITLE%</h1><hr/>\n';

const HTML_TAIL = '</div></div></div><center>[<a href="wikipedia://raw/%TITLE%">Source</a>] [<a href="wikipedia://wiki/search:?id=*talk:%TITLE%">Talk</a>]</body>\n</html>';


var Wiki = {
  gZipData: null, 

  getWikiZipData: function() {
    if (!this.gZipData) {
      this.gZipData = Components.classes[WIKIDATACID].createInstance(zpwkIWikiData);
    }

    return this.gZipData;
  },
  parseTitle: function(title) {
    var idx = title.indexOf('#');

    var fragment = "";

    if (idx > 0) {
      fragment = title.substr(idx).replace(/ /g, '_');
      title = title.substr(0, idx);
    }

    return [title, fragment];
  },
  getCategory: function(key) {
    var category;

    if (key.match(/talk:/i)) {
      category = "talk";
    } else if (key.match(/^image:/i)) {
      category = "image";
    } else if (key.match(/^user:/i)) {
      category = "user";
    } else {
      category = "article";
    }

    return category;
  },    

  getPageData: function(key, noredirect) {
    var zips = this.getWikiZipData();

    var category = this.getCategory(key);

    var retval = {
      title: key,
      fragment: "",
      text: "No such page",
      found: false
    };


    var zip = zips.getData(category);
    
    if (zip) {
      try {
	var ins = zip.getInputStream(key);
    
	var sis = Components.classes["@mozilla.org/scriptableinputstream;1"].
	  createInstance(Components.interfaces.nsIScriptableInputStream);
	sis.init(ins);
      
	retval.text = sis.read(sis.available());
    
	if (!noredirect) {
	  retval.text = retval.text.replace(/\s*(<!--.*-->)\s*/g, '');
	  var r = retval.text.match(/^\W*#[Rr][Ee][Dd][Ii][Rr][Ee][Cc][Tt]\s*:?\s*\[\[([^\]]+)\]\]/);
    
	  if (r) {
	    var res = this.parseTitle(r[1]);
	  
	    var title = res[0];
	  
	    var fragment = res[1];
	  
	    retval = this.findPage(Wiki.escape(title));
	    retval.fragment = fragment;
	    retval.text = "(Redirected from " + key + ")\n" + retval.text;
	  }
	}
	retval.found = true;
      } finally {
	zips.releaseData();
      }
    }

    return retval;
  },
  escape: function(searchTerm) {
    searchTerm = searchTerm.replace(/([\[\]\(\)\*])/g, "\\$1");
    return searchTerm;
  },
  findPage: function(title) {
    var list;
    
    var zips = this.getWikiZipData();

    var category = this.getCategory(title);

    var retval = {title: title, fragment: "", text: "No such page", found: false};

    var zip = zips.getData(category);

    if (zip) {
      try {
	zip.getEntry(title);
	list = [title];
      } catch (e) {
	list = this.findEntries(title);
      } finally {
	zips.releaseData();
      }


      switch (list.length) {
      case 1:
	retval = this.getPageData(list[0]); // recursive call to this function for redirected pages!
	break;
      case 0:
	retval.title = "Search Results - Zipedia";
	retval.text = "Error: no page result for " + title;
	break;
      default:
	text = "<h2><nowiki>" + list.length + " Search Results for " + title + "</nowiki></h2>\n";
	
	for (var i = 0; i < list.length; ++i) {
	  text += "# [[" + list[i] + "]]\n";
	}

	retval.title = "Search Results - Zipedia";
	retval.text = text;
	
	break;
      }
    }
    return retval;
  },
  makeSearchTerm: function(text) {
    var search = "";

    for (var i = 0; i < text.length; ++i) {
      var origChar = text.charAt(i);
      var capitalized = origChar.toUpperCase();

      if (origChar == capitalized) {
	switch (origChar) {
	case '_':
	case ' ':
	  search += "[ _]";
	  break;
	default:
	  search += origChar;
	  break;
	}
      } else {
	search += "[" + origChar + capitalized + "]";
      }
    }
    return search;
  },
  findEntries: function(text) {
    var zips = this.getWikiZipData();

    // make search term case insensitive, replace '_' with '[ _]'

    var search = this.makeSearchTerm(text);

    var res = new Array();

    var categories = zips.categories.split(' ');

    for (var i in categories) {
      var category = categories[i];

      var zip = zips.getData(category);

      if (zip) {
	try {
	  var it = zip.findEntries(search);

	  var count = 0;

	  while (it.hasMoreElements() && count++ < 50) {
	    var e = it.getNext().QueryInterface(Components.interfaces.nsIZipEntry);
	
	    res.push(e.name);
	  }
	} finally {
	  zips.releaseData();
	}
      }
    }
    return res;
  },
};

var ZipediaSearchChannel = {
  fixUri: function(uri, info) {
    uri.spec = "wikipedia://wiki/" + info.title + info.fragment;
  },
  makeChannel: function(title, uri, makehtml) {
    var nsIPipe = Components.interfaces.nsIPipe;
    var nsIInputStreamChannel = Components.interfaces.nsIInputStreamChannel;

    var channel = Components.classes["@mozilla.org/network/input-stream-channel;1"].createInstance(nsIInputStreamChannel);
    var pipe = Components.classes["@mozilla.org/pipe;1"].createInstance(nsIPipe);

    pipe.init(false, false, 0, 0, null);


    channel.contentStream = pipe.inputStream;
    channel.contentType = makehtml ? "text/html" : "text/plain";
    channel.contentCharset = "utf8";
    channel.contentLength = 100000000; //-1;
    channel.setURI(uri);


    var runnable = {
      outputStream: pipe.outputStream,
      channel: makeProxy(channel, Components.interfaces.nsIInputStreamChannel),
      //channel: channel,
      title: title,
      uri: uri,
      makehtml: makehtml,

      run: function() {

	if (makehtml) {
	  var info = null;

	  var found = false;

	  try {
	    info = Wiki.findPage(this.title);

	    found = true;
	  } catch (e) {
	    var oldTitle = this.title;
	    this.title = "Error: No Wiki Pages Found";
	    this.data = "No pages found for term \"" + oldTitle + " \nOriginal error: " + (this.makehtml ? "<br/>\n<pre>" + e + "</pre>" : e);
	  }

	  if (info) {
	    this.title = info.title;

	    if (info.found) {
	      ZipediaSearchChannel.fixUri(this.uri, info);
	    }

	    this.channel.originalURI = this.uri;
	    
	    var location = this.uri.resolve("");
	    
	    var wikiConverter = 
	    Components.classes["@nargila.org/wiki-convert;1"].
	    createInstance(Components.interfaces.zpwkIWikiConvert);
	    
	    this.data = wikiConverter.convert(info.text, location);
	  }
	  
	  this.data = HTML_HEAD.replace(/%TITLE%/g, this.title) + this.data + HTML_TAIL.replace(/%TITLE%/g, this.title);
	} else {
	  this.data = Wiki.getPageData(this.title, true).text; 
	}
	try {
	  this.outputStream.write(this.data, this.data.length);
	  this.outputStream.close();
	} catch (e) {
	  /* 
	   * user probably cancelled page loading
	   */
	}
      }
    };

    makeThread(runnable);

    //runnable.run();

    return channel;
  }
};


var tracingEnabled = false;

function trace(msg) {
  if (tracingEnabled) {
    Components.classes[kCONSOLESERVICE_CONTRACTID].getService(nsIConsoleService).logStringMessage(msg);
  }
};

var ZipediaModule = {
  
  /* CID for this class */
  cid: kPROTOCOL_CID,

  /* Contract ID for this class */
  contractId: kPROTOCOL_CONTRACTID,

  registerSelf : function(compMgr, fileSpec, location, type) {
    compMgr = compMgr.QueryInterface(nsIComponentRegistrar);
    compMgr.registerFactoryLocation(
      kPROTOCOL_CID, 
      kPROTOCOL_NAME, 
      kPROTOCOL_CONTRACTID, 
      fileSpec, 
      location,
      type
    );
  },
  
  getClassObject : function(compMgr, cid, iid) {
    if (!cid.equals(kPROTOCOL_CID)) {
      throw Components.results.NS_ERROR_NO_INTERFACE;
    }
    if (!iid.equals(nsIFactory)) {
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
                        
      return new ZipediaHandler().QueryInterface(iid);
    }
  }
};

function NSGetModule(compMgr, fileSpec) {
    return ZipediaModule;
}

/*----------------------------------------------------------------------
 * The Zipedia Handler
 *----------------------------------------------------------------------
 */

function ZipediaHandler() {
  trace("[ZipediaHandler.<init>]");
  
  this.wrappedJSObject = this;
  
  this._system_principal = null;
  
}

ZipediaHandler.prototype = {

  scheme: kSCHEME,
  
  defaultPort : -1,
  
  protocolFlags : nsIProtocolHandler.URI_STD,
  
  
  allowPort : function(port, scheme) {
    trace("[ZipediaHandler.allowPort]");
    
    return false;
  },
  newURI : function(spec, charset, baseURI) {
    trace("[ZipediaHandler.newURI] " + spec);
      
    var new_url = Components.classes[kSTANDARDURL_CONTRACTID].createInstance(nsIStandardURL);
    new_url.init(kURLTYPE_STANDARD, -1, spec, charset, null);    
    
    var new_uri = new_url.QueryInterface(nsIURI);
    return new_uri;
  },  
  _makeStringStream: function(strData) {
    var dataStream = 
    Components.classes["@mozilla.org/io/string-input-stream;1"]
    .createInstance(Components.interfaces.nsIStringInputStream);

    dataStream.setData(strData, strData.length);

    return dataStream;
  },
  makeStringChannel: function(strData, contentType, uri) {
    var inputStream = this._makeStringStream(strData);
    var channel = this.makeStreamChannel(inputStream, contentType, uri);
    channel.contentLength = strData.length;

    return channel;
  },
  makeStreamChannel: function(inputStream, contentType, uri) {
    var channel = Components.classes["@mozilla.org/network/input-stream-channel;1"].createInstance(Components.interfaces.nsIInputStreamChannel);

    channel.contentStream = inputStream;

    channel.setURI(uri);
    channel.contentType = contentType;
    channel.contentCharset = "utf8";
    
    return channel;
  },
  newChannel: function(origuri) {
    trace("[ZipediaHandler.newChannel] new channel requested for: " + origuri.spec);
    trace("[ZipediaHandler.newChannel(path)] new channel requested for: " + origuri.path);

    var uri = this.newURI(origuri.spec); // duplicate uri for path mannipulation

    uri.spec = origuri.resolve("");	// this removes any '#bla, or ?query

    var data;
    
    var wikipedia = "wikipedia:";
    
    var title = uri.path.substr(1);;

    title = urldecode(title);

    var host = uri.host;

    var contentType = "text/html";

    switch (host) {
    case "raw":
      var makehtml = false;
      return ZipediaSearchChannel.makeChannel(title, origuri, makehtml);
    case "wiki":
      var makehtml = true;

      var searchTerm;

      if (title.indexOf("search:") == 0) {
	var line = title.substr(title.indexOf("?"));

	var r = line.match(/id=(.+)/);

	if (r) {
	  searchTerm = r[1].replace(/\+/g, ' ');
	}
      } else {
	searchTerm = Wiki.escape(title);
      }
	
      return ZipediaSearchChannel.makeChannel(searchTerm, origuri, makehtml);
    default:
      data = "<h1>Unknown prefix " + uri.host + "</h1>";
      break;
    }

    var channel = this.makeStringChannel(data, contentType, origuri);

    return channel;
  },
  QueryInterface : function(iid) {
    trace("[ZipediaHandler.QueryInterface]");

    if (!iid.equals(Components.interfaces.nsIProtocolHandler) &&
      !iid.equals(Components.interfaces.nsISupports)) {
      
      trace("[ZipediaHandler.QueryInterface] error - NS_ERROR_NO_INTERFACE " + iid);
      
      throw Components.results.NS_ERROR_NO_INTERFACE;
    }
    return this;
  }
};
const CHARTABLE = new Array('\x00', '\x01', '\x02', '\x03', '\x04', '\x05', '\x06', '\x07', '\x08', '\t', '\n', '\x0b', '\x0c', '\r', '\x0e', '\x0f', '\x10', '\x11', '\x12', '\x13', '\x14', '\x15', '\x16', '\x17', '\x18', '\x19', '\x1a', '\x1b', '\x1c', '\x1d', '\x1e', '\x1f', ' ', '!', '"', '#', '$', '%', '&', "'", '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '\x7f', '\x80', '\x81', '\x82', '\x83', '\x84', '\x85', '\x86', '\x87', '\x88', '\x89', '\x8a', '\x8b', '\x8c', '\x8d', '\x8e', '\x8f', '\x90', '\x91', '\x92', '\x93', '\x94', '\x95', '\x96', '\x97', '\x98', '\x99', '\x9a', '\x9b', '\x9c', '\x9d', '\x9e', '\x9f', '\xa0', '\xa1', '\xa2', '\xa3', '\xa4', '\xa5', '\xa6', '\xa7', '\xa8', '\xa9', '\xaa', '\xab', '\xac', '\xad', '\xae', '\xaf', '\xb0', '\xb1', '\xb2', '\xb3', '\xb4', '\xb5', '\xb6', '\xb7', '\xb8', '\xb9', '\xba', '\xbb', '\xbc', '\xbd', '\xbe', '\xbf', '\xc0', '\xc1', '\xc2', '\xc3', '\xc4', '\xc5', '\xc6', '\xc7', '\xc8', '\xc9', '\xca', '\xcb', '\xcc', '\xcd', '\xce', '\xcf', '\xd0', '\xd1', '\xd2', '\xd3', '\xd4', '\xd5', '\xd6', '\xd7', '\xd8', '\xd9', '\xda', '\xdb', '\xdc', '\xdd', '\xde', '\xdf', '\xe0', '\xe1', '\xe2', '\xe3', '\xe4', '\xe5', '\xe6', '\xe7', '\xe8', '\xe9', '\xea', '\xeb', '\xec', '\xed', '\xee', '\xef', '\xf0', '\xf1', '\xf2', '\xf3', '\xf4', '\xf5', '\xf6', '\xf7', '\xf8', '\xf9', '\xfa', '\xfb', '\xfc', '\xfd', '\xfe', '\xff');

function getChar(code) {
  return CHARTABLE[parseInt(code)];
}

function urldecode(url) {
  var res = "";

  var l = url.split("%");

  var res = l[0];

  for (var i = 1; i < l.length; ++i) {
    var s = l[i];
	    
    var code = parseInt('0x' + s.substr(0, 2));
	    
    res += getChar(code);
	    
    res += s.substr(2);
  }

  return res;
}


function makeProxy(obj, iid, needsWrapping) {

  if (needsWrapping) {
    obj.QueryInterface = function(iid) {
      if (iid.equals(iid) || 
	  iid.equals(Components.interfaces.nsISupports)) {
	return this;
      }
      
      throw Components.results.NS_ERROR_NO_INTERFACE;
    };
  }
  var proxyCID = "@mozilla.org/xpcomproxy;1";
  var nsIProxyObjectManager = Components.interfaces.nsIProxyObjectManager;

  var proxyManager = Components.classes[proxyCID].createInstance(nsIProxyObjectManager);
  var proxy = 
    proxyManager.getProxyForObject(null, iid, obj, 5);

  return proxy;
}

function makeEventQueueProxy(queue, obj, iid) {

  if (needsWrapping) {
    obj.QueryInterface = function(iid) {
      if (iid.equals(iid) || 
	  iid.equals(Components.interfaces.nsISupports)) {
	return this;
      }
      
      throw Components.results.NS_ERROR_NO_INTERFACE;
    };
  }
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

  return eventservice.createFromIThread(thread, false);
}
  
  
function makeThread(runnable) {
  var nsIThread = Components.interfaces.nsIThread;

  var thread = Components.classes["@mozilla.org/thread;1"].createInstance(nsIThread);

  thread.init(runnable, 0, 
	      nsIThread.PRIORITY_NORMAL,
	      nsIThread.SCOPE_GLOBAL,
	      nsIThread.STATE_UNJOINABLE);

}


function getStringPref(branch, name) {
  var prefs = Components.classes[kPREFERENCES_SERVICE].getService(nsIPrefService);

  var branch = prefs.getBranch(branch);

  return branch.getCharPref(name);
}
