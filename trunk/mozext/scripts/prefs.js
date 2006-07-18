// main preferences script

// default pref array
const DEF = [];

var gPrefsBranch = null;
var gPrefsService = null;
var gStylesheet = null;
var gPrefsBranchName = "setme"; // e.g. 'ssspeak'
var gRule_1 = 0;

function initPrefs() {
	try {
		var prefBranch = GetPrefs(), i;
		if (GetBoolPref("firstrun")){
			prefBranch.setBoolPref("firstrun", "false");
		}
		loadRecentFiles();
		loadExternalBrowsers();
		// set editor css style rules
		setCodePrefs();

		// set temporary file checkbox
		document.getElementById('use-temps-menuitem').setAttribute("checked", GetBoolPref("previewtmp"));

		// Why doesn't this work? Changing the wrap attribute has no effect on the textboxes?
		/*
		if (GetBoolPref("codewrap")){
			for(i=0;i<codetch.docs.length;i++)
				codetch.docs[i].panel.panels['code'].element.removeAttribute('wrap');
		}else{
			for(i=0;i<codetch.docs.length;i++)
				codetch.docs[i].panel.panels['code'].element.setAttribute('wrap', 'off');
		}
		alert(codetch.docs[0].panel.panels['code'].element.getAttribute('wrap'));
		//*/

	} catch (ex) {alert(ex);}
}
function setCodePrefs(){
		gStylesheet = document.styleSheets[0];
		gRule_1 = gStylesheet.insertRule('.editor-code textarea, .editor-code-html textarea {'
		+'color: '+GetStringPref('codecolor')
		+' !important;font-family: '+GetStringPref('codefont')
		+' !important;font-size: '+GetStringPref('codefontsize')
		+' !important;background-color: '+GetStringPref('codebg')
		+' !important;'+GetStringPref('codecss')+'}', gStylesheet.cssRules.length);
}

function GetPrefsService()
{
  if (gPrefsService)
    return gPrefsService;

  try {
    gPrefsService = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService);
  }
  catch(ex) {
    dump("failed to get prefs service!\n");
  }

  return gPrefsService;
}

function GetPrefs()
{
  if (!gPrefsBranch) {
    try {
      var prefService=GetPrefsService();
      if (prefService) {
	gPrefsBranch = prefService.getBranch(gPrefsBranchName);
      }
    } catch(ex) {}
  }
   
  if (!gPrefsBranch) {
    dump("failed to get " + gPrefsBranchName + " prefs!\n");
  }

  return gPrefsBranch;
}

function GetStringPref(name)
{
  try {
    var val = GetPrefs().getCharPref(name);
    return val;
  } catch (e) {}
  return DEF[name];
}

function GetBoolPref(name)
{
  try {
    var val = GetPrefs().getBoolPref(name);
    return val;
  } catch (e) {}
  return (DEF[name]);
}
function GetIntPref(name)
{
  try {
    var val = GetPrefs().getIntPref(name);
    return val;
  } catch (e) {}
  return DEF[name];
}

function SetUnicharPref(aPrefName, aPrefValue)
{
  var prefs = GetPrefs();
  if (prefs)
  {
    try {
      var str = Components.classes["@mozilla.org/supports-string;1"]
                          .createInstance(Components.interfaces.nsISupportsString);
      str.data = aPrefValue;
      prefs.setComplexValue(aPrefName, Components.interfaces.nsISupportsString, str);
    }
    catch(e) {}
  }
}

function GetUnicharPref(aPrefName, aDefVal)
{
  var prefs = GetPrefs();
  if (prefs)
  {
    try {
      var val = prefs.getComplexValue(aPrefName, Components.interfaces.nsISupportsString).data;
      return val;
    }
    catch(e) {}
  }
  return DEF[name];
}

function getPrefValues() {
  var res = {};

  for (key in DEF) {
    res[key] = GetStringPref(key);
  }
  
  return res;
}




// JavaScript Document

