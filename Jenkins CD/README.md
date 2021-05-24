# Jenkins CD plugin to kick off CloudBees CD pipeline and release

This example illustrates how to start CloudBees CD pipelines and releases from Jenkins using the
[Jenkins CloudBees CD plugin](https://plugins.jenkins.io/electricflow/) including how to
pass in parameters. Each example is made up of two files, the CD DSL and "Jenkinsfile". The CD
DSL is used to create the pipeline or release model. The Jenkinsfile is used to kick off
the pipeline or release from Jenkins.

## Instructions

1. Install the [Jenkins CloudBees CD plugin](https://plugins.jenkins.io/electricflow/) into your Jenkins Controller (fka "Master")
1. Create a configruation in Jenkins called "CD"
1. Apply [CDPipeline.groovy](CDPipeline.groovy) and [CDRelease.groovy](CDRelease.groovy)
through CloudBees CD DSLIDE or command line
1. Create a Jenkins pipeline jobs with each of the Jenkinsfile,
[StartCDPipeline_Jenkinsfile.groovy](StartCDPipeline_Jenkinsfile.groovy) and
[StartCDRelease_Jenkinsfile.groovy](StartCDRelease_Jenkinsfile.groovy)
1. Run the Jenkins jobs
1. Navigate to each of the jobs and select the link "Pipeline URL:" or "Release Pipeline Run URL:"
