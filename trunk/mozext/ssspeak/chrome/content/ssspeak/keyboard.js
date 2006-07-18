const CHARCODE = ['none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'space', 'exclamation mark', 'double quote', 'hash mark', 'dollar', 'percent', 'ampersand', "quote", 'left round bracket', 'right round bracket', 'asterisk', '+', 'comma', 'hyphen', 'period', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'column', 'semicolumn', 'less than', '=', 'greater than', 'question mark', 'at mark', 'eigh', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'left square bracket', '\\', 'right square bracket', '^', 'underscore', '`', 'eigh', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'left curly bracket', 'vertical bar', 'right curly bracket', 'tilde', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none', 'none'];

const nsIDOMKeyEvent = Components.interfaces.nsIDOMKeyEvent;

const KEYCODE = [];

KEYCODE[nsIDOMKeyEvent.DOM_VK_CANCEL] = "CANCEL";
KEYCODE[nsIDOMKeyEvent.DOM_VK_HELP] = "HELP";
KEYCODE[nsIDOMKeyEvent.DOM_VK_BACK_SPACE] = "BACK SPACE";
KEYCODE[nsIDOMKeyEvent.DOM_VK_TAB] = "TAB";
KEYCODE[nsIDOMKeyEvent.DOM_VK_CLEAR] = "CLEAR";
KEYCODE[nsIDOMKeyEvent.DOM_VK_RETURN] = "RETURN";
KEYCODE[nsIDOMKeyEvent.DOM_VK_ENTER] = "ENTER";
KEYCODE[nsIDOMKeyEvent.DOM_VK_SHIFT] = "SHIFT";
KEYCODE[nsIDOMKeyEvent.DOM_VK_CONTROL] = "CONTROL";
KEYCODE[nsIDOMKeyEvent.DOM_VK_ALT] = "ALT";
KEYCODE[nsIDOMKeyEvent.DOM_VK_PAUSE] = "PAUSE";
KEYCODE[nsIDOMKeyEvent.DOM_VK_CAPS_LOCK] = "CAPS LOCK";
KEYCODE[nsIDOMKeyEvent.DOM_VK_ESCAPE] = "ESCAPE";
KEYCODE[nsIDOMKeyEvent.DOM_VK_SPACE] = "SPACE";
KEYCODE[nsIDOMKeyEvent.DOM_VK_PAGE_UP] = "PAGE UP";
KEYCODE[nsIDOMKeyEvent.DOM_VK_PAGE_DOWN] = "PAGE DOWN";
KEYCODE[nsIDOMKeyEvent.DOM_VK_END] = "END";
KEYCODE[nsIDOMKeyEvent.DOM_VK_HOME] = "HOME";
KEYCODE[nsIDOMKeyEvent.DOM_VK_LEFT] = "LEFT";
KEYCODE[nsIDOMKeyEvent.DOM_VK_UP] = "UP";
KEYCODE[nsIDOMKeyEvent.DOM_VK_RIGHT] = "RIGHT";
KEYCODE[nsIDOMKeyEvent.DOM_VK_DOWN] = "DOWN";
KEYCODE[nsIDOMKeyEvent.DOM_VK_PRINTSCREEN] = "PRINTSCREEN";
KEYCODE[nsIDOMKeyEvent.DOM_VK_INSERT] = "INSERT";
KEYCODE[nsIDOMKeyEvent.DOM_VK_DELETE] = "DELETE";
KEYCODE[nsIDOMKeyEvent.DOM_VK_MULTIPLY] = "MULTIPLY";
KEYCODE[nsIDOMKeyEvent.DOM_VK_ADD] = "ADD";
KEYCODE[nsIDOMKeyEvent.DOM_VK_SEPARATOR] = "SEPARATOR";
KEYCODE[nsIDOMKeyEvent.DOM_VK_SUBTRACT] = "SUBTRACT";
KEYCODE[nsIDOMKeyEvent.DOM_VK_DECIMAL] = "DECIMAL";
KEYCODE[nsIDOMKeyEvent.DOM_VK_DIVIDE] = "DIVIDE";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F1] = "F1";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F2] = "F2";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F3] = "F3";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F4] = "F4";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F5] = "F5";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F6] = "F6";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F7] = "F7";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F8] = "F8";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F9] = "F9";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F10] = "F10";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F11] = "F11";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F12] = "F12";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F13] = "F13";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F14] = "F14";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F15] = "F15";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F16] = "F16";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F17] = "F17";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F18] = "F18";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F19] = "F19";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F20] = "F20";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F21] = "F21";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F22] = "F22";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F23] = "F23";
KEYCODE[nsIDOMKeyEvent.DOM_VK_F24] = "F24";
KEYCODE[nsIDOMKeyEvent.DOM_VK_NUM_LOCK] = "NUM LOCK";
KEYCODE[nsIDOMKeyEvent.DOM_VK_SCROLL_LOCK] = "SCROLL LOCK";
KEYCODE[nsIDOMKeyEvent.DOM_VK_COMMA] = "COMMA";
KEYCODE[nsIDOMKeyEvent.DOM_VK_PERIOD] = "PERIOD";
KEYCODE[nsIDOMKeyEvent.DOM_VK_SLASH] = "SLASH";
KEYCODE[nsIDOMKeyEvent.DOM_VK_BACK_QUOTE] = "BACK QUOTE";
KEYCODE[nsIDOMKeyEvent.DOM_VK_OPEN_BRACKET] = "OPEN BRACKET";
KEYCODE[nsIDOMKeyEvent.DOM_VK_BACK_SLASH] = "BACK SLASH";
KEYCODE[nsIDOMKeyEvent.DOM_VK_CLOSE_BRACKET] = "CLOSE BRACKET";
KEYCODE[nsIDOMKeyEvent.DOM_VK_QUOTE] = "QUOTE";


function Keyboard() {}

Keyboard.prototype = {
  nextKeyEchoJob: null,
  voice: ssspeak.voice.wrappedJSObject,

  checkIgnoreKey: function(event) {
    var ignoreKeys = [];
    var keyPause = document.getElementById("keyPauseToggle");
    var keySpeak = document.getElementById("keySpeakToggle");

    ignoreKeys.push(keyPause);
    ignoreKeys.push(keySpeak);

    for (var i = 0; i < ignoreKeys.length; ++i) {
      if (ignoreKeys[i].getAttribute("key").toLowerCase() == CHARCODE[event.charCode].toLowerCase()) {
	var modifyers = ignoreKeys[i].getAttribute("modifiers").split(",");

	for (var j in modifyers) {
	  var n = modifyers[j];

	  switch (n) {
	  case "accel":
	    if (!event.ctrlKey) return false;
	    break;
	  case "shift":
	    if (!event.shiftKey) return false;
	    break;
	  case "alt":
	    if (!event.altKey) return false;
	    break;
	  default:
	    throw "HDIGH!";
	  }
	}
	return true;
      }
    }

    return false;
  },
  sayKey: function(event) {
    if (null != this.nextKeyEchoJob) {
      clearTimeout(this.nextKeyEchoJob);
    }

    if (!this.checkIgnoreKey(event)) {
      var s = "";

      if (event.ctrlKey) {
	s += " control";
      }

      if (event.altKey) {
	s += " alt";
      }

      if (event.shiftKey) {
	s += " shift";
      }

      if (event.charCode) {
	s += " " + CHARCODE[event.charCode];
      }

      if (event.keyCode) {
	s += " " + KEYCODE[event.keyCode];
      }
    
      this.nextKeyEchoJob = setTimeout("keyboard.voice.say('" + s + "', true).play()", 300);
    }
  }
}

var keyboard = new Keyboard();


function sayKeyOnPress(event) {
  keyboard.sayKey(event);
}

