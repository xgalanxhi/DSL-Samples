/*
	Sample Code for creating RCC Configurations
	CloudBees Flow DSL
	
	This code illustrates how to create multiple Jenkins RCC build configurations
	
	Instructions
	1. Edit the Project, Release, JenkinsConfig and JenkinsJobs below
	2. Run this DSL code (from EC-DSLIDE, Flow DSL Editor, ectool evalDsl --dslFile <thisfile>
	3. Examine the Command Center Editor Setup for the specified Release

*/

// ---- Edit These -----
def Project = "My Release Project"
def Release = "My Release"
def JenkinsConfig = "MyJenkinsConfig"
def JenkinsJobs = [
		"job1",
		"job2"
	]
// ---- End of User edits ----

JenkinsJobs.each { job ->
	devOpsInsightDataSource job,
		projectName: Project,
		releaseName: Release,
		pluginKey: "EC-Jenkins",
		reportObjectTypeName: "build",
		pluginParameters: [
			configName:JenkinsConfig,
			jenkinsProject:job,
			jenkinsTestUrl:"/testReport",		
			frequency: "10"
			]
	}
