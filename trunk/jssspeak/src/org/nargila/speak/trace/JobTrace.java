package org.nargila.speak.trace;

import java.io.File;

aspect JobTrace extends AbstractTrace {
    /**
     * The application classes
     */
    //pointcut classes(): within(jssspeak.util.AsyncJob+) ;
    pointcut classes():  within(jssspeak.player.WavPlayer+)  || within(jssspeak.ssmlsynth.*)|| within(jssspeak.rmiserver.*) || within(jssspeak.festisab.*) ;
    /**
     * The constructors in those classes - but only the ones with 3
     * arguments.
     */
    pointcut constructors(): execution(new(..));

        pointcut ignore(): execution(* *.write*(..)) || execution(* *.add(..)) || execution(* *.schedulePlayEvent(..)) || execution(* *.getCurrentDataPosMills(..)) || execution(* *.nowPlaying(..));
            pointcut play(File wav): execution(* *.play(File)) && args(wav);

//                 void around(File wav): play(wav) {
//                     System.out.println("play(" + wav + ")");
//                     proceed(wav);
//                 }
                    //pointcut methods(): execution(* *.doJob(..))|| execution(* *.finish(..));
                    
                    //pointcut methods(): execution(* *.wait(..)) || execution(* *.doJob(..))|| execution(* *.finish(..));
                    pointcut methods(): execution(* *.finish(..));

                    //pointcut methods(): execution(* *.checkState(..)) || execution(* *.finish(..)) || execution(* *.pause(..)) || execution(* *.resume(..)) || execution (* *.schedulePlayEvent(..)) || execution(* *.abort(..))   ||  execution(* *.play(..))  || execution(* *.write(..))   || execution(* *.run(..))  && !ignore();

}