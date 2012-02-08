This file contains hints for some of the steps. The current hint is:

// @BEGIN_VERSION es-java-api
Have a look at:

* QueryBuilders.fuzzyQuery
* QueryBuilders.rangeQuery
* QueryBuilders.boolQuery

// @END_VERSION es-java-api
// @BEGIN_VERSION es-java-documents
Try this:

	.startObject()
		.field("file", Base64.encodeFromFile(fileName))
	.endObject()
// @END_VERSION es-java-documents
// @BEGIN_VERSION es-highlighting
N/A
// @END_VERSION es-highlighting
// @BEGIN_VERSION gdocs-list
See <http://code.google.com/apis/documents/docs/2.0/developers_guide_java.html#ListDocs>
// @END_VERSION gdocs-list
// @BEGIN_VERSION gdocs-download
N/A
// @END_VERSION gdocs-download
// @BEGIN_VERSION integration
N/A
// @END_VERSION integration
