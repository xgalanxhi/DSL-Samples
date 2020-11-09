# Jenkins CD plugin to kick off CloudBees CD pipeline

This example illustrates how to start CloudBees CD pipelines from Jenkins using the [Jenkins CloudBees CD plugin](https://plugins.jenkins.io/electricflow/) and how to pass parameters into the CloudBees CD pipeline. This example is made up of two files, [CDPipeline.groovy](CDPipeline.groovy) which is used to build the sample pipeline in CloudBees CD and [Jenkinsfile.groovy](Jenkinsfile.groovy) which implments the Jenkins pipeline that kicks off the CloudBees CD pipeline.

## Instructions

1. Install the Jenkins CloudBees CD plugin into your Jenkins Controller (fka "Master")
1. Create a configruation in Jenkins called "CD"
1. Apply CDPipeline.groovy through CloudBees CD DSLIDE or command line
1. Create a Jenkins job with Jenkinsfile.groovy
1. Run the Jenkins job and examine the resultant CloudBees CD pipeline run
