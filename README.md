This is a lab to quickly get started with Elastic Search and Google Docs.

Get started
-----------

To get Elastic Search up and running:

	git clone https://github.com/jankronquist/elastic-search-lab.git
	mvn package
	mvn exec:java -Dexec.mainClass="com.jayway.search.RunElasticSearch"

You have now started Elastic Search in an embedded Java process! 

Import the code into your favorite IDE. You can of course run the class RunElasticSearch from here instead of from command line. 
However, make sure to only run a single Elastic Search process at a time.

Step 1
------

