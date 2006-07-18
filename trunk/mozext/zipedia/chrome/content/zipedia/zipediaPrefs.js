gPrefsBranchName = "zipedia.";

// default prefs
DEF['dump.path'] = "";
DEF['zip.path'] = "";
DEF['zip.skipTalk'] = false;
DEF['zip.skipUser'] = false;
DEF['zip.skipImage'] = false;
DEF['zip.compression'] = "default";

function openDumpDialog() {
  window.openDialog('chrome://zipedia/content/zipdump.xul', 'Zipedia dump', 'centerscreen,chrome,dialog,modal', document.getElementById("zip.path"));
}

function onPrefAccept() {
  var zippath = document.getElementById("zip.path").value;

  var err;

  var file;

  if (!zippath) {
    err = "you must specify the zipedia directory";
  } else if (!(file = checkExists(zippath))) {
    err = "specified path does not exist";
  } else if (!file.isDirectory()) {
    err = "specified path is not a directory";
  } else if (!checkExists(zippath, ["article.zip"])) {
    err = "specified path does not seem to contain zipedia files";
  }

  if (err) {
    AlertWithTitle("No Zipedia", err, window);
    return false;
  }

  setPrefs();

  return true;
}
