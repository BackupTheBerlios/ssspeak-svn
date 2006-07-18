#ifndef WIKIDUMPHANDLER_H_
#define WIKIDUMPHANDLER_H_

#include <vector>
#include <string>
#include <libxml/parser.h>

#include "Lockable.h"

class DumpReader;
class WikiZipPageWriter;
class zpwkIProgressListener;

class WikiDumpParser : virtual public Lockable
{
private:
	std::string m_curTitle;
	std::string m_curText;
	std::auto_ptr<WikiZipPageWriter> m_zipWriter;
	std::auto_ptr<DumpReader> m_dumpReader;
	bool m_skipUser;
	bool m_skipImage;
	bool m_skipTalk;
	zpwkIProgressListener *m_progressListener;
	std::string m_dump;
	std::string m_errMsg;
	int m_status;
	bool m_done;
public:
	WikiDumpParser(const std::string &dump, const std::string &zipdir);	
	virtual ~WikiDumpParser();
	
	void parse();
	
	void abort();

	void setProgressListener(zpwkIProgressListener *progressListener) {
		m_progressListener = progressListener;
	}
	
	zpwkIProgressListener *getProgressListener() {
		return m_progressListener;
	}

	void skipUser(bool skip) {m_skipUser = skip;}

	void skipTalk(bool skip) {m_skipTalk = skip;}

	void skipImage(bool skip) {m_skipImage = skip;}

	void setCompression(short level);

	bool done() const {return m_done;}

	int status() const {return m_status;}

	const std::string &errMsg() const {return m_errMsg;}

	float getProgress();
	void error(const std::string &msg);
 private:
	void done(int status, const std::string &msg);
	void parseError();
};

#endif /*WIKIDUMPHANDLER_H_*/