function initPrefWin(){
// 	var buttonApply = document.documentElement.getButton("help");

// 	if (buttonApply) {
// 	  buttonApply.accessKey = localize('Apply').charAt(0);
// 	  buttonApply.label=localize('Apply');
// 	}

	var bools = document.getElementsByTagName('checkbox'),
	chars = document.getElementsByTagName('textbox'),
	radios = document.getElementsByTagName('radiogroup'),
	options = document.getElementsByTagName('menulist'),
	i;
	for(i=0;i<bools.length;i++){
		bools[i].checked = GetBoolPref(bools[i].getAttribute('id'));
	}
	for(i=0;i<chars.length;i++){
		chars[i].value= (!chars[i].hasAttribute('preftype'))?
		GetStringPref(chars[i].getAttribute('id')):
		GetIntPref(chars[i].getAttribute('id'));
	}
	for(i=0;i<options.length;i++){
		var value = (!options[i].hasAttribute('preftype'))?
		  GetStringPref(options[i].getAttribute('id')):
		  GetIntPref(options[i].getAttribute('id'));

		var items = options[i].menupopup.childNodes;

		var vals = [];
		
		var len = items.length;

		for (var j = 0; j < len; ++j) {
		  vals.push(items.item(j).value);
		}

		var idx = vals.indexOf(value);

		options[i].selectedIndex = idx;

		options[i].value = value;
	}
	for(i=0;i<radios.length;i++){
		radios[i].value = GetStringPref(radios[i].getAttribute('id'));
	}
}

function setPrefs(){
	var bools = document.getElementsByTagName('checkbox'),
	chars = document.getElementsByTagName('textbox'),
	radios = document.getElementsByTagName('radiogroup'),
	options = document.getElementsByTagName('menulist'),
	i;
	//alert(bools.length);

	for(i=0;i<bools.length;i++){
		GetPrefs().setBoolPref(bools[i].getAttribute('id'), (bools[i].checked));
	}
	for(i=0;i<radios.length;i++){
	  GetPrefs().setCharPref(radios[i].getAttribute('id'), radios[i].value);

	}

	for(i=0;i<chars.length;i++){
		(!chars[i].hasAttribute('preftype'))?
		GetPrefs().setCharPref(chars[i].getAttribute('id'), chars[i].value):
		GetPrefs().setIntPref(chars[i].getAttribute('id'), parseInt(chars[i].value));
	}

	for(i=0;i<options.length;i++){
		(!options[i].hasAttribute('preftype'))?
		GetPrefs().setCharPref(options[i].getAttribute('id'), options[i].value):
		GetPrefs().setIntPref(options[i].getAttribute('id'), parseInt(options[i].value));
	}

	if(gDoClear) GetPrefs().setCharPref('recentfiles','');
	
// 	var conf = getSsspeak().conf;

// 	for (key in DEF) {
// 	  conf.setProperty(key, GetStringPref(key));
// 	}

	return true;
}
var gDoClear = false;
function clearHistory(){
	gDoClear = true;
}

// exaple use: put this in a file or code *after* inclusion of prefs.js:

if (false) {
// default prefs
DEF['enableSpeak'] = true;
DEF['enableListen'] = false;
DEF['enableKeyboardEcho'] = true;
DEF['ssspeakDir'] = '/opt/ssspeak';
DEF['dumpSavePath'] = getHomeDir();
DEF['sayRepeatDelay'] = 4000;
DEF['debug.keepFiles'] = false;
DEF['skip.skip0'] = 3;
DEF['skip.skip1'] = 20;
DEF['skip.skip2'] = 80;
DEF['synthesizer.volume'] = 'medium';
DEF['synthesizer.speed'] = 'medium';
DEF['sounds.assignment.internalLink'] = 'link1.wav';
DEF['sounds.assignment.externalLink'] = 'link2.wav';
DEF['sounds.assignment.relativeLink'] = 'link3.wav';
DEF['sounds.assignment.seekStart'] = 'skipping.wav';
DEF['sounds.assignment.endOfPage'] = 'end.wav';
 }

