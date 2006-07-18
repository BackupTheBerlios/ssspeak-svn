
var gLocalFonts = null;
var gStringBundle = null;
var gUserHomeDir = null;
var kFixedFontFaceMenuItems = 1;

function LocalFile(path) {
  const myFile = new Components.Constructor("@mozilla.org/file/local;1", 
                                            "nsILocalFile",
                                            "initWithPath");
  return new myFile(path);
}

function getHomeDir() {
  if (!gUserHomeDir) {
    const nsIDirectoryServiceProvider_CONTRACTID = "@mozilla.org/file/directory_service;1";
    const nsIDirectoryServiceProvider = Components.interfaces.nsIDirectoryServiceProvider;
    var dirServiceProvider = Components.classes[nsIDirectoryServiceProvider_CONTRACTID]
      .getService(nsIDirectoryServiceProvider);
    var persistent = new Object();

    gUserHomeDir = dirServiceProvider.getFile("Home", persistent);
  }

  return gUserHomeDir.path;
}

function fileBrowse(mode, defaultDir, suggestName, filters)
{
	var nsIFilePicker = Components.interfaces.nsIFilePicker;
	var fp = Components.classes["@mozilla.org/filepicker;1"].createInstance(nsIFilePicker);

	if(mode=="open")
		fp.init(window, localize('Open'), nsIFilePicker.modeOpen);
	else if(mode=="save") {
	  fp.init(window, localize('SaveAs'), nsIFilePicker.modeSave);
	  if (suggestName) fp.defaultString = suggestName;
	} else if(mode=="folder")
		fp.init(window, localize('SaveFolder'), nsIFilePicker.modeGetFolder);

	for (var i in filters) {
	  var f = filters[i];
	  fp.appendFilter(f[0], f[1]);
	}

	fp.appendFilters( nsIFilePicker.filterAll);

	if(defaultDir && defaultDir.exists()) {
	  fp.displayDirectory = defaultDir;
	} else {
	  getHomeDir();
	  fp.displayDirectory = gUserHomeDir;
	}

	var res=fp.show();
	if (res==nsIFilePicker.returnOK || nsIFilePicker.returnReplace ){
		var thefile=fp.file;
		return thefile;
	}else{
		return false;
	}
}

function fillFileName(mode, suggestName, filters, elementID) {
  var file = fileBrowse(mode, null, suggestName, filters);

  if (file) {
    var err = null;
  
    switch (mode) {
    case "open":
      if (!file.isReadable()) {
	err = "no read permission";
      }
      break;
    case "save":
      if (!file.parent.isWritable()) {
	err = "no write permission";
      }
      break;
    }

    if (err) {
      alert(file.path + ": " + err);
    } else {
      document.getElementById(elementID).value = file.path;
    }
  }
}


function goToggleToolbar( id, elementID )
{
  var toolbar = document.getElementById(id);
  var element = document.getElementById(elementID);
  if (toolbar)
  {
    var isHidden = toolbar.hidden;
    toolbar.hidden = !isHidden;
    document.persist(id, 'hidden');
    if (element) {
      element.setAttribute("checked", isHidden ? "true" : "false");
      document.persist(elementID, 'checked');
    }
  }
}

function getFolderPath()
{
	var newfile = fileBrowse("folder");
	return FileIO.path(newfile);
}

function getFilePath()
{
	var newfile = fileBrowse("open");
	return FileIO.path(newfile);
}

function AlertWithTitle(title, message, parentWindow)
{
  if (!parentWindow)
    parentWindow = window;

  var promptService = Components.classes["@mozilla.org/embedcomp/prompt-service;1"].getService();
  promptService = promptService.QueryInterface(Components.interfaces.nsIPromptService);

  if (promptService)
  {
    if (!title)
      title = GetString("Alert");

    // "window" is the calling dialog window
    promptService.alert(parentWindow, title, message);
  }
}

// Optional: Caller may supply text to substitue for "Ok" and/or "Cancel"
function ConfirmWithTitle(title, message, okButtonText, cancelButtonText)
{
  var promptService = Components.classes["@mozilla.org/embedcomp/prompt-service;1"].getService();
  promptService = promptService.QueryInterface(Components.interfaces.nsIPromptService);

  if (promptService)
  {
    var okFlag = okButtonText ? promptService.BUTTON_TITLE_IS_STRING : promptService.BUTTON_TITLE_OK;
    var cancelFlag = cancelButtonText ? promptService.BUTTON_TITLE_IS_STRING : promptService.BUTTON_TITLE_CANCEL;

    return promptService.confirmEx(window, title, message,
                            (okFlag * promptService.BUTTON_POS_0) +
                            (cancelFlag * promptService.BUTTON_POS_1),
                            okButtonText, cancelButtonText, null, null, {value:0}) == 0;
  }
  return false;
}

