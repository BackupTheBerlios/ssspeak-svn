// JavaScript Document
const kXUL_NS            = "http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul";

gPrefsBranchName = "ssspeak.";


// default prefs
DEF['enableSpeak'] = true;
DEF['enableListen'] = false;
DEF['enableKeyboardEcho'] = true;
DEF['ssspeakDir'] = '';
DEF['dumpSavePath'] = getHomeDir();
DEF['sayRepeatDelay'] = 4000;
DEF['liveSessionsMax'] = 10;
DEF['debug.keepFiles'] = false;
DEF['debug.showJavaConsole'] = false;
DEF['skip.skip0'] = 3;
DEF['skip.skip1'] = 20;
DEF['skip.skip2'] = 80;
DEF['synthesizer.voice.read.volume'] = 'medium';
DEF['synthesizer.voice.read.speed'] = 'medium';
DEF['synthesizer.voice.read'] = 'default';
DEF['synthesizer.voice.announce.volume'] = 'medium';
DEF['synthesizer.voice.announce.speed'] = 'medium';
DEF['synthesizer.voice.announce'] = 'default';
DEF['sounds.assignment.internalLink'] = 'link1.wav';
DEF['sounds.assignment.externalLink'] = 'link2.wav';
DEF['sounds.assignment.relativeLink'] = 'link3.wav';
DEF['sounds.assignment.seekStart'] = 'skipping.wav';
DEF['sounds.assignment.endOfPage'] = 'end.wav';

DEF['festival.prog'] = '';
DEF['festival.lib'] = '';
DEF['festival.dir'] = '';
DEF['festival.cygwin'] = true;
DEF['festival.procPoolSize'] = 5;

function resolveFestivalDir() {
  if (!GetStringPref("festival.dir") && !GetStringPref("festival.prog")) {
    var festival_dir = buildPath(getExtensionPath("ssspeak", true), ["ext", "festival"]).path;

    getVoice().setProperty("festival.dir", festival_dir);
  }
}

function syncVoiceProperties() {
  var prefs = GetPrefs();
  nsIPrefBranch = Components.interfaces.nsIPrefBranch;

  var keyList = prefs.getChildList("", []);

  var voice = getVoice();

  for (var i in keyList) {
    var prefVal = null;
    var type = prefs.getPrefType(keyList[i]);
    var key = keyList[i];

    switch (type) {
    case nsIPrefBranch.PREF_STRING:
    case nsIPrefBranch.PREF_BOOL:
      prefVal = (nsIPrefBranch.PREF_STRING == type) ? prefs.getCharPref(key) :  prefs.getBoolPref(key);

      if (!prefVal) {
	voice.removeProperty(key);
	continue;
      }
      break;
    case nsIPrefBranch.PREF_INT:
      prefVal = prefs.getIntPref(key);
      break;
    default:
      break;
    }
    
    voice.setProperty(key, prefVal);
  }

  resolveFestivalDir();

  voice.configurationChanged();
}

function acceptPrefDialog() {
  if (setPrefs()) {
    syncVoiceProperties();

    return setVoices(); // setVoices() must come after setPrefs(), because it depends on java property settings
  }

  return false;
}

function getVoice() {
  var ssspeak = Components.classes["@nargila.org/ssspeak;1"].createInstance(Components.interfaces.nsISsspeak);
  var voice = ssspeak.voice.wrappedJSObject;
  return voice;
}

function makeVoiceList() {
  var voice = getVoice();
  
  var voices;

  try {
    voices = voice.getVoices();
  } catch (e) {
    alert("could not fetch voices - check your festival executable path settings");
    return null;
  } 
  
  var voicelist = ["default"];

  for (var i in voices) { // copy java array to javascript native string list (or else indexOf() may not find the string)
    voicelist.push(String(voices[i]));
  }
  
  return voicelist;
}

function setVoices() {
  var voicelist = makeVoiceList();

  if (voicelist) {

    for (var voiceCategory in {"read":1, "announce":1}) {
      var voicepop = document.getElementById("synthesizer.voice." + voiceCategory);
      var currentVoice = voicepop.value;
    
      var voices = null;

      voicepop.removeAllItems();

      for (var i in voicelist) {
	voicepop.appendItem(voicelist[i], voicelist[i]);
      }    

      { // set voice 
	var idx = voicelist.indexOf(currentVoice);
      
	if (-1 == idx) {
	  idx = 0;
	}

	voicepop.selectedIndex = idx;
      }
    }

    return true;
  }

  return false;
}
  
function initPrefDialog() {
  initPrefWin();

  setVoices();
}
    
var gDoClear = false;
function clearHistory(){
	gDoClear = true;
}
