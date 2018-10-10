CHANGELOG
===

##### 1.255
Upcoming
* [PR #136](https://github.com/stapler/stapler/pull/136) - log when a property is left out of serialised result due to exception
Related to [JENKINS-48198](https://issues.jenkins-ci.org/browse/JENKINS-48198).

##### 1.254
Release date: Dec 15, 2017
* [PR #135](https://github.com/stapler/stapler/pull/135) - allow customizing `RequirePOST` error pages

##### 1.253
Release date: Oct 20, 2017
* [PR #127](https://github.com/stapler/stapler/pull/127) - performance optimizations
* [PR #128](https://github.com/stapler/stapler/pull/128) - fixing binary incompatibility in 1.251
* [PR #129](https://github.com/stapler/stapler/pull/129) - adding default implementations for some `DataWriter` methods
* [PR #130](https://github.com/stapler/stapler/pull/130) - updated to Servlet API 3.1.0
* [PR #134](https://github.com/stapler/stapler/pull/134) - updating various dependencies, better matching versions in Jenkins core

##### 1.252
Release date: Aug 03, 2017
* [PR #113](https://github.com/stapler/stapler/pull/113) - **updated to Java 8** and testing some effects of that
* [PR #117](https://github.com/stapler/stapler/pull/117) - made `ClassDescriptor` deterministic
* [PR #118](https://github.com/stapler/stapler/pull/118) - deprecated `HttpResponses.html` and `.plainText` in favor of `.literalHtml` and `.text`.
* [PR #123](https://github.com/stapler/stapler/pull/123) - lower Guava dep to 11 to match Jenkins core

##### 1.251
Release date: May 22, 2017
* [PR #106](https://github.com/stapler/stapler/pull/106) -
Support configurable stapler serialization behavior.
Related to [JENKINS-40088](https://issues.jenkins-ci.org/browse/JENKINS-40088).
* [Issue #109](https://github.com/stapler/stapler/issues/109) -
Prevent `NullPointerException` in `IndexHtmlDispatcher` when index.html resource is provided in an interface.
Fixes [JENKINS-43715](https://issues.jenkins-ci.org/browse/JENKINS-43715), regression in 1.249.
* [PR #112](https://github.com/stapler/stapler/pull/112) -
Prevent `NullPointerException` in `FilteringTreePruner` when child object in the tree is `null`.
Related to [JENKINS-40979](https://issues.jenkins-ci.org/browse/JENKINS-40979).

##### 1.250
Release date: January 20, 2017
* [PR 103](https://github.com/stapler/stapler/pull/103) -
Fix use of `static` methods in PR 96 in 1.249.

##### 1.249
Release date: January 18, 2017
* [PR 89](https://github.com/stapler/stapler/pull/89) -
Be more robust when exporting collections ([JENKINS-40088](https://issues.jenkins-ci.org/browse/JENKINS-40088))
* [PR 94](https://github.com/stapler/stapler/pull/94) -
Define an annotation to inject `req.getSubmittedForm()`
* [PR 96](https://github.com/stapler/stapler/pull/96) -
Use `MethodHandle` for faster reflective method invocation; Stapler now requires Java 7
* [PR 99](https://github.com/stapler/stapler/pull/99) -
Detect wrong array type in `RequestImpl.convertJSON()`
* [PR 101](https://github.com/stapler/stapler/pull/101) (and 102) -
Prefer `doIndex` over `index` views

##### 1.248
Release date: November 7, 2016
* [PR 88](https://github.com/stapler/stapler/pull/88) - 
Fixes binary compatibility issues caused in Stapler 1.246. Related to [JENKINS-39414](https://issues.jenkins-ci.org/browse/JENKINS-39414). Also see [Issue 86](https://github.com/stapler/stapler/issues/86). 

##### 1.247
Release date: November 14, 2016
* [PR 83](https://github.com/stapler/stapler/pull/83) - 
Jenkinsfile to build Stapler.
* [PR 84](https://github.com/stapler/stapler/pull/84) - 
Add a since tag for documentation of jelly attributes.
* [PR 85](https://github.com/stapler/stapler/pull/85) - 
Workaround Ruby Runtime Plugin and Jenkins 2.28 compatibility issues. Fixes compatibility issues/regressions caused by Stapler 1.246. Related to [JENKINS-39414](https://issues.jenkins-ci.org/browse/JENKINS-39414).

##### 1.246
Release date: October 14, 2016
* [PR 78](https://github.com/stapler/stapler/pull/78) -
Immediately throw an EOF in a boundary error condition. This fix is related to [JENKINS-37664](https://issues.jenkins-ci.org/browse/JENKINS-37664).
* [PR 79](https://github.com/stapler/stapler/pull/79) -
Allow JSON responses to be compressed when serving with serveStaticResource.
* [PR 80](https://github.com/stapler/stapler/pull/80) -
Correctly route to public fields on super class. It fixes the issue where public fields were not picked correctly thru inheritance, this resulted in regression detected in Jenkins tests. 

##### 1.244
Release Date: June 18, 2016
* [PR 73](https://github.com/stapler/stapler/pull/73) - 
StaplerResponseWrapper to help override methods if needed
* [PR 75](https://github.com/stapler/stapler/pull/75) -
Fix related to handling of relative weight of Accept header in AcceptHeader
* [PR 77](https://github.com/stapler/stapler/pull/77) -
Parallel request routing for Blue Ocean. This will allow BlueOcean or any Jenkins plugin to tweak request routing, for example to serve request routes that otherwise be served by jelly views as JSON response. See [Issue 76](https://github.com/stapler/stapler/issues/76).
 
##### 1.243
Release date: April 29, 2016
* [PR 74](https://github.com/stapler/stapler/pull/74/files) - 
BeanInfo doesn't work when there's asymmetry in getter vs setter