function ConfirmAdvanced(dialogTitle, dialogMsg, okButtonText, cancelButtonText, extraButtonText)
{
  var promptService = Components.classes["@mozilla.org/embedcomp/prompt-service;1"].getService();
  promptService = promptService.QueryInterface(Components.interfaces.nsIPromptService);
  var result = 1;
  if (promptService)
  {
    var okFlag = okButtonText ? promptService.BUTTON_TITLE_IS_STRING : promptService.BUTTON_TITLE_SAVE;
    var cancelFlag = cancelButtonText ? promptService.BUTTON_TITLE_IS_STRING : promptService.BUTTON_TITLE_CANCEL;
    var extraFlag = extraButtonText ? promptService.BUTTON_TITLE_IS_STRING : promptService.BUTTON_TITLE_DONT_SAVE;

    result = promptService.confirmEx(window, dialogTitle, dialogMsg,
                            (okFlag * promptService.BUTTON_POS_0) +
                            (cancelFlag * promptService.BUTTON_POS_1)+
                            (extraFlag * promptService.BUTTON_POS_2),
                            okButtonText, cancelButtonText, extraButtonText, null, {value:0});
  }
  return result;
}
function infoWin(img, title, message)
{
    const params = Components.classes["@mozilla.org/embedcomp/dialogparam;1"]
                             .createInstance(Components.interfaces.nsIDialogParamBlock);
    params.SetNumberStrings(3);
    params.SetString(0, img);
    params.SetString(1, title);
    params.SetString(2, message);
    const ww = Components.classes["@mozilla.org/embedcomp/window-watcher;1"]
                         .getService(Components.interfaces.nsIWindowWatcher);
    return ww.openWindow(null, "chrome://ssspeak/content/dialogs/info.xul", "info", "chrome,all,centerscreen,alwaysRaised,dialog,modal=no,resizable=no", params);

}

if(typeof openHelp=='undefined'){
	function openHelp() {
		return openBrowserURL('http://ssspeak.com/support');
	}
}

function localize(name)
{
  if (!gStringBundle)
  {
    try {
      var strBundleService =
          Components.classes["@mozilla.org/intl/stringbundle;1"].getService(); 
      strBundleService = 
          strBundleService.QueryInterface(Components.interfaces.nsIStringBundleService);

      gStringBundle = strBundleService.createBundle("chrome://ssspeak/locale/ssspeak.properties"); 

    } catch (ex) {}
  }
  if (gStringBundle)
  {
    try {
      return gStringBundle.GetStringFromName(name);
    } catch (e) {}
  }
  return null;
}

function SupportsArray()
{
	return Components.classes['@mozilla.org/supports-array;1']
							.createInstance(Components.interfaces.nsISupportsArray);
}

function IsWhitespace(string)
{
  return /^\s/.test(string);
}

function GetSelectionAsText()
{
  try {
    return GetCurrentEditor().outputToString("text/plain", 1); // OutputSelectionOnly
  } catch (e) {}

  return "";
}
function stripChars(string) {
    string = string.replace(/\\/g,"\\\\");
    string = string.replace(/\(/g,"\\(");
    string = string.replace(/\)/g,"\\)");
    string = string.replace(/\$/g,"\\$");
    string = string.replace(/\^/g,"\\^");
    string = string.replace(/\[/g,"\\[");
    string = string.replace(/\]/g,"\\]");
    string = string.replace(/\{/g,"\\{");
    string = string.replace(/\}/g,"\\}");
    string = string.replace(/\+/g,"\\+");
    string = string.replace(/\?/g,"\\?");
    string = string.replace(/\./g,"\\.");
    string = string.replace(/\|/g,"\\|");

    return string;
}
function TextIsURI(selectedText)
{
  return selectedText && /^http:\/\/|^https:\/\/|^file:\/\/|\
    ^ftp:\/\/|^about:|^mailto:|^news:|^snews:|^telnet:|^ldap:|\
    ^ldaps:|^gopher:|^finger:|^javascript:/i.test(selectedText);
}

