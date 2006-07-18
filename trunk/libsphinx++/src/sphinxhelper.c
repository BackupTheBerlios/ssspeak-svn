#include <sys/time.h>

#include "sphinxhelper.h"

#include "err.h"
#include "s2types.h"
#include "ad.h"
#include "cont_ad.h"

#define ADBUF_BUFLEN 4096

const char *sphinx_error = "";

struct sphinxData_t {
  ad_rec_t *ad;
  
  cont_ad_t *cont;
  
  int16 adbuf[ADBUF_BUFLEN];

  int32 k, fr, ts, rem;
  char *hyp;
};

/* Sleep for specified msec */
static void sleep_msec (int32 ms)
{
#ifdef WIN32
    Sleep(ms);
#else
    /* ------------------- Unix ------------------ */
    struct timeval tmo;
    
    tmo.tv_sec = 0;
    tmo.tv_usec = ms*1000;
    
    select(0, NULL, NULL, NULL, &tmo);
#endif
}


struct sphinxData_t *sphinxHelper_init(int sample_rate) {
  struct sphinxData_t *data = (struct sphinxData_t *)malloc(sizeof(struct sphinxData_t));
  bzero(data, sizeof(struct sphinxData_t));

  if ((data->ad = ad_open_sps (sample_rate)) == NULL) {
    sphinx_error = "ad_open_sps failed";
    return 0;
  }
  
  /* Initialize continuous listening module */
  if ((data->cont = cont_ad_init (data->ad, ad_read)) == NULL)
    { sphinx_error = "cont_ad_init failed";  return 0;}
  if (ad_start_rec (data->ad) < 0)
    { sphinx_error = "ad_start_rec failed";  return 0;}
  if (cont_ad_calib (data->cont) < 0)
    { sphinx_error = "cont_ad_calib failed";  return 0;}
  
  return data;
}

int sphinxHelper_read1st(struct sphinxData_t *data) {
  data->k = cont_ad_read (data->cont, data->adbuf, ADBUF_BUFLEN);  
  
  if (data->k < 0) {
    sphinx_error = "cont_ad_read failed";
    return -1;
  }
  
  return data->k;
}


char *sphinxHelper_finish(struct sphinxData_t *data, int has_utterance) {
  char *res = 0;

  /*
   * Utterance ended; flush any accumulated, unprocessed A/D data and stop
   * listening until current utterance completely decoded
   */
  ad_stop_rec (data->ad);

  while (ad_read (data->ad, data->adbuf, ADBUF_BUFLEN) >= 0);
  cont_ad_reset (data->cont);
  
  printf ("Stopped listening, please wait...\n"); fflush (stdout);


  if (has_utterance) {
    /* Finish decoding, obtain and print result */
    uttproc_end_utt ();
    if (uttproc_result (&data->fr, &data->hyp, 1) < 0) {
      sphinx_error = "uttproc_result failed";
    } else {
      printf ("%d: %s\n", data->fr, data->hyp); fflush (stdout);
      res = data->hyp;
    }
  }

  cont_ad_close (data->cont);
  ad_close (data->ad);

  free(data);

  return res;
}
  
