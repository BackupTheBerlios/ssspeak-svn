#include <prthread.h>

#include <libxml/parser.h>

#include <libxml/xmlreader.h>

#include <algorithm>
#include <cstdio>

#include "WikiDumpParser.h"
#include "DumpReader.h"
#include "WikiZipPageWriter.h"
#include "MonitorBlock.h"

 int zpwk_xmlInputReadCallback(void * context, char * buffer, int len) {
 	DumpReader *reader = static_cast<DumpReader*>(context);
 	
 	return reader->read(buffer, len);
 }
 
 int zpwk_xmlInputCloseCallback(void * context) {
  	DumpReader *reader = static_cast<DumpReader*>(context);
 	
 	reader->close();
 	
 	return 0;
 }
	
WikiDumpParser::WikiDumpParser(const std::string &dump, const std::string &zipdir) 
  : m_zipWriter(new WikiZipPageWriter(zipdir)), m_dumpReader(new DumpReader(dump)), 
    m_skipUser(false), m_skipImage(false), m_skipTalk(false), m_progressListener(0), m_dump(dump), m_status(-1), m_done(false)
{
}

WikiDumpParser::~WikiDumpParser()
{
}

float WikiDumpParser::getProgress() {
  if (!done()) {
    return std::min(m_dumpReader->getProgress(), (float)0.99); // never report 100% if still running
  } else if (m_status) {
    return -1;
  } else {
    return 1;
  }
}

void WikiDumpParser::error(const std::string &msg) {
  throw std::logic_error(msg);
}

void WikiDumpParser::parseError() {
  error("parse error");
}


void WikiDumpParser::done(int status, const std::string &msg) {
  MonitorBlock sync(this);

  m_status = status;
  m_errMsg = msg;
  m_done = true;

  sync.notifyAll();
}


void WikiDumpParser::abort() {

  if (m_dumpReader.get()) {
    m_dumpReader->close();
  }

  {
    MonitorBlock sync(this);

    while (!done()) {
      sync.wait();
    }
  }
}

void WikiDumpParser::parse() {

  xmlTextReaderPtr reader = 0;
  int ret;

  m_status = -1;
  m_errMsg = "";
  m_done = false;
  
  {
    MonitorBlock sync(this);

    sync.notifyAll();
  }
	
  std::string msg;
  int status = -1;

  try {
    /*
     * build an xmlReader for that file
     */
    reader = xmlReaderForIO(zpwk_xmlInputReadCallback, zpwk_xmlInputCloseCallback, m_dumpReader.get(), std::string("file://" + m_dump).c_str(), "utf8", 0);

    if (!reader) {
      error("WikiDumpParser::parse(): can't create xmlReaderForIO");
    }
    std::string title, text;

    WikiZipPageWriter::page_categoty_t page_cat = WikiZipPageWriter::CAT_ARTICLE;
    
    bool skip = false;
    
    for (ret = xmlTextReaderRead(reader); ret == 1; ret = xmlTextReaderRead(reader)) {
      std::string sname = (const char*)xmlTextReaderConstName(reader);
      
      std::string data;
      
      if (sname == "title" || sname == "text") {
	xmlNodePtr node = xmlTextReaderExpand(reader);
	
	
	if (!node) {
	  parseError();
	}

	xmlNodePtr textNode = node->children;
		   		
	if (textNode) {
	  
	  if (!textNode->content) {
	    parseError();
	  }
	  
	  data = (const char*)textNode->content;
	  
	  
	  if (sname == "title") {
	    title = data;
	    page_cat = WikiZipPageWriter::CAT_ARTICLE;
	    size_t idx;
	    
	    if (title.find("Image:") == 0) {
	      page_cat = WikiZipPageWriter::CAT_IMAGE;
	    } else if (title.find("Special:") == 0) {
	      if (m_skipImage) {
		skip = true;
		continue;
	      }
	      // prefix = "s/";
	    } else if ((idx = title.find("Talk:")) != title.npos ||
		       (idx = title.find("talk:")) != title.npos) {
	      if (m_skipTalk) {
		skip = true;
		continue;
	      }		   							   						
	      page_cat = WikiZipPageWriter::CAT_TALK;
	    } else if ((idx = title.find("User:")) == 0) {
	      if (m_skipUser) {
		skip = true;
		continue;
	      }		   							   						
	      page_cat = WikiZipPageWriter::CAT_USER;
	    }		   						   				
	  } else {
	    if (!skip) {
	      text = data;
	      m_zipWriter->writePage(title, text, page_cat);
	    }
	    skip = false;
	  }
	}
      }
      PR_Sleep(PR_INTERVAL_NO_WAIT);
    }
    status = 0;
  } catch (std::exception &ex) {
    msg = ex.what();
  } catch (...) {
    msg = "WikiDumpParser::parse(): unknown exception";
  }
  
  m_zipWriter->close();
  m_dumpReader->close();
     
  if (reader) xmlFreeTextReader(reader);
     
  xmlCleanupParser();

  done(status, msg);
}




void WikiDumpParser::setCompression(short level) {
  m_zipWriter->setCompression(level);
}

