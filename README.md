This is a lab to quickly get started with Elastic Search and Google Docs. 

Many examples have been taken from the Elastic Search website:

<http://www.elasticsearch.org/>

Get started
-----------

To get Elastic Search up and running:

	git clone https://github.com/jankronquist/elastic-search-lab.git
	mvn lab:init
	mvn package
	mvn exec:java -Dexec.mainClass="com.jayway.search.RunElasticSearch"

You have now started Elastic Search in an embedded Java process! 

Import the code into your favorite IDE. You can of course run the class RunElasticSearch from here instead of from command line. 
Since Elastic Search provide automatic clustering functionality make sure to only run a single Elastic Search process at a time! 

**Do not install and run Elastic Search as the default settings will create a cluster across the local network!**  

Notice that each stes require that you use `mvn lab:next` to initiate the step! Solutions are available in the file ending in *Solution.java. Hints are sometimes available in HINTS.md. This means now is the time to run:

	mvn lab:next
	
Then you can begin step 1 below. In the maven output you can see which is the current step.

Step 1 - Hello Elastic Search
-----------------------------

Lets try some of the examples from the Elastic Search home page.

Inserting data:

	$ curl -XPUT http://localhost:9200/twitter/user/kimchy -d '{
    	"name" : "Shay Banon"
	}'
	
	$ curl -XPUT http://localhost:9200/twitter/tweet/1 -d '{
    	"user": "kimchy",
    	"post_date": "2009-11-15T13:12:00",
    	"message": "Trying out elasticsearch, so far so good?"
	}'
	
	$ curl -XPUT http://localhost:9200/twitter/tweet/2 -d '{
    	"user": "kimchy",
    	"post_date": "2009-11-15T14:12:12",
    	"message": "You know, for Search"
	}'

Getting data:

	$ curl -XGET http://localhost:9200/twitter/tweet/2

Searching:

	$ curl -XGET http://localhost:9200/twitter/tweet/_search?q=user:kimchy
	
	$ curl -XGET http://localhost:9200/twitter/tweet/_search -d '{
	    "query" : {
	        "term" : { "user": "kimchy" }
	    }
	}'
	
	$ curl -XGET http://localhost:9200/twitter/_search?pretty=true -d '{
	    "query" : {
	        "range" : {
	            "post_date" : {
	                "from" : "2009-11-15T13:00:00",
	                "to" : "2009-11-15T14:30:00"
	            }
	        }
	    }
	}'

Excellent! Try to create insert some of your own data, maybe your colleagues or projects you have worked on.

Step 2 - Inserting documents
----------------------------

This project and RunElasticSearch already has the mapper-attachments plugin installed. Lets try to add and search some documents! Simply follow the instructions here:

<http://www.elasticsearch.org/tutorials/2011/07/18/attachment-type-in-action.html>

Step 3 - Java API
-----------------

Shut down the RunElasticSearch process. Notice the folder `data`. All documents we have inserted will be available when we start a new Elastic Search node.

Open `ElasticSearchLab.java`.

Lets get some data:

	GetResponse gr = client.prepareGet("twitter", "tweet", "1").execute().actionGet();
	System.out.println(gr);

Investigate the GetResponse class. 

* How can you get the value of a field? 
* How can you print the original JSON document?

Documentation: <http://www.elasticsearch.org/guide/reference/java-api/get.html>

Lets add data:

	import static org.elasticsearch.common.xcontent.XContentFactory.*;
	
	IndexResponse ir = client.prepareIndex("twitter", "tweet", "3")
        .setSource(jsonBuilder()
                    .startObject()
                        .field("user", "kimchy")
                        .field("postDate", new Date())
                        .field("message", "trying out Java API")
                    .endObject()
                  )
        .execute()
        .actionGet();

Verify that the data was added!

Finally lets query the data:

	import static org.elasticsearch.index.query.FilterBuilders.*;
	import static org.elasticsearch.index.query.QueryBuilders.*;
	
	SearchResponse sr = client.prepareSearch("test")
        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
        .setQuery(termQuery("multi", "test"))
        .setFrom(0).setSize(60).setExplain(true)
        .execute()
        .actionGet();
	
	for (SearchHit hit : sr.getHits()) {
		System.out.println(hit.getId() + ": " + hit.getSource());
	}

