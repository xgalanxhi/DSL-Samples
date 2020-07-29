/*

CloudBee CD DSL: Push custom build data to DevOps Insights reporting server/hostName

If you are using a custom build solution, you will want to instrument your build procedure to push data to the DevOps Insights reporting server so that data is available for the Continuous Integration dashboard. The example below illustrates how to do this instrumentation.

See https://docs.beescloud.com/docs/cloudbees-cd/latest/devops-insight/report-object-type-reference for details about the build report object and the expected data content.

*/

project "DSL-Samples",{
	procedure "Build",{
		step "Build with occasional errors",
			command: '$[/javascript (Math.random() < 0.1)?"exit 1":"echo OK"]'
		step "Push reporting data", alwaysRun: true, shell: "ec-groovy",
			command: '''\
				import com.electriccloud.client.groovy.ElectricFlow
				import groovy.json.JsonOutput
				ElectricFlow ef = new ElectricFlow()
				
				// Use the DSL to get the list below
				// def a=[];getReportObjectAttributes(reportObjectTypeName: "build").each { a.push(it.name) };a
				// [ "timestamp", "tags", "startTime", "sourceUrl", "source", "releaseUri", "releaseProjectName", "releaseName", "releaseId", "projectName", "pluginName", "pluginConfiguration", "launchedBy", "jobName", "flowRuntimeName", "flowRuntimeId", "endTime", "duration", "ciBuildDetailProjectName", "ciBuildDetailName", "ciBuildDetailId", "buildStatus", "buildNumber", "building", "baseDrilldownUrl" ]
				
				enum buildStatus{SUCCESS, FAILURE, UNSTABLE , NOT_BUILT, ABORTED, WARNING}
				// DATETIME: 2017-01-01T11:54:58.569Z
				
							
				
				def Payload = [
					// "timestamp":"",
					// "tags":"",
					"startTime":"$[/myJob/start]",
					"sourceUrl":"https://\$[/server/hostName]/commander/link/jobDetails/jobs/\$[/myJob/jobId]",
					"source":"CloudBee CD",
					// "releaseUri":"",
					// "releaseProjectName":"",
					// "releaseName":"",
					// "releaseId":"",
					"projectName":"$[/myProject/projectName]",
					"pluginName":"Custom",
					// "pluginConfiguration":"",
					"launchedBy":"$[/myJob/launchedByUser]",
					"jobName":"$[/myJob/jobName]",
					// "flowRuntimeName":"",
					// "flowRuntimeId":"",
					//"endTime":"$[/myJobStep/start]",
					"duration":$[/myJob/elapsedTime],
					"ciBuildDetailProjectName":"$[/myProject/projectName]",
					"ciBuildDetailName":"$[/myProcedure/procedureName]",
					// "ciBuildDetailId":"",
					"buildStatus":"$[/javascript (myJob.outcome=="success")?"SUCCESS":"FAILURE"]",
					"buildNumber":$[/increment /myProcedure/BuildNumber],
					// "building":"",
					"baseDrilldownUrl":"https://\$[/server/hostName]/commander/link/jobDetails/jobs/\$[/myJob/jobId]"
				]
				def PayloadJson = JsonOutput.toJson(Payload)
				println PayloadJson
				ef.sendReportingData( reportObjectTypeName: "build", payload: PayloadJson, validate: true)
		'''.stripIndent()
	}
}


