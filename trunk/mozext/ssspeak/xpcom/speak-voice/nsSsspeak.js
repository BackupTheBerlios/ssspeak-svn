/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mozilla XPCOM Dictionary.
 *
 * The Initial Developer of the Original Code is
 * Digital Creations 2, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2000
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Martijn Pieters <mj@digicool.com> (original author)
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

/*
 *  nsDictionary XPCOM component
 *  Version: $Revision: 1.7 $
 *
 *  $Id: nsDictionary.js,v 1.7 2004/04/18 22:14:12 gerv%gerv.net Exp $
 */

/*
 * Constants
 */
const SSSPEAK_CONTRACTID = '@nargila.org/ssspeak;1';
const SSSPEAK_CID = Components.ID('{36883ab7-1b78-47fb-9b81-f3564efa094e}');
const SSSPEAK_IID = Components.interfaces.nsISsspeak;


/**
 * helper function to fix path from e.g. '/c:/path' to 'c:/path'
 */
function fixPath(unixPath) {
  unixPath = unixPath.replace(/\\/g, '/'); // replace windows '\' with universal '/' - windows XP/2K will not complain
  return unixPath;
}

/**
 * helper function to extract cause Java exception
 */
function getExceptionCause(javaException) {
  while (javaException.getCause()) {
    javaException = javaException.getCause();
  }
  return javaException;
}

/*
 * Class definitions
 */

/* The nsSsspeak class constructor. */
function nsSsspeak() {
}

var gSsspeak = null;

/* the nsSsspeak class def */
nsSsspeak.prototype= {
  java: null,
  voice: null,
  QueryInterface: function(iid) {
    if (!iid.equals(Components.interfaces.nsISupports) &&
	!iid.equals(SSSPEAK_IID))
      throw Components.results.NS_ERROR_NO_INTERFACE;
    return this;
  },
  init: function(javaWrapper, ssspeakDir) {
    if (!this.voice) {
      var java = javaWrapper.wrappedJSObject;

      var javaPathBase = getExtensionPath("ssspeak") + "components/lib"

      var bootStrapPath = javaPathBase + "/class/";

      var classLoader = new ClassLoader(java, bootStrapPath);

      classLoader.addURL(javaPathBase + "/jssspeak.jar");

      try {
	classLoader.getClass("javax.xml.xpath.XPathFactory");
      } catch (e) {
	if (e instanceof java.lang.Exception) {
	  e = getExceptionCause(e);

	  if (e instanceof java.lang.ClassNotFoundException) {
	    /*
	     * jdk 1.4 does not come with jaxp1.3 nor xalan
	     */
	    classLoader.addURL(javaPathBase + "/xalan2.jar");
	    classLoader.addURL(javaPathBase + "/xml-apis.jar");
	    classLoader.addURL(javaPathBase + "/xercesImpl.jar");
	    classLoader.addURL(javaPathBase + "/serializer.jar");
	  } else {
	    // something went wrong..
	    throw e;
	  }
	}
      }
       
      if (!ssspeakDir) {
	
	var path = getExtensionPath("ssspeak", true);
	path = fixPath(path); // windows,unix path compatibility
	ssspeakDir = path + "/ext";
      }

      /*
       * set ssspeakDir first before instanciating Voice
       */
      java.lang.System.setProperty("ssspeakDir", ssspeakDir);


      this.voice = {
	wrappedJSObject: classLoader.newInstance("org.nargila.speak.apps.mozext.Voice")
      };
    }
  }
};

/*
 * Objects
 */

