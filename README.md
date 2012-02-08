This is a lab to quickly get started with Elastic Search and Google Docs. 

Many examples have been taken from the Elastic Search website:

<http://www.elasticsearch.org/>

Get started
-----------

To get Elastic Search up and running:

	git clone https://github.com/jankronquist/elastic-search-lab.git
	mvn package
	mvn exec:java -Dexec.mainClass="com.jayway.search.RunElasticSearch"

You have now started Elastic Search in an embedded Java process! 

Import the code into your favorite IDE. You can of course run the class RunElasticSearch from here instead of from command line. 
Since Elastic Search provide automatic clustering functionality make sure to only run a single Elastic Search process at a time! 

**Do not install and run Elastic Search as the default settings will create a cluster across the local network!**  

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

Open the class ElasticSearchJava.

Lets get some data:

	GetResponse response = client.prepareGet("twitter", "tweet", "1").execute().actionGet();
	System.out.println(response);

Investigate the GetResponse class. 

* How can you get the value of a field? 
* How can you print the original JSON document?

Documentation: <http://www.elasticsearch.org/guide/reference/java-api/get.html>

Lets add data:

	import static org.elasticsearch.common.xcontent.XContentFactory.*;
	
	IndexResponse response = client.prepareIndex("twitter", "tweet", "3")
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