function openBrowserURL(url)
{
	var b = getContentBrowser();
	if(b){
		gotoLink(url);
	}else{
		try{
			open(url);
		}catch(ex){
			var winw = Components.classes["@mozilla.org/embedcomp/window-watcher;1"]
                         .getService(Components.interfaces.nsIWindowWatcher);
			winw.openWindow(null, url, "_blank", "chrome,all,dialog=no,width=500,height=400,sizemode=maximized",
				Components.classes["@mozilla.org/embedcomp/dialogparam;1"]
                             .createInstance(Components.interfaces.nsIDialogParamBlock));
			return true;
		}
	}
	return true;
}
function launchPreview(targetpath, url)
{
	var targetFile = FileIO.open(targetpath);

	var process = Components.classes['@mozilla.org/process/util;1'].getService(Components.interfaces.nsIProcess);
	process.init(targetFile);
	var arguments= [] ;

	arguments.push(url);

	process.run(false, arguments, arguments.length,{});
}

function goToggleElement( id, elementID )
{
  var toolbar = document.getElementById( id );
  var element = document.getElementById( elementID );
  if ( toolbar )
  {
    var attribValue = toolbar.getAttribute("hidden") ;

    if ( attribValue == "true" )
    {
      toolbar.setAttribute("hidden", "false" );
      if ( element )
        element.setAttribute("checked","true");
    }
    else
    {
      toolbar.setAttribute("hidden", true );
      if ( element )
        element.setAttribute("checked","false");
    }
    document.persist(id, 'hidden');
    document.persist(elementID, 'checked');
  }
}

function colID(tree, id){

}

function initLocalFontFaceMenu(menuPopup)
{
  if (!gLocalFonts)
  {
    // Build list of all local fonts once per editor
    try 
    {
      var enumerator = Components.classes["@mozilla.org/gfx/fontenumerator;1"]
                                 .getService(Components.interfaces.nsIFontEnumerator);
      var localFontCount = { value: 0 }
      gLocalFonts = enumerator.EnumerateAllFonts(localFontCount);
    }
    catch(e) { }
  }
  
  var useRadioMenuitems = (menuPopup.parentNode.localName == "menu"); // don't do this for menulists  
  if (menuPopup.childNodes.length == kFixedFontFaceMenuItems) 
  {
    if (gLocalFonts.length == 0) {
      menuPopup.childNodes[kFixedFontFaceMenuItems - 1].hidden = true;
    }
    for (var i = 0; i < gLocalFonts.length; ++i)
    {
      if (gLocalFonts[i] != "")
      {
        var itemNode = document.createElementNS(XULNS, "menuitem");
        itemNode.setAttribute("label", gLocalFonts[i]);
        itemNode.setAttribute("value", gLocalFonts[i]);
        if (useRadioMenuitems) {
          //itemNode.setAttribute("type", "radio");
          itemNode.setAttribute("name", "2");
          itemNode.setAttribute("observes", "cmd_formatBlock");
        }
        menuPopup.appendChild(itemNode);
      }
    }
  }
}
function osPath(path)
{
	path = unescape(path);
	if(gOS==gWin)
		return path.replace('file:///', '').replace('|',':').replace(/\//g, '\\');
	else
		return path.replace('file://', '');
};

function getExt(path) {
	var rv='';
	try {
		var dotIndex  = path.lastIndexOf('.');
		rv=(dotIndex >= 0) ? path.substring(dotIndex+1) : "";
		return rv;
	}catch(e) {
		return '';
	}
}

function getBrowserPath(path) {
	try {
		return 'file:///' + path.replace(/\\/g, '\/')
			.replace(/^\s*\/?/, '').replace(/\ /g, '%20');
	}
	catch(e) {
		return false;
	}
}

function gotoLink(URL) {
	var brw = getContentBrowser();
	if (true) {
		var newtab = brw.addTab(URL);
		brw.selectedTab = newtab;
		return newtab;
	} else {
		return brw.loadURI(URL);
	}
}

function getContentBrowser() {
	var windowManager = Components.classes['@mozilla.org/appshell/window-mediator;1'].getService(Components.interfaces.nsIWindowMediator);
	var topWindowOfType = windowManager.getMostRecentWindow("navigator:browser");
	if (topWindowOfType) {
		return topWindowOfType.document.getElementById('content');
	}
	return null;
}


//-----------------------------------------------------------------------------------
function alertObject(obj)
{
  var names = "";
  for (var i in obj)
  {
    if (i == "value")
      names += i + ": " + obj.value + "\n";
    else if (i == "id")
      names += i + ": " + obj.id + "\n";
    else
      names += i +"\t=\t"+String(obj[i])+ "\n";
  }
  
  alert("-----" + obj + "------\n"+names + "-----------\n");
}

//-----------------------------------------------------------------------------------
function PrintObject(obj)
{
  dump("-----" + obj + "------\n");
  var names = "";
  for (var i in obj)
  {
    if (i == "value")
      names += i + ": " + obj.value + "\n";
    else if (i == "id")
      names += i + ": " + obj.id + "\n";
    else
      names += i + "\n";
  }
  
  dump(names + "-----------\n");
}

//-----------------------------------------------------------------------------------
function PrintNodeID(id)
{
  PrintObject(document.getElementById(id));
}


String.prototype.trim = function(){
	return this.replace(/(^\s+)|(\s+$)/g, '');
};


String.prototype.replaceWhitespace = function(replaceVal)
{
	return this.replace(/(^\s+)|(\s+$)/g,'').replace(/\s+/g,replaceVal)
}

String.prototype.convertToCDATA = function()
{
  return this.replace(/\s+/g,"_").replace(/[^a-zA-Z0-9_\.\-\:\u0080-\uFFFF]+/g,'');
}

/*
Array.prototype.removeAt=function(i){
	this.splice(i,1);
};
Array.prototype.sortn=function(i){
	this.sort(function(a,b){return a-b;});
};
*/

String.prototype.capitalize=function()
{
	return this.charAt(0).toUpperCase()+this.substr(1);
};

function toggleCommandSet(id, enable) {
  var commands = document.getElementById(id).childNodes;

  for (var i = 0; i < commands.length; ++i) {
    var command = commands[i];
    
    if (enable) {
      command.removeAttribute("disabled");
    } else {
      command.setAttribute("disabled", "true");
    }
  }
  return;
}

/*
 *  Get the file path to the installation directory of this 
 *  extension.
 */

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

function buildPath(rootPath, pathParts) {
  dump("rootPath=" + rootPath + ", pathParts=" + pathParts + "\n");

  if (!rootPath && (!pathParts || !pathParts.length)) {
    throw "internal error: buildPath(): both arguments are null or empty!";
  }

  var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);

  if (!pathParts) {
    pathParts = [];
  }

  pathParts.reverse();

  if (!rootPath) {
    rootPath = pathParts.pop();
  }

  file.initWithPath(rootPath);

  while (pathParts.length) {
    file.appendRelativePath(pathParts.pop());
  }
  
  return file;
}

