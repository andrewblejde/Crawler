Web Crawler for PDF Files

Build and run the program with the build.sh script.

Usage:

./build.sh [website] [keyword] [keyword] [limit]

website: the website you want to crawl for PDF files.
keyword: a keyword to look for when looking at files / links
*limit: the number of websites you want to crawl

*optional. Not providing a limit will crawl all pages, which might take some time.

e.g. 
./build.sh http://math.purdue.edu MA265 Math265 100

