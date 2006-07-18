#include <stdexcept>
#include <iostream>

#include <prthread.h>

#include "DumpReader.h"
#include "WikiZipPageWriter.h"
#include "WikiDumpParser.h"
#include "MonitorBlock.h"

namespace {
  void myThreadStart(void *arg) {
    WikiDumpParser *parser = static_cast<WikiDumpParser*>(arg);

    parser->parse();
  }
}

static void usage(const char *argv0) {
	std::cerr << "usage: " << argv0 << " <wikidump> <zipoutdir>" << std::endl;
	exit(1);
}

int main(int argc, const char **argv) {	
	
	if (argc != 3) usage(argv[0]);
	
	try {
	  WikiDumpParser parser(argv[1], argv[2]);
	  PRThread *runner = 0;
	  {
	    MonitorBlock sync(&parser);
	    runner = PR_CreateThread(PR_USER_THREAD, myThreadStart, &parser, PR_PRIORITY_NORMAL, PR_LOCAL_THREAD, PR_JOINABLE_THREAD, 0);

	    sync.wait();
	  }
	  size_t ticksPerSecond = PR_TicksPerSecond();

	  float progress;

	  do {
	    progress = parser.getProgress();
	    std::cout << progress << std::endl;
	    PR_Sleep(1 * ticksPerSecond);
	  } while (progress > 0 && progress < 1);

	  PR_JoinThread(runner);

	  if (progress == -1) {
	    std::cerr << parser.errMsg() << std::endl;

	    return 1;
	  }
	  
	  //parser.parse();
	
	} catch (std::exception &e) {
		std::cerr << "error: " << e.what() << std::endl;
	} catch (...) {
		std::cerr << "error: ..." << std::endl;
	}		

	return 0;
}
