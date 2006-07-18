package org.nargila.speak.impl.synth.festival;

import  javax.sound.sampled.AudioFormat;

/**
 * Raw audio format as produced by festival
 * @author tshalif
 *
 */
class RawAudioFormat extends AudioFormat {
    RawAudioFormat() {
        super(16000, 16,1, true, false);
    }
}

