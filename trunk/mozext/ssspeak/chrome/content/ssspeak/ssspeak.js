var SynthUtils = {
  guessHtmlProvider: function(url) {
    var htmlProviderName = null;
    
    var wikipediaRe = new RegExp(".*wikipedia.org/wiki/");
    
    if (url == null) {
      var docshell = getBrowser().webNavigation;
      url = docshell.currentURI.spec;
    }
    
    if (wikipediaRe.test(url)) {
      htmlProviderName = "wikipedia";
    }

    return htmlProviderName;
  },
  addLinkIds: function(doc) {
    var links = xPathSearch(doc, "//*[local-name() = 'a' or local-name() = 'A'][@href or @name]");
    
    var absolutLocation = window._content.document.location.href.split(/#/)[0];

    var counter = 0;

    for (var i in links) {

      { /*
	 * add id, if not exist:
	 */
	if (!links[i].getAttribute("id")) {
	  links[i].setAttribute("id", "ssspeak" + counter++);
	}
      }
    }
  },
  prepHtml: function(html, provider) {

    if (!html) {
      html = window._content.document;
    }

    if (!provider) {
      provider = this.guessHtmlProvider(null);
    }
    if (provider  && provider != "IGNORE") {
      if (GetBoolPref("debug.keepFiles")) {
	var tmpFile = makeTmpFile("prepHtml-b4provider", ".html");

	serializeNode(tmpFile, html);
      }
      html = xslt(provider + "2html", html, null);
    }
    return html;
  },
  prepSsml: function(html, provider, outfile) {
    html = this.prepHtml(html, provider);

    if (GetBoolPref("debug.keepFiles")) {
      var tmpFile = makeTmpFile("prepSsml", ".html");
      serializeNode(tmpFile, html);
    }

    var voice = GetStringPref("synthesizer.voice.read");
    var speed = GetStringPref("synthesizer.voice.read.speed");
    var volume = GetStringPref("synthesizer.voice.read.volume");
    
    var params = ["volume=" + volume, 
		  "speed=" + speed, 
		  "sounddir=" + buildPath(getExtensionPath("ssspeak", true), ["ext", "sound"]).path];


    if (voice != "default") {
      params.push("voice=" + voice);
    }
    

    if (outfile) { // dump - do not generate url beeps!
      params.push("marklinks=0");
    }

    var ssml = xslt("html2ssml", html, params);
    
    return ssml;
  }
};

function currentDocAsNode(asssml) {
  var doc = window._content.document;
  if (asssml) {
    return SynthUtils.prepSsml(doc);
  } 
    
  return doc;
}

function currentDocAsFile(asssml) {
  var node = currentDocAsNode(asssml);

  var outfile = makeTmpFile("ssspeak", (asssml ? ".ssml" : ".html"));
    
  serializeNode(outfile, node);

  return outfile;
}

function assert(test) {
  if (!test) {
    throw "assert error";
  }
}

function myDeleteFile(path) {
  if (!GetBoolPref("debug.keepFiles")) {
    try {
      var f = myLocalFile(path);
      f.remove(false);
    } catch (e) {
      Components.utils.reportError(e + ": " + path); 
    }
  }
}
  
var Sounds = {
  voice: Components.classes["@nargila.org/ssspeak;1"].createInstance(Components.interfaces.nsISsspeak).voice.wrappedJSObject,
  soundDir: buildPath(getExtensionPath("ssspeak", true), ["sounds"]).path,
  
  stop: function(eventName) {
    var audioFile = this._getAudioFileForEvent(eventName);
    var player = this.voice.sound(audioFile, true);
    player.stop();
  },

  play: function(eventName, repeat) {
    var audioFile = this._getAudioFileForEvent(eventName);

    var player = this.voice.sound(audioFile, true);

    if (repeat) {
      player.play(repeat);
    } else {
      player.play();
    }
  },

  key: function(event) {
    dump(event);
  },
  _getAudioFileForEvent: function(eventName) {
    var audioFile = GetStringPref("sounds.assignment." + eventName);

    if (-1 == audioFile.indexOf('/')) { // prepend soundDir if relative file name
      audioFile = this.soundDir + "/" + audioFile;
    }

    return audioFile
  }
};

var gSkipManager = null;
    
function SkipManager(synthManager) {
   this.synthManager = synthManager;
   gSkipManager = this;
}

SkipManager.prototype = {
    acummulatedSkipTime: 0,
    defferedTimer: null,
    skip: function(sec) {
      clearTimeout(this.defferedTimer);
      this.synthManager.resetMark();
      this.synthManager.voice.disableSound("SkipManager");
      this.acummulatedSkipTime += sec;
      this.defferedTimer = setTimeout("gSkipManager._skip()", 300);
    },
    _skip: function() {
      

    this.synthManager.voice.skip(this.acummulatedSkipTime);

    this.synthManager.voice.enableSound("SkipManager");

    this.acummulatedSkipTime = 0;

    this.synthManager.refreshCommandState();

    if (this.synthManager.getStatus() == "paused") {
      this.synthManager.speakProgressAnnounce();
    }
  }
}

/**
 * SynthBrowser knows about the mozilla browser, current document, history, etc.
 */
function SynthManagerClass() {
  this.skipManager = new SkipManager(this);
}

SynthManagerClass.prototype = {
  voice: ssspeak.voice.wrappedJSObject,
  timer: null,
  lastElement: null,
  currentUrl: null,
  pending: null,
  busy: false,
  enabled: true,
  lastProgressMessage: "",
  announceJob: null,
  announcement: null,
  progressAnnouncement: {
    loadProgressMessageTimeouts : {},
    lastMessage: "",
    timer: null
  },
  skipManager: null,

  currentIsTemp: false,

  handleMark: function(mark) {
    var doc = window._content.document;

    if (doc && mark) {
      if (this.lastElement) {
	this.lastElement.style.color = null;
      }

      var element = doc.getElementById(mark);
      
      if (element) {
	element.focus();
	element.style.color = "red";
	this.lastElement = element;
	
	var href = element.getAttribute("href");

	if (href) {
	  var eventName;

	  if (href.split("#") == 1) {
	    eventName = "externalLink";
	  } else {
	    eventName = "internalLink";
	  }

	  Sounds.play(eventName);
	}
      }
    }
  },
  QueryInterface : function(aIID)  {
    dump("QueryInterface:\n");
    if (aIID.equals(Components.interfaces.nsIWebProgressListener) ||
	aIID.equals(Components.interfaces.nsISupportsWeakReference) ||
	aIID.equals(Components.interfaces.nsISupports))
      return this;
    throw Components.results.NS_NOINTERFACE;
  },
  refreshCommandState: function() {
    var status = this.getStatus();
    var progress = "" + this.voice.getProgress();
    var hidden = true;

    toggleCommandSet("cmdsAvailableWhenSpeaking", status != "none");
    document.getElementById("cmdSpeak").setAttribute("disabled", status == "none" ? "false" : "true");
    document.getElementById("cmdPause").setAttribute("disabled", status == "paused" || status == "none" ? "true" : "false");
    document.getElementById("cmdResume").setAttribute("disabled", status == "speaking" || status == "none"  ? "true" : "false");

    keyboardEchoEnabled(status != "speaking");

    document.getElementById("pwmeter").hidden = status == "none";
    document.getElementById("pwmeter").value = progress;
  },
  getMarkForTarget: function(target) {
    var doc = window._content.document;

    var elm = xPathSearch(doc, "//*[@id = '" + target + "' or @name = '" + target + "']/preceding-sibling::a[1]")[0];

    if (elm) {
      var mark = elm.getAttribute("id");

      if (mark) {
	return mark;
      }
    }

    return null;
  },
  setCurrent: function(url) {
    Sounds.stop('seekStart');

    if (url) {
      this.voice.setSession(url);
      
      if (this.getStatus() == "paused") {
	this.speakProgressAnnounce();
      }
    } else {
      this.voice.setSession();
    }
  },
  onLocationChange : function(aWebProgress, aRequest, aLocation)  {
    dump("onLocationChange: " + aLocation.spec + "\n");

    assert(aLocation.spec);

    if (this.currentIsTemp) { // always remove temporary 'selection speak' session - if exists
      this.currentIsTemp = false;
      try {
	this.voice.remove("http://example.com");
      } catch (e) {}
    }

    var oldCurrent = this.current;

    this.current = aLocation.spec;

    this.voice.setSession();

    if (!this.pending) {

      if (!this.voice.hasSession(this.current)) {
	if (GetBoolPref("enableSpeak")) {
	  //this.say("preparing for speaking", true, true);
	  this.speak();
	  //setTimeout("SynthManager.say('preparing for speaking', true, true);SynthManager.speak();", 200);
	} else {
	  this.voice.setSession();
	}
      } else {
	var doc = window._content.document;
	
	SynthUtils.addLinkIds(doc);
	
	this.setCurrent(this.current);
      }
      this.refreshCommandState();
    }
  },
  unsay: function(what, cached) {
    try {
      this.voice.say(what, cached).stop();
    } catch (e) {}
  },
  loadProgressAnnounce: function(msg, force) {
    if (!this.currentIsTemp) { // no announcement for temporary selected-text speaking sessions
      if ( !this.progressAnnouncement.loadProgressMessageTimeouts[msg] &&
	   !this.announceJob && (!this.announcement || this.announcement.isStopped())) {
	this.progressAnnouncement.loadProgressMessageTimeouts[msg] = true;
	
	this.announceJob = setTimeout("SynthManager.announcement = SynthManager.say('" + msg + "', true); SynthManager.announceJob = null;", 1000);
	
	setTimeout("SynthManager.progressAnnouncement.loadProgressMessageTimeouts['" + msg + "'] = false", GetIntPref("sayRepeatDelay"));
      }
    }
  },

    say: function(what, cache, drain, repeat) {
      var player = null;
      if (GetBoolPref("enableSpeak")) {
	player = this.voice.say(what, cache == true);

	if (drain) {
	  player.play().drain();
	} else {
	  var repeat_ms = repeat ? repeat : -1;
	  player.play(repeat_ms);
	}      
      }
      return player;
    },    
   resetMark: function() {
    if (this.lastElement) {
      this.lastElement.style.color = null;
      
      this.lastElement = null;
    }
  },
  speak: function() {
    if (!this.current || this.current == "about:blank") {
      return;
    }


    { // debug
      if (GetBoolPref("debug.keepFiles")) {
	var outpath = makeTmpFile("ssspeak-justLoaded", ".html");
	
	serializeNode(outpath, window._content.document);
      }
    }


    SynthUtils.addLinkIds(window._content.document);

    var isssml = false;

    var html = null;

    try {
      var selection = window._content.getSelection().getRangeAt(0);

      if (selection == "") {
	throw "dummy";
      }

      this.currentIsTemp = true;
      html = getSelectionAsHtmlDoc();
    } catch (e) {
      this.currentIsTemp = false;
      html = currentDocAsNode(false);
    }

    var outpath = makeTmpFile("ssspeak", (isssml ? ".ssml" : ".html"));

    serializeNode(outpath, html);

    this.voice.setSession();

    var url = this.currentIsTemp ? "http://example.com" : this.current;

    try {
      this.voice.speakHtml(pathToUri(outpath), url);
    } catch (e) {
      Components.utils.reportError(e); 
      
      this.handleJavaException(e);

      return;
    }

    myDeleteFile(outpath);

    this.resetMark();

    this.setCurrent(url);

    this.refreshCommandState();
  },
  onStateChange : function(aWebProgress, aRequest, aStateFlags, aStatus)  {
    dump("onStateChange:\n");

    if (!(aStateFlags & Components.interfaces.nsIWebProgressListener.STATE_IS_NETWORK) ||
	aWebProgress != getBrowser().webProgress) {
      return 0;
    }

    if (aStateFlags & Components.interfaces.nsIWebProgressListener.STATE_START) {
      dump("onStateChange:start\n");
      this.voice.setSession();
      this.loadProgressAnnounce("loading", true);
      this.pending = aRequest.name;
    } else if (aStateFlags & Components.interfaces.nsIWebProgressListener.STATE_STOP) {
      assert(aRequest.name);

      if (this.announceJob) {
	clearTimeout(this.announceJob);
	this.announceJob = null;
      }

      dump("onStateChange:stop\n");

      this.pending = null;

      if (!this.voice.hasSession(this.current)) {
	if (GetBoolPref("enableSpeak")) {
	  this.speak();
	  //setTimeout("SynthManager.say('preparing for speaking', true, true);SynthManager.speak();", 200);
	}
      } else {
	var doc = window._content.document;

	SynthUtils.addLinkIds(doc);

	this.setCurrent(this.current);
      }
    }
    
    this.refreshCommandState();

    return 0;
  },
  onProgressChange : function(aWebProgress, aRequest,
                              aCurSelfProgress, aMaxSelfProgress,
                              aCurTotalProgress, aMaxTotalProgress)  {

    var progress = parseInt(aCurTotalProgress / aMaxTotalProgress * 100);

    if (!this.currentIsTemp) {
      if (progress > 0 && progress < 100) {
	this.loadProgressAnnounce("" + progress + "%");
      }
    }

    dump("onProgressChange:\n");

    return 0;
  },

  onStatusChange : function(aWebProgress, aRequest, aStatus, aMessage)  {
    this.loadProgressAnnounce(aMessage.split(' ')[0]);

    dump("onStatusChange:" + aMessage + "\n");
  },

  onSecurityChange: function(aWebProgress, aRequest, aState)  {
    dump("onSecurityChange:\n");
  },
  onLinkIconAvailable : function(aWebProgress){
    var dummy = false;
  },
  init: function() {
    syncVoiceProperties();
    this.eventLoop();
  },
  eventLoop: function() {
    var event = this.voice.nextEvent();

    while (event) {
      if (event.getSource() == this.current || (event.getSource() == "http://example.com" && this.currentIsTemp)) {
	var eventName = "" + event.getClass().getName();

	switch (eventName) {
	case "org.nargila.speak.event.synth.SynthesizerMarkEvent":
	  dump("MARK:" + event.getMark() + "\n");
	  this.handleMark(event.getMark());
	  break;
	case "org.nargila.speak.event.player.PlayerStartEvent":
	case "org.nargila.speak.event.player.PlayerPausedEvent":
	case "org.nargila.speak.event.player.PlayerResumedEvent":
	case "org.nargila.speak.event.player.PlayerEndEvent":
	case "org.nargila.speak.event.player.PlayerStopEvent":
	case "org.nargila.speak.event.player.PlayerSeekEvent":
	case "org.nargila.speak.event.player.PlayerOffsetEvent":
	case "org.nargila.speak.event.synth.html.HtmlLinkEvent":
	  break;
	case "org.nargila.speak.event.synth.SynthesizerSeekStartEvent":
	  Sounds.play('seekStart', 800);
	  break;
	case "org.nargila.speak.event.synth.SynthesizerSeekEndEvent":
	  Sounds.stop('seekStart');
	  break;
	case "org.nargila.speak.event.synth.SynthesizerEndEvent":
	  dump(eventName + "\n");
	  if (!this.currentIsTemp) {
	    Sounds.play('endOfPage');
	  }
	  if (this.getStatus() == "speaking") {
	    this.stop();
	  }
	  break;
	default:
	  dump("warning: unhandled event (" + eventName + ")\n");
	  break;
	}
      }

      event = this.voice.nextEvent();
    }

    document.getElementById("pwmeter").value = "" + this.voice.getProgress();

    setTimeout("SynthManager.eventLoop()", 200);
  },
  stop: function() {

    Sounds.stop('seekStart');

    var uri = this.currentIsTemp ? "http://example.com" : this.current;

    this.voice.remove(uri);

    this.refreshCommandState();

    this.currentIsTemp = false;
    return;
  },
  speakProgressAnnounce: function() {
    if (this.progressAnnouncement.timer) {
      clearTimeout(this.progressAnnouncement.timer);
    }

    var message = "at " + this.voice.getProgress() + "%";

    this.unsay(this.lastProgressMessage, true);

    this.lastProgressMessage = message;

    this.progressAnnouncement.timer = setTimeout("SynthManager.say('" + message + "', true)", 500);
  },
    
  pause: function() {
    this.voice.disableSound("SynthManager");
    this.refreshCommandState();
    this.speakProgressAnnounce();
  },
  resume: function() {
    this.voice.enableSound("SynthManager");
    this.refreshCommandState();
  },
  forward: function() {
    window._content.history.go(1);
  },
  "go back": function() {
    window._content.history.go(-1);
  },
  "follow link": function() {
    var element = this.lastElement;
    
    if (element) {
      this.lastElement = null;
      window._content.document.location = element.href;
    }
  },
  "go home": function() {
    window._content.home();
  },
  "page info": function() {
    var msg = "no info";
	
    var titles = window._content.document.getElementsByTagName("title");
    
    if (titles.length) {
      msg = titles.item(0).text;
    }
    
    this.say(msg, false, true);
  },
  toggleSpeak: function() {
    var status = this.getStatus();

    if (status == "none") {
      this.speak();
    } else {
      this.stop();
    }
  },

  togglePause: function() {
    var status = this.getStatus();

    switch (status) {
    case "none":
      break;
    case "speaking":
      this.pause();
      break;
    case "paused":
      this.resume();
      break;
    }
  },
  skip: function(sec) {

      if (!sec) {
	sec = 10;
      }
      this.skipManager.skip(sec);
  },

  handleJavaException: function(e) {
    e.printStackTrace();
    this.say("Error occured:" + e.toString());
  },
  "skip on": function() {
    this.skip(10);
  },
  "skip back": function() {
    this.skip(-10);
  },
  getStatus: function() {
    var status = "none";

    if (this.voice.getCurrent()) {
      status = this.voice.isEnabledSound("SynthManager") ? "speaking" : "paused";
    }

    return status;
  },
  "page status": function() {
    var msg = "";
    var status = this.getStatus();

    switch (status) {
    case "speaking":
    case "paused":
      msg = status;
      break;
    case "none":
      msg = "not speaking";
      break;
    default:
      throw "HDIGH!";
    }

    this.say(msg, false, true);
  },
  onCommand: function(cmd) {
    if (cmd in this) {
      this.say(cmd, true, true);
      this[cmd]();
    } else if (cmd.startsWith("skip")) {
      var l = cmd.split(" ");

      var error = false;

      if (l.length >= 2) {
	var sec = 0;
	var sign = 1;

	const convMap = {
	  "zero": 0,
	  "one": 1,
	  "two": 2,
	  "three": 3,
	  "four": 4,
	  "five": 5,
	  "six": 5,
	  "seven": 7,
	  "eight": 8,
	  "nine": 9
	};

	for (var i = 0; i < l.length; i++) {
	  var w = l[i];

	  if (w == "skip") {
	    continue;
	  } else if (w == "minus") {
	    sign = -1;
	  } else if (w in convMap) {
	    sec *= 10;
	    sec += convMap[w];
	  } else {
	    this.say("what did you say?", true, true);
	    error = true;
	    break;
	  }
	}
      } else {
	error = true;
      }
      
      if (error) {
	this.say("what did you say?", true, true);
      } else {
	sec *= sign;

	this.say("skip " + sec, false, true);
	this.skip(sec);
      }
    } else {
      
      this.say("what did you say?", true, true);
    }      
  },
  onUnload: function() {
    if (this.current) {
      SynthManager.voice.remove(this.current);
    }
  }
};


var SynthManager = new SynthManagerClass();

function buttonToggle(button_id, pref_name, image_arr, text_arr, state, temporary) {
  var enabled = GetBoolPref(pref_name);

  var button = document.getElementById(button_id);

  switch (state) {
  case 0: // neutral: toggle current state
    enabled = !enabled;
    break;
  case -1: // disable
    enabled = false;
    break;
  case 1:
    if (!temporary) {
      enabled = true;
    }
    break;
  }

  if (!temporary) {
    GetPrefs().setBoolPref(pref_name, enabled);
  }
  
  var imagepath = "chrome://ssspeak/skin/";

  var image = enabled ? image_arr[0] : image_arr[1];

  imagepath += image;

  if (text_arr) {
    button.label = enabled ? text_arr[0] : text_arr[1];
  }

  button.image = imagepath;

  return enabled;
}
function menuCheckboxToggle(id, name, init) {
  var state = GetBoolPref(name);

  if (!init) {
    state = !state;

    GetPrefs().setBoolPref(name, state);
  }

  document.getElementById(id).setAttribute("checked", state == true ? "true" : "false");

  if (name == "enableKeyboardEcho") {
    if (state && SynthManager.getStatus() != "speaking") {
      document.addEventListener("keypress", sayKeyOnPress, true);
    } else {
      document.removeEventListener("keypress", sayKeyOnPress, true);
    }
  }
}


function speakTogglee(state) {
  SynthManager.enabled = buttonToggle("ssspeak_toggle", 
				      "enableSpeak", 
				      ["ssspeak_enable.png", "ssspeak_disable.png"],
				      null,
				      state);
}


function keyboardEchoEnabled(enabled) {
  if (enabled && GetBoolPref("enableKeyboardEcho")) {
    document.addEventListener("keypress", sayKeyOnPress, true);
  } else {
    document.removeEventListener("keypress", sayKeyOnPress, true);
  }
}

function keyboardTogglee(state, temporary) {
  var enabled = buttonToggle("keyboard_toggle", 
				      "enableKeyboardEcho", 
				      ["keyboard-on.png", "keyboard-off.png"],
				      null,
			     state, temporary);
  if (enabled) {
    document.addEventListener("keypress", sayKeyOnPress, true);
  } else {
    document.removeEventListener("keypress", sayKeyOnPress, true);
  }
}

var Mp3Dump = {
  _makeOutputFile: function(chooseLocation) {
    var url = window._content.document.location.toString();
  
    var saveName = url.split("/").pop(); // return file part of the url

    var title = window._content.document.title;

    if (title) {
      saveName = title;
    }

    saveName += ".mp3";

    var savePath = null;

    if (chooseLocation) {
      var file = fileBrowse("save", LocalFile(GetStringPref("dumpSavePath")), saveName, [["MP3 Files", "mp3"]]);

      if (file) {
	GetPrefs().setCharPref("dumpSavePath", file.parent.path);
	savePath = file.path;
      }
    } else {
      savePath = GetStringPref("dumpSavePath") + "/" + saveName + ".mp3";
    }
    return savePath;
  },
  dump: function(chooseLocation) {

    var outfile = this._makeOutputFile(chooseLocation);

    if (outfile) {

      /*
       * current HTML document will be used (unless selection exists)
       */
      var html = SynthUtils.prepHtml(getSelectionAsHtmlDoc());
  
      var htmlFile = makeTmpFile("htmlDump", ".html");

      serializeNode(htmlFile, html);

      getVoice().getDumpQueue().dump(pathToUri(htmlFile), outfile);
    }
  }
}

function onProgressChange() {
  alert("ssspeak_progress has changed");
}

function checkMp3DumpAvailable() {
  var sable2mp3check =  buildPath(getExtensionPath("ssspeak", true), ["ext", "scripts", "helpers", "sable2mp3check"]);

  var process = execFile(sable2mp3check, [], true);


  if (process.exitValue) {
    document.getElementById("cmdDump").setAttribute("disabled", "true");

    Components.utils.reportError("ssspeak: 'lame' or 'sox' where not found in system executable search path. MP3 dump functionality disabled"); 
  }
}

function initialiseClient()
{
  document.removeEventListener( "load", initialiseClient, true );

  menuCheckboxToggle("context-speak-auto-speak", "enableSpeak", true);
  menuCheckboxToggle("context-speak-keyboard-echo", "enableKeyboardEcho", true);
  checkMp3DumpAvailable();

  //speakToggle(null);
  // listenToggle(null);
  //keyboardToggle(null);

  SynthManager.init();
  getBrowser().addProgressListener(SynthManager, Components.interfaces.nsIWebProgress.NOTIFY_ALL);
  //getBrowser().addEventListener("unload", SynthManager.onUnload, true);
}

document.addEventListener( "load", initialiseClient, true );

