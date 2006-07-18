#ifndef WIKIZIPPAGEWRITER_H_
#define WIKIZIPPAGEWRITER_H_

#include <string>
#include <iostream>
#include <memory>
#include <zipios++/zipoutputstream.h>


class WikiZipPageWriter
{
public:
  typedef enum {
    CAT_ARTICLE,
    CAT_IMAGE,
    CAT_TALK,
    CAT_USER,
    CAT_NONE
  } page_categoty_t;

  WikiZipPageWriter(const std::string &zipdir);
  virtual ~WikiZipPageWriter();
  
  void writePage(const std::string &title, const std::string &text, page_categoty_t cat);
  void close();
  int getCount() {return m_count;}
  void setCompression(int level) {m_compressionLevel = level;}
 private:
  const std::string m_zipdir;
  zipios::ZipOutputStream *m_zipFiles[CAT_NONE];
  const char *m_catNames[CAT_NONE];
  int m_compressionLevel;
/*   std::auto_ptr<zipios::ZipOutputStream> m_zosArticles; */
/*   std::auto_ptr<zipios::ZipOutputStream> m_zosImage; */
/*   std::auto_ptr<zipios::ZipOutputStream> m_zosTalk; */
/*   std::auto_ptr<zipios::ZipOutputStream> m_zosUser; */
  int m_count;
};

#endif /*WIKIZIPPAGEWRITER_H_*/