function checkReadable(path, relpaths) {
  var file = buildPath(path, relpaths);
  return file.exists() && file.isReadable();
}

function checkWritable(path, relpaths) {
  var file = buildPath(path, relpaths);

  return (file.exists() && file.isWritable()) || (file.parent.exists() && file.parent.isWritable()) ? file : null;
}

function execFile(file, args, blocking) {
  var nsIProcess = Components.interfaces.nsIProcess;

  var process = Components.classes["@mozilla.org/process/util;1"].createInstance(nsIProcess);
  
  process.init(file);

  var pid = process.run(blocking, args, args.length);

  return process;
}


function checkExists(path, relpaths) {
  var file = buildPath(path, relpaths);
  return file.exists() ? file : null;
}

function makeDir(path) {
  var nsILocalFile = Components.interfaces.nsILocalFile;
  var nsIFile = Components.interfaces.nsIFile;

  var file = Components.classes["@mozilla.org/file/local;1"].createInstance(nsILocalFile);

  file.initWithPath(path);

  if (!file.exists()) {
    file.create(nsIFile.DIRECTORY_TYPE, 0755);
  }

  return file;
}

function pathToUri(path) {
  var protocol = "file://";

  if (path[0] != '/') { // windows files start with c: or d: etc.
    protocol += '/'; // jre 1.4 throws UnknownHostExcetion with urls like "file://c:/" - need file:///c:/
  }

  return protocol + path;
}

var gTmpFileCounter = 0;

function makeTmpFile(prefix, suffix) {
  const nsILocalFile = Components.interfaces.nsILocalFile;
  const nsIProperties = Components.interfaces.nsIProperties;
  const dirservCID = '@mozilla.org/file/directory_service;1';
  
  var dir = Components.classes[dirservCID].createInstance(nsIProperties).get('TmpD', nsILocalFile);

  
  dir.appendRelativePath(prefix + gTmpFileCounter++ + suffix);

  return dir.path;
}