/* nsSsspeak Module (for XPCOM registration) */
var nsSsspeakModule = {
    registerSelf: function(compMgr, fileSpec, location, type) {
        compMgr = compMgr.QueryInterface(Components.interfaces.nsIComponentRegistrar);
        compMgr.registerFactoryLocation(SSSPEAK_CID, 
                                        "nsSsspeak JS component", 
                                        SSSPEAK_CONTRACTID, 
                                        fileSpec, 
                                        location,
                                        type);
    },

    getClassObject: function(compMgr, cid, iid) {
        if (!cid.equals(SSSPEAK_CID))
            throw Components.results.NS_ERROR_NO_INTERFACE;

        if (!iid.equals(Components.interfaces.nsIFactory))
            throw Components.results.NS_ERROR_NOT_IMPLEMENTED;

        return nsSsspeakFactory;
    },

    canUnload: function(compMgr) { return true; }
};

/* nsSsspeak Class Factory */
var nsSsspeakFactory = {
    createInstance: function(outer, iid) {
        if (outer != null)
            throw Components.results.NS_ERROR_NO_AGGREGATION;
    
        if (!iid.equals(SSSPEAK_IID) &&
            !iid.equals(Components.interfaces.nsISupports))
            throw Components.results.NS_ERROR_INVALID_ARG;

	if (!gSsspeak) {
	  gSsspeak = new nsSsspeak();
	}
	return gSsspeak;
    }
}

/*
 * Functions
 */

/* module initialisation */
function NSGetModule(comMgr, fileSpec) { return nsSsspeakModule; }

// vim:sw=4:sr:sta:et:sts:



function ClassLoader(java, classLoaderPath) {
  this.policy = null;
  this.java = java;

  var classLoaderURL = new java.net.URL(classLoaderPath);
  var bootstrapClassLoader = java.net.URLClassLoader.newInstance([classLoaderURL]);
  var policyClass = java.lang.Class.forName(
					    "org.nargila.mozilla.URLSetPolicy",
					    true,
					    bootstrapClassLoader);
        
  // then we inject this class into the security policy of the JVM security manager
  // to have permission to load our own classloader from the classloading jar.
  var policy = policyClass.newInstance();
  policy.setOuterPolicy(java.security.Policy.getPolicy());
  java.security.Policy.setPolicy(policy);

  policy.addPermission(new java.security.AllPermission());
    
  policy.addURL(classLoaderURL);

  this.policy = policy;
  this.java = java;

}

ClassLoader.prototype = {
  addURL: function(url) {
    this.policy.addURL(new this.java.net.URL(url));
  },

  addURLs: function(urls) {
    for (var i in urls) {
      this.addURL(urls[i]);
    }
  },
  getClass: function(name) {
    var classLoader = new this.java.net.URLClassLoader(this.policy.getUrls());

    this.java.lang.Thread.currentThread().setContextClassLoader(classLoader);

    var javaClass = this.java.lang.Class.forName(name, true, classLoader);

    return javaClass;
  },
  newInstance: function(name, args) {
    var classLoader = new this.java.net.URLClassLoader(this.policy.getUrls());

    this.java.lang.Thread.currentThread().setContextClassLoader(classLoader);

    var javaClass = this.getClass(name);

    var instance = args ? javaClass.newInstance(args) : javaClass.newInstance();
    return instance;
  },
};
  

function getExtensionPath(extensionName, returnAsLocalPath) {
  var chromeRegistry = 
    Components.classes["@mozilla.org/chrome/chrome-registry;1"].getService(Components.interfaces.nsIChromeRegistry);

  var uri = Components.classes["@mozilla.org/network/standard-url;1"].createInstance(Components.interfaces.nsIURI);

  uri.spec = "chrome://" + extensionName + "/content/";

  var path = chromeRegistry.convertChromeURL(uri);

  var retval;

  if (returnAsLocalPath) {
    var file = path.QueryInterface(Components.interfaces.nsIFileURL).file;

    while (!file.path.match(/chrome$/)) {
      file = file.parent;
    }

    retval = file.parent.path;
  } else {
    if (typeof(path) == "object") {
      path = path.spec;
    }

    path = path.substring(0, path.indexOf("/chrome/") + 1);
    
    var arr = path.split("jar:"); // just in case the chrome contents is packed into a jar archive

    retval = arr.length == 2 ?  arr[1] : arr[0];
  }

  return retval
}


