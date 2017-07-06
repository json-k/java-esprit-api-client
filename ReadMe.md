# Esprit API Client

## Intro

This is a Java implementation of the [Dalim](https://dalim.com) [Esprit](https://www.dalim.com/en/products/es-enterprise-solutions/) API.

## Philosophy

REST and RPC APIs can often be a challenge - creating something robust that can differentiate between a transport issue and an API issue can be an interesting design challenge.

Implementing this particular API provides some unique challenges - objects of similar (or apparently the same) type behave in different ways, there is no simple linear inheritance of objects, the methods often act in unexpected ways - and as such it provides a very interesting project. My goal was to implement this as it is presented, that is without trying to abstract into something else.

This project is intended to be an embodiment of the idea that problems don't change in complexity simply because we wish it so.

# Maven

The project is now available in the Maven Central Repository. For your Gradle build:

```
	compile 'org.keeber:esprit-api-client:+'
```

# TODO

All the things