/*
 * Andrew Blejde
 * Fall 2015
 * 
 * JSoup Documentation: http://jsoup.org/apidocs/
 * 
 * Web Crawler to fetch PDF documents within given
 * parameters. Use at your own risk.
*/

// java imports
import java.util.*;
import java.util.ArrayList;
import java.io.*; 
import java.net.*; 

// JSoup libraries
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler
{
	// Class variables for keywords, domain, and host
	private String root;
	private String host;
	private String key1;
	private String key2;

	// Array list for URLs
	private static ArrayList<String> urlList = new ArrayList<String>();
	private static Stack<String> stack = new Stack<String>();

	// HashSet for visited URLs
	private static Set<String> visited = new HashSet<String>();

	// Count for upper limit, current pointer, and documents found
	private int found;
	private int current;
	private int total;
	private int limit;
	private boolean hasLimit;

	public WebCrawler(String domain, String key1, String key2, int limit) throws IOException
	{	
		// Fix domain if necessary
		if(!domain.contains("http"))
			domain = "http://".concat(domain);

		// Assign details
		this.found = 0;

		// Get host
		URL u = new URL(domain);

		// Store domain and keyword info
		this.root = domain;
		this.host = u.getHost();
		this.key1 = key1.toLowerCase();
		this.key2 = key2.toLowerCase();
		this.current = 0;
		this.total = 0;

		// Check if limit given
		if(limit < 0)
			this.hasLimit = false;
		else
			this.hasLimit = true;

		// Assign limit anyway
		this.limit = limit;

		// Add initial url to the list
		stack.push(domain);

		// Begin crawl
		crawl();
	}
	public void getURLs(Document document) throws IOException
	{
		Elements page;
		
		page = document.select("a");
		
		for(Element e : page)
		{
			String link = e.attr("abs:href");
			link = link.trim();
			String lower = link.toLowerCase();

			// Check HashMap for duplicates
			if(visited.contains(link))
			{
				// Link already in HashMap continue
				continue;	
			}
			else
			{
				// Weed out garbage URLs and stick to the current host
				// http://purdue.edu => purdue.edu
				// http://math.purdue.edu => math.purdue.edu
				// Calendar is Purdue specific, no idea why the math website is messed up.
				if(!lower.contains(this.host) || link.contains("?") || link.contains("#") || lower.contains("mailto") || lower.contains(".php"))
				{
					// Not interested in this link, add it to the HashMap and continue
					visited.add(link);
					this.total++;
					
					continue;
				}
				else if((lower.contains(this.key1) || lower.contains(this.key2)) && lower.contains(".pdf"))
				{
					FileWriter fw = new FileWriter("papers.txt", true);
					fw.write(link);
					fw.write("\r\b");

					// Close writer
					fw.close();

					// Increment found
					found++;
				}
				else
				{
					// Create URL
					URL u = new URL(link);

					// Open connection
					HttpURLConnection connection = null;
					String contentType;
					try
					{
						connection = (HttpURLConnection) u.openConnection();
						connection.setRequestMethod("HEAD");
						connection.connect();

						// Get content type
						contentType = connection.getContentType();
					}
					catch(Exception ex)
					{
						// Probably timed out. Mark visited and continue.
						visited.add(link);
						continue;
					}

					// Push url for parsing
					if(contentType == null || !contentType.toLowerCase().contains("html") 
						|| hasLimit && this.current > this.limit)
						continue;
					else
					{
						// Push and increment
						stack.push(link);
						this.total++;
					}

					// Mark it as visited in HashMap
					visited.add(link);
				}
			}
		}
	}
	public Document getDoc(String url) throws IOException
	{
		// Get document from url source using InputStream
		Document document;
		try
		{
			// Get document contents
			InputStream in = new URL(url).openStream();
			document = Jsoup.parse(in, "ISO-8859-1", url);
		}
		catch(Exception e)
		{
			// Check for null document
			document = null;
			return null;
		}
	
		return document;
	}
	public void crawl() throws IOException
	{
		// Crawl while less than the requested url total
		while(true)
		{
			// Check if limit has been reached and queue is empty
			if((hasLimit && this.current > this.limit) || stack.empty())
			{
				// Reached given limit, return
				return;
			}
			// Get URL
			String url = stack.pop();

			// Increment current
			this.current++;

			// Print URL being parsed
			System.out.println("Parsing ( " + this.current + " / " + this.total + " ) : " + url);

			// Get document for parsing
			Document document = getDoc(url);

			// If the document is null, throw it away and continue;
			if(document == null)
			{
				// Continue execution
				continue;
			}

			// Pass to getURLs method to extract links from document
			getURLs(document);
		}
	}
	public int getFound()
	{
		return this.found;
	}
	public int getTotal()
	{
		return this.total;
	}
	public static void main(String[] args) throws IOException
	{
		// If arguments given is unsatisfactory, print usage.
		if(args == null || args.length < 3 || args.length > 4)
		{
			// Issue with the provided input..
			System.out.println("Input format error.\n");

			// Print usage + example
			System.out.println("\nUsage: ./build.sh [domain] [keyword] [keyword] [limit]");
			System.out.println("\nDomain: web address you want to to crawl.");
			System.out.println("Keyword: a keyword that identifies your class.");
			System.out.println("Keyword: another keyword that identifies your class.");
			System.out.println("Limit: (optional) maximum number of URLs you want to crawl.");
			System.out.println("Note: If no limit is given, it will crawl all URLs it can reach #BFS.");
			System.out.println("\nE.g.: ./build.sh http://math.purdue.edu Math265 MA265 10000\n");
			System.exit(0);
		}

		// Create WebCrawler object
		WebCrawler crawler = null;

		// Create web crawler. If no limit provided, set to -1
		
		// Parse limit
		int limit = 0;
		
		try
		{
			limit = Integer.parseInt(args[3]);
		}
		catch(Exception e)
		{
			limit = -1;
		}

		// Call Constructor
		crawler = new WebCrawler(args[0], args[1], args[2], limit);	

		// Print number of documents found
		System.out.println("URLs found: " + crawler.getTotal() + ".\nDocuments found: " + crawler.getFound());
	}
}