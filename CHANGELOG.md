CHANGELOG
===

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
BeanInfo doesn't work when there's assymetry in getter vs setter