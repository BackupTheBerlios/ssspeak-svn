var gAborted = false;
var gDumped = null;
var gDump2Zip = null;

function initWizard() {
  initPrefWin();
}

var gProgress = null;

function zipdump() {
  setPrefs();

  var wizard = document.getElementById("zipdumpWizard");
  wizard.canRewind = false;
  wizard.canAdvance = false;

  var dump = document.getElementById("dump.path").value;
  var outpath = document.getElementById("zip.path").value;

  makeDir(outpath);
    
  var zpwkIWikiDumpToZip = Components.interfaces.zpwkIWikiDumpToZip;

  gDump2Zip = Components.classes["@nargila.org/wiki-dump-tozip;1"].
    createInstance(zpwkIWikiDumpToZip);

  { // set dump properties
    var skipTalk = document.getElementById("zip.skipTalk");
    var skipUser = document.getElementById("zip.skipUser");
    var skipImage = document.getElementById("zip.skipImage");
    var compressionLevel = document.getElementById("zip.compression");

    if (skipTalk.value) {
      gDump2Zip.skipFlags |= zpwkIWikiDumpToZip.SKIP_TALK;
    }

    if (skipUser.value) {
      gDump2Zip.skipFlags |= zpwkIWikiDumpToZip.SKIP_USER;
    }

    if (skipImage.value) {
      gDump2Zip.skipFlags |= zpwkIWikiDumpToZip.SKIP_IMAGE;
    }

    
    var compression;

    switch (compressionLevel.value) {
    case "best":
      compression = gDump2Zip.BEST_COMPRESSION;
      break;
    case "speed":
      compression = gDump2Zip.BEST_SPEED;
      break;
    case "undefined":
    default:
      compression = gDump2Zip.DEFAULT_COMPRESSION;
      break;
    }

    gDump2Zip.compression = compression;
  }

  gProgress = {
    startTime: Date.now(),
    done: function() {
      wizard.canAdvance = true;
      wizard.canRewind = true;
      var percent = 100;
      document.getElementById("dumpProgress").value = percent;
      document.getElementById("progressTxt").value = "" + percent + "%";
      
      document.getElementById("ETA").innerHTML = "Done.";
    },
    timeConv: function(eta) {
      const sec_in_day = 3600 * 24;
      const sec_in_hour = 3600;
      const sec_in_minute = 60;

      var days = parseInt(eta / sec_in_day);

      eta -= days * sec_in_day;

      var hours = parseInt(eta / sec_in_hour);

      eta -= hours * sec_in_hour;

      var minutes = parseInt(eta / sec_in_minute);

      eta -= minutes * sec_in_minute;

      var sec = eta;

      var res = sec;

      if (minutes) {
	res = minutes + ":" + res;
      }

      if (hours) {
	res = hours + ":" + res;
      }
      
      if (days) {
	res = days + ":" + res;
      }

      return res;
    },
    error: function(msg) {
      wizard.canRewind = true;
      gAborted = true;
      
      document.getElementById("ETA").innerHTML = "Error: " + msg;
    },
    progress: function() {
      var eta;

      var fraction = gDump2Zip.progress;

      if (fraction == -1) {
	this.error(gDump2Zip.err);
      } else if (fraction == 1.0) {
	this.done();
      } else {
	if (fraction > 0) {
	  var timeLapse = Date.now() - this.startTime;
	  
	  var remainTime = parseInt(((timeLapse / fraction) - timeLapse) / 1000) + 1;
	  
	  eta = "Time remaining: " + this.timeConv(remainTime);
	  
	  var percent = parseInt(fraction * 100);
	  document.getElementById("dumpProgress").value = percent;
	  document.getElementById("progressTxt").value = "" + percent + "%";
	  
	  document.getElementById("ETA").innerHTML = eta;
	}
	
	setTimeout("gProgress.progress()", 500);
      }
    }
  };

  try {
    gDump2Zip.wiki2zip(dump, outpath);
    gProgress.progress();
  } catch (e) {
    gProgress.error(gDump2Zip.err);
  }
}

function abort() {
  if (!gAborted) {
    gAborted = true;

    if (null != gDump2Zip) {
      gDump2Zip.abort();
    }
  }
}

function onCancel() {
  abort();

  return true;
}

function onFinish() {
  var dumpPath = window.arguments[0];

  dumpPath.value = document.getElementById("zip.path").value;
}

function onDumpPathsPageAdvanced() {
  var dumpPath = document.getElementById("dump.path").value;
  var zipPath = document.getElementById("zip.path").value;

  var err = null;

  do {
    if (!dumpPath || !zipPath) {
      err = "you must specify paths";
      break;
    }

    if (!checkReadable(dumpPath)) {
      err = dumpPath + ": file does not exist or is not readable";
      break;
    }
    
    if (checkExists(zipPath, ["article.zip"])) {
      if (!ConfirmWithTitle("Files Exist", zipPath + ": directory seems to already contain previous dump - overwrite?", "Yes", "No")) {
	return false;
      }
    }

    if (!checkWritable(zipPath)) {
      err = zipPath + ": parent directory does not exist or not writable";
      break;
    }
  } while (false);
    

  if (err) {
    alert(err);
  }

  return !err;
}
