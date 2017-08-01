# Esprit API Client

## Intro

This is a Java implementation of the [Dalim](https://dalim.com) [Esprit](https://www.dalim.com/en/products/es-enterprise-solutions/) API.

## Philosophy

REST and RPC APIs can often be a challenge - creating something robust that can differentiate between a transport issue and an API issue can be an interesting design challenge.

Implementing this particular API provides some unique challenges - objects of similar (or apparently the same) type behave in different ways, there is no simple linear inheritance of objects, the methods often act in unexpected ways - and as such it provides a very interesting project. My goal was to implement this as it is presented, that is without trying to abstract into something else.

This project is intended to be an embodiment of the idea that problems don't change in complexity simply because we wish it so. Also, after completing most of it I understand that we often don't understand something until we have implemented it.

# Maven

The project is now available in the Maven Central Repository. For your Gradle build:

```
	compile 'org.keeber:esprit-api-client:+'
```

# Quickstart

## EspritAPI

The entry for the API is the EspritAPI class. Create one and login (logout when you are done so you don't use up a session on your Esprit server):

```java
	EspritAPI api=new EspritAPI("https://my.eshost.com","username","password");
	api.login();
	///Do useful things.
	//SERIOUSLY - always log out.
	api.logout();
```

The EspirtAPI class implements Closeable - and has a special constructor that opens it automatically on creation so it can be used in a try with resources:

```java
    try (EspritAPI api = new EspritAPI("https://my.eshost.com", "username", "password", true)) {
      ///Do useful work.
    } catch (EspritConnectionException e) {
      Logger.getGlobal().log(Level.SEVERE, "An API error occurred.", e);
    }
```

The API class methods are structured based on the API method names. The "document.get" method can be found at api.documment.get(...) Each method should only have one method signature in the implementation. Some methods that take complex parameters have builders or helper objects along side the method call:

```java
	api.production.executeSQL(api.production.newSQLQueryBuilder().addC...)
```
Creation and object edit methods take parameters from the Es Object themselves: example: 

```java
	api.document.create(EsDocument.create(EsRef.from(6564), "random.jpg").withMoveFile(false).withURL("https://ckinknoazoro.files.wordpress.com/2011/06/random.jpg").addMetadata("Namespace", "prop", "the-value"))
```
## API Responses

All API calls return responses when they are successfully able to complete the API call - they only throw exceptions in the event of a HTTP or auth error. Every ApiResponse contains an optional result and error. The delegation to Optional methods means that interesting patterns are available:

```java
	ApiResponse<EsDocument> response = api.document.get(EsRef.from(90988900), false);
	if (response.hasResult()) {
		///Document was found
	} 
	
	/// Get the result or throw.
    EsDocument document = api.document.get(EsRef.from(90988900), false).orThrow(() -> new DocumentNotFoundException());
    // This call can't really fail (to produce a result)
    EsColorSpace.ListOf colors = api.production.colorSpaces().get();
```

## EsRef (and EsReferencable...and Classable)

Lots of calls in ES require a path or an ID - in this case the EsRef class can be used - as it can be created from either:

```java
	api.document.get(EsRef.from(90988900), false);
```

 Lots of objects are already references (implement EsReferencable) so to save some time you can use an existing object where available (Documents, Jobs, Folders, Customers).

```java
	api.document.delete(myDocument);
```

Some calls require a path OR an ID and class - in this case we have the EsClassable interface - implemented by (Documents, Jobs, Folder, Customers).

```java
	//Created reference
	EsWorkflowStep.ListOf wfl=api.workflow.get(EsRef.WithClass.from(19125007, EsClass.PageOrder), "MyWORKFLOW").get();
	//From an existing document reference
	EsDocument doc=api.document.get(EsRef.from(19125997), false).get();
	wfl=api.workflow.get(doc, "MyWORKFLOW").get();
```

## EspritAPIManager

The EspritAPIManager always returns a logged in EspritAPI instance. EspritAPI instances are "acquired" then "released" - if the API instance is idle (ie: one isn't checked out) for the connection timeout it logs out of ES.

```java
	EspritAPIManager manager = new EspritAPIManager("https://my.esprit.com", "username", "password");
	EspritAPI api = manager.acquireAPI();
	/// Do useful things
	manager.releaseAPI();
	//
	manager.shutdown();
```

The manager should be shut down when it is no longer needed (there are threads that need killing).

The EspritAPI instances acquired from the manager can be closed (they implement Closeable) and will actually release themselves from their parent manager. This allows for this pattern:

```java
    EspritAPIManager manager = new EspritAPIManager("https://my.esprit.com", "username", "password");
    try(EspritAPI api = manager.acquireAPI()){
      /// Do useful things
    } catch (EspritConnectionException e) {
      Logger.getGlobal().log(Level.SEVERE, "An API error occurred.", e);
    }
    manager.shutdown();
```

# Todo

More documentation.

A couple of the folder methods, most of the directory API, misc cleanup.
