#include <cassert>

#include "WikiZipPageWriter.h"

WikiZipPageWriter::WikiZipPageWriter(const std::string &zipdir) : m_zipdir(zipdir), m_compressionLevel(INT_MAX), m_count(0) {
  for (int i = 0; i < CAT_NONE; ++i) {
    m_zipFiles[i] = 0;
  }
  
  m_catNames[CAT_ARTICLE] = "article";
  m_catNames[CAT_IMAGE] = "image";
  m_catNames[CAT_TALK] = "talk";
  m_catNames[CAT_USER] = "user";
}

WikiZipPageWriter::~WikiZipPageWriter()
{
  close();
}

void WikiZipPageWriter::writePage(const std::string &title, const std::string &text, page_categoty_t cat) {
  if (!m_zipFiles[cat]) {
    m_zipFiles[cat] = new zipios::ZipOutputStream(m_zipdir + "/" + m_catNames[cat] + ".zip");
    
    if (m_compressionLevel != INT_MAX) {
      m_zipFiles[cat]->setLevel(m_compressionLevel);
    }
  }

  assert(cat < CAT_NONE && cat >= CAT_ARTICLE);

  zipios::ZipOutputStream *zos = m_zipFiles[cat];

  if (!zos->good()) {
    throw std::logic_error("zip io error");
  }

  zos->putNextEntry(title);
  
  m_count++;
  
  zos->write(text.data(), text.length());
}

void WikiZipPageWriter::close() {
  for (int i = 0; i < CAT_NONE; ++i) {
    if (m_zipFiles[i]) {
      m_zipFiles[i]->close();
      m_zipFiles[i] = 0;
    }
  }
}