Exercises:

* Add your own data
* Test a fuzzy query (QueryBuilders.fuzzyQuery)
* Test querying between two dates
* Test boolean queries (eg two conditions)

Step 4 - Documents in Java
--------------------------

To add a file you need to Base64 encode it first. Elastic Search has a nice utility class for this:

	import org.elasticsearch.common.Base64;
	

All you need to do is build an object that contains:

	{ "file": "<base64 encoded data>"}

After you have added the document make sure you can query for the object contents.

Step 5 - Result highlighting
----------------------------

To get search result where you can see what has matched the query you can use highlighting. Simply add `.addHighlightedField("<field>")` and then for each SearchHit you get highlighted results:
	
	for (HighlightField field : hit.getHighlightFields().values()) {
		System.out.println(" " + Arrays.asList(field.fragments()));
	}

Step 6 - Google Docs Intro
--------------------------

All Google API:s are based on OAuth. According to <http://en.wikipedia.org/wiki/OAuth>:

> OAuth (Open Authorization) is an open standard for authorization. It allows users to share their private resources (e.g. photos, videos, contact lists) stored on one site with another site without having to hand out their credentials, typically username and password.
> 
> OAuth allows users to hand out tokens instead of credentials to their data hosted by a given service provider. Each token grants access to a specific site (e.g., a video editing site) for specific resources (e.g., just videos from a specific album) and for a defined duration (e.g., the next 2 hours). This allows a user to grant a third party site access to their information stored with another service provider, without sharing their access permissions or the full extent of their data.

In this lab we will access YOUR personal Google Docs. To do this you must first create an Access Token that can be used to access your account. Usually this is done with a website that has been registered with Google, but to simplify things this lab contains a utility to create an access token from command line. The access token will be restricted to Google Docs.

Simply run and follow the instructions:

	mvn compile exec:java -Dexec.mainClass="com.jayway.gdocs.GetAccessToken"

The access token have been stored in a file called `accessToken.properties`. (Feel free to study the program GetAccessToken find out what is going on.)

All Google API:s are based on Google Data Protocol which is a REST-inspired technology for reading, writing, and modifying information on the web. For detals refer to <http://code.google.com/apis/gdata/>.

The documentation for Google Docs API is available here: 

* REST API <http://code.google.com/apis/documents/docs/3.0/developers_guide_protocol.html>
* Java API <http://code.google.com/apis/documents/docs/2.0/developers_guide_java.html>


Step 7 - List documents
-----------------------

Open the file `GoogleDocsLab.java`.

First of all we must create the OAuth parameters based on the AccessToken we saved earlier:

	GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
	oauthParameters.setOAuthConsumerKey(CONSUMER_KEY);
	oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
	AccessToken.load("accessToken.properties").prepare(oauthParameters);

Then we create a client for the Google Docs API:

	DocsService client = new DocsService("lab-client");
	client.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());

Then we get a list of all documents:

	URL feedUri = new URL("https://docs.google.com/feeds/default/private/full/");
	DocumentListFeed feed = client.getFeed(feedUri, DocumentListFeed.class);
	
	for (DocumentListEntry entry : feed.getEntries()) {
		// TODO: print document
	}

Exercise: Print document title, id, link, author and the person that modified it last.

Step 8 - Download document
--------------------------

Downloading a document is a bit weird and the reason is that the OAuth token must also be included when downloading.

	// get download URI
	String uri = ((com.google.gdata.data.MediaContent)entry.getContent()).getUri();

	// download
	MediaContent mc = new MediaContent();
	mc.setUri(url + "&exportFormat=" + exportFormat);
	MediaSource ms = client.getMedia(mc);
	IOUtils.copy(ms.getInputStream(), new FileOutputStream(fileName));

`exportFormat` have to be set to doc, pdf, xls etc. See here for details: <http://code.google.com/apis/documents/docs/3.0/reference.html#ExportParameters>

Exercise: Use this to download all your documents

Step 9 - Integration time
-------------------------

Instead of just downloading the documents add the to an Elastic Search index. Verify that you can query for the contents.

