var gClassLoader = null;

function ClassLoader() {
    this.policy = null;
    this.java = null;
}

ClassLoader.prototype = {
  init: function(classLoaderPath, java) {
    if (this.java) {
      return;
    }
    var classLoaderURL = new java.net.URL(classLoaderPath);
    var bootstrapClassLoader = java.net.URLClassLoader.newInstance([classLoaderURL]);
    var policyClass = java.lang.Class.forName(
					      "org.nargila.util.mozilla.URLSetPolicy",
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
  },
  addURL: function(url) {
    this.policy.addURL(new this.java.net.URL(url));
  },

  addURLs: function(urls) {
    for (var i in urls) {
      this.addURL(urls[i]);
    }
  },
  newInstance: function(name) {
    var classLoader = new this.java.net.URLClassLoader(this.policy.getUrls());

    this.java.lang.Thread.currentThread().setContextClassLoader(classLoader);

    var javaClass = this.java.lang.Class.forName(name, true, classLoader);

    var instance = javaClass.newInstance();

    return instance;
  },
  getExtensionPath: function(extensionName) {
    var chromeRegistry = 
    Components.classes["@mozilla.org/chrome/chrome-registry;1"].getService(Components.interfaces.nsIChromeRegistry);

    var uri = Components.classes["@mozilla.org/network/standard-url;1"].createInstance(Components.interfaces.nsIURI);

    uri.spec = "chrome://" + extensionName + "/content/";

    var path = chromeRegistry.convertChromeURL(uri);
    if (typeof(path) == "object") {
      path = path.spec;
    }

    path = path.substring(0, path.indexOf("/chrome/") + 1);

    var arr = path.split("jar:"); // just in case the chrome contents is packed into a jar archive

    return arr.length == 2 ?  arr[1] : arr[0];
  }
}
