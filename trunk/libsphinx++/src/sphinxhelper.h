#ifndef SPHINXHELPER_H
#define SPHINXHELPER_H

#ifdef __cplusplus
extern "C" {
#endif

  struct sphinxData_t;

  extern const char *sphinx_error;

  struct sphinxData_t *sphinxHelper_init(int sample_rate);
  int sphinxHelper_read1st(struct sphinxData_t *data);
  char *sphinxHelper_finish(struct sphinxData_t *data, int has_utterance);
  int sphinxHelper_read1st(struct sphinxData_t *data);
  int sphinxHelper_read(struct sphinxData_t *data);
  void sphinxHelper_loadLm(const char *lm_path);
  void sphinxHelper_sphinxInit(const char *worddic);
#ifdef __cplusplus
}
#endif

#endif // SPHINXHELPER_H
