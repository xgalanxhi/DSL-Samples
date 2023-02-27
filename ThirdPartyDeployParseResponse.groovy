/*

CloudBees CDRO Groovy DSL: Call a third party deployment tool and update inventory

This example illustrates how to use the Third Party Deployment feature https://docs.beescloud.com/docs/cloudbees-cd/latest/deploy-automation/third-party-deployment to update environment inventory based on the response from a third party deployment tool.

Instructions
------------
0. Evaluate this file using the DSL IDE
1. Run the pipeline "Third party deployment" in the project of the same name
2. Examine the Stage Summary; navigate the the QA environment
3. Examine the inventory. Note the application name and version, and the component and version

How it works
------------
A pipeline task set to "Deploy" type runs a procedure. The procedure runs a step generates dummy output similar to what a third party deployment system might generate--in practice, this would be a call to a plugin. The following step parses this repsonse and sets a property that the deploy task uses to update environment inventory:

If the job property ec_deployment_artifacts is set appropriately, environment inventory will be updated.

{
	"deploymentSystem": "name of the 3rd-party tool, e.g., Ansible, G3, Gitlab",
	"deploymentUrl: "Url for this deployment job in the 3rd-party tool",
	"applicationVersion": "1.2", # optional
	"artifacts": [
	  {
		"artifactName": "artifact1", # required
		"artifactVersion": "1.2.4", # required
		# Optional fields, their availability will depend on the deployment tool
		"artifactUrl": "https://repository.sonatype.org/service/local/repositories/atlassian-maven-external/content/org/apache/tomcat/apache-tomcat/10.0.14-atlassian-hosted/apache-tomcat-10.0.14-atlassian-hosted.pom",
		"additionalDetails": {
		  "deploymentTargets": "target1, target2",
		  "detail1": "value1",
		  "detail2": "value2"
		}
	  },
	  {
		"artifactName": "artifact2", # required
		"artifactVersion": "2.3.5", # required
		"artifactUrl": "https://repository.sonatype.org/service/local/repositories/atlassian-maven-external/content/org/apache/tomcat/apache-tomcat/10.0.14-atlassian-hosted/apache-tomcat-10.0.14-atlassian-hosted.pom",
		"additionalDetails": {
		  "deploymentTargets": "target2, target4",
		  "detail1": "value1",
		  "detail2": "value2"
		}
	  }
	]
}

*/

project "Third party deployment",{
	procedure "Call deployment tool and parse response",{
		formalParameter "deployStep", defaultValue: "Call deployment tool"
		step "Grab resource", command: 'ectool setProperty /myJob/resourceName --value "$[/myJobStep/assignedResourceName]"'
		step "Call deployment tool", shell: "ec-groovy", command: '''\
			print """\
				Installing application
				Application Name: "My App"
				Application Version: 1.0
				Components: myapp:2.0
				Deployment Hosts: host1, host2
			""".stripIndent()
		'''.stripIndent()
		step "Parse response for inventory items", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			import groovy.json.*
			ElectricFlow ef = new ElectricFlow()
			def logFile = "\$[deployStep].\$[/myJob/jobSteps/$[deployStep]/jobStepId].log"
			File theInfoFile = new File( logFile )
			if( !theInfoFile.exists() ) {
				println "File does not exist"
			} else {
				def keyPairs = [:]
				// Step through each line in the file
				theInfoFile.eachLine { line ->
					// If the line isn't blank
					if( line.trim() ) {
						// Split into a key and value
						def (key,value) = line.split( ': ' ).collect { it.trim() }
						// and store them in the keyPairs Map
						keyPairs."$key" = value
					}
				}
				def inventory = [
					deploymentSystem: "Dummy Third party deployment system",
					deploymentUrl: "https://example/app",
					applicationVersion: keyPairs["Application Version"],
					artifacts: [
						[
							artifactName: keyPairs["Components"].split(":")[0],
							artifactVersion: keyPairs["Components"].split(":")[1],
							artifactUrl: "https://example.com/myapp",
							additionalDetails: [
								deploymentTargets: keyPairs["Deployment Hosts"],
								detail1: "value1",
								detail2: "value2"
							]
						],
					]
				]				
				ef.setProperty(propertyName: "/myJob/ec_deployment_artifacts", value: JsonOutput.toJson(inventory))				
			}

		'''.stripIndent()
	}
	pipeline "Third party deployment",{
		stage "Stage 1",{
			task "Deploy",{
				taskType = 'PROCEDURE'
				subTaskType = 'DEPLOY'
				subproject = projectName
				subprocedure = 'Call deployment tool and parse response'
				applicationName = 'My App'
				applicationProjectName = projectName
				environmentName = 'QA'
				environmentProjectName = projectName
			}
		}
	}
}