int sphinxHelper_read(struct sphinxData_t *data) {
  /*
   * Non-zero amount of data received; start recognition of new utterance.
   * NULL argument to uttproc_begin_utt => automatic generation of utterance-id.
   */
  if (uttproc_begin_utt (NULL) < 0) {
    sphinx_error = "uttproc_begin_utt() failed";
    return -1;
  }

  uttproc_rawdata (data->adbuf, data->k, 0);

  //m_recognizer.dispatchEvent(Recognizer::EVENT_LISTEN);

    /* Note timestamp for this first block of data */
  data->ts = data->cont->read_ts;

  /* Decode utterance until end (marked by a "long" silence, >1sec) */
  while (1) {
    /* Read non-silence audio data, if any, from continuous listening module */
    if ((data->k = cont_ad_read (data->cont, data->adbuf, ADBUF_BUFLEN)) < 0)
      { /* sphinx_error = "cont_ad_read failed\n"; */ return;}

    if (data->k == 0) {
      /*
       * No speech data available; check current timestamp with most recent
       * speech to see if more than 1 sec elapsed.  If so, end of utterance.
       */
      if ((data->cont->read_ts - data->ts) > DEFAULT_SAMPLES_PER_SEC / 2) {
        break;
      }
    } else {
      /* New speech data received; note current timestamp */
      data->ts = data->cont->read_ts;
    }

    /*
     * Decode whatever data was read above.  NOTE: Non-blocking mode!!
     * rem = #frames remaining to be decoded upon return from the function.
     */
    data->rem = uttproc_rawdata (data->adbuf, data->k, 0);

    /* If no work to be done, sleep a bit */
    if ((data->rem == 0) && (data->k == 0))
      sleep_msec(20);
  }

  return 0;
}	
void sphinxHelper_sphinxInit(const char *worddic) {
  static int initialized = 0;

  const char *init_argv[] = {
    "$0",
    "-live", "TRUE",
    "-ctloffset", "0",
    "-ctlcount", "100000000",
    //       "-cepdir", "/home/tshalif/.perlbox-voice/commands/ctl",
    //       "-datadir", "/home/tshalif/.perlbox-voice/commands/ctl",
    "-agcemax", "TRUE", 
    "-langwt", "6.5", 
    "-verbose", "0",
    "-fwdflatlw", "8.5", 
    "-rescorelw", "9.5", 
    "-ugwt", "0.5", 
    "-fillpen", "1e-10", 
    "-silpen", "0.005", 
    "-inspen", "0.65", 
    "-top", "1", 
    "-topsenfrm", "3", 
    "-topsenthresh", "-70000", 
    "-beam", "2e-06", 
    "-npbeam", "2e-06", 
    "-lpbeam", "2e-05", 
    "-lponlybeam", "0.0005", 
    "-nwbeam", "0.0005", 
    "-fwdflat", "FALSE", 
    "-fwdflatbeam", "1e-08", 
    "-fwdflatnwbeam", "0.0003", 
    "-bestpath", "TRUE", 
    //       "-kbdumpdir", "/home/tshalif/.perlbox-voice/commands", 
    //       "-lmfn", "/home/tshalif/.perlbox-voice/commands/current.lm", 
    "-dictfn", worddic,
    "-noisedict", "/usr/share/sphinx2/model/hmm/6k/noisedict", 
    "-phnfn", "/usr/share/sphinx2/model/hmm/6k/phone", 
    "-mapfn", "/usr/share/sphinx2/model/hmm/6k/map", 
    "-hmmdir", "/usr/share/sphinx2/model/hmm/6k", 
    "-hmmdirlist", "/usr/share/sphinx2/model/hmm/6k", 
    "-8bsen", "TRUE", 
    "-sendumpfn", "/usr/share/sphinx2/model/hmm/6k/sendump", 
    "-cbdir", "/usr/share/sphinx2/model/hmm/6k"
  };


  if (initialized) {
    return;
  }

  initialized = 1;


  fbs_init (sizeof(init_argv) / sizeof(init_argv)[0], (char **)init_argv);
}

void sphinxHelper_loadLm(const char *lm_path) {
    E_INFO ("%s(%d): Looking for existing model %s\n",
          __FILE__, __LINE__, lm_path);
  
  if (-1 == uttproc_set_lm(lm_path)) {
    E_INFO ("%s(%d): model %s not found, loading..\n",
            __FILE__, __LINE__, lm_path);
    lm_read(lm_path, lm_path, 6.5, 0.5, 0.649999976);
   
    if (-1 == uttproc_set_lm(lm_path)) {
      E_ERROR ("%s(%d): can not set current model %s I have just read with lm_read()!\n",
               __FILE__, __LINE__, lm_path);
    }
  }

  E_INFO ("%s(%d):model %s is set as current\n", __FILE__, __LINE__, lm_path);
}
