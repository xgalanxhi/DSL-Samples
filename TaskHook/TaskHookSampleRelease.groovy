/*

CloudBees CD/RO DSL: Release to illustrate task hook parsing for Third-party deployment

The release model includes a procedure that simulates the deployment from a
third party tool by inserting artifact versions in multiple log files. The task
hook procedure extracts these artifact versions and saves the result to a property
which is used by the task third-party deploy feature to update environment inventory.

Instructions
0. Evaluate the DSL: ectool evalDsl --dslFile TaskHookSampleRelease.groovy
1. Start the Release "Task Hook Example Release"
2. Examine the logfiles
3. Navigate to Environment Inventory to see the results

*/


project 'Task Hook Example', {

	procedure 'Task Hook - Parse Deployment Artifacts', {
		description = 'It\'s a procedure that could be used as a Task Hook to parse deployment artifacts information from job logs and to store it in JSON format in "/myJob/ec_deployment_artifacts" so that it\'s used to seed inventories. TBD'
		timeLimit = '0'

		formalParameter 'stepsToHandle', {
			description = 'A list of end-of-line-separated list of step names. If empty - all steps will be processed'
			label = 'Steps to process logs'
			orderIndex = '1'
			type = 'textarea'
		}

		formalParameter 'parsingScript', defaultValue: '''\
			/*	
			 - `line` - contains a line of log file
			 - `deploymentData` - an object that is convered into JSON and stored on "/myJob/ec_deployment_artifacts"
			 {
			"deploymentSystem": "name of the 3rd-party tool, e.g., Ansible, G3, Gitlab",
			"deploymentUrl": "Url for this deployment job in the 3rd-party tool",
			"applicationVersion": "1.2", 
			"artifacts": [
				{
					"artifactName": "artifact1", 
					"artifactVersion": "1.2.4", 
					"artifactUrl": "https://repository.sonatype.org/service/local/repositories/atlassian-maven-external/content/org/apache/tomcat/apache-tomcat/10.0.14-atlassian-hosted/apache-tomcat-10.0.14-atlassian-hosted.pom",
					"additionalDetails": {
						"deploymentTargets": "target1, target2",
						"detail1": "value1",
						"detail2": "value2"
					}
				},
				{
					"artifactName": "artifact2",
					"artifactVersion": "2.3.5",
					"artifactUrl": "https://repository.sonatype.org/service/local/repositories/atlassian-maven-external/content/org/apache/tomcat/apache-tomcat/10.0.14-atlassian-hosted/apache-tomcat-10.0.14-atlassian-hosted.pom",
					"additionalDetails": {
						"deploymentTargets": "target2, target4",
						"detail1": "value1",
						"detail2": "value2"
					}
				}
			]
			}
			 - `artifacts` - a list of new artifacts data that will be added into `deploymentData.artifacts` 
			 
			 */
			 
			if (match = line =~/deploymentSystem (.*)/) {

				deploymentData << ["deploymentSystem", match.group(1)]

			} if (match = line =~/deploymentUrl (.*)/) {

				deploymentData << ["deploymentUrl", match.group(1)]

			} else if (match = line =~ /Deploying ([\\\\w-_.]+:)?([\\\\w-_.]+):([\\\\w-_.]+).*/) {

				def artifactNamePart1 = match.group(1)
				def artifactNamePart2 = match.group(2)
				def artifactName = artifactNamePart1? artifactNamePart1 + artifactNamePart2 :artifactNamePart2
				artifacts << [
					"artifactName" : artifactName,
					"artifactVersion" : match.group(3)
				]

			}'''.stripIndent(), {
			description = 'A groovy code that handle parsing of one line of log file and filling a deploymentData object that is finally stored in `/myJob/ec_deployment_artifacts` property'
			label = 'Parsing script'
			orderIndex = '2'
			required = '1'
			type = 'textarea'
		}

		step 'Process Logs', {
			command = '''\
			import groovy.json.JsonOutput
			import com.electriccloud.client.groovy.models.Filter
			import com.electriccloud.client.groovy.models.Sort

			import com.electriccloud.client.groovy.ElectricFlow

			ElectricFlow ef = new ElectricFlow()

			def commandToRunPatters = """
						import groovy.json.JsonOutput
						import groovy.json.JsonSlurper

						import com.electriccloud.client.groovy.ElectricFlow

						ElectricFlow ef = new ElectricFlow()

						def logFileName = ef.getProperty(propertyName: "/jobSteps/%s/logFileName").property.value

						File logFile = new File(logFileName)

						if (!logFile.exists()) {
								 println "File does not exist"

						} else {
						
							def deploymentData = [:]
							JsonSlurper jsonSlurper = new JsonSlurper()

							try {
								def dataString = ef.getProperty(propertyName: "/myJob/ec_deployment_artifacts").property.value
								deploymentData = jsonSlurper.parseText(dataString) 

							} catch (def ignore) {}


							logFile.eachLine { line ->
							 
						def artifacts = []
									
									$[parsingScript]
									
									if (!artifacts.isEmpty()) { 
										// add artifacts data into data object to store in \'ec_deployment_artifacts\' job property									 
										if (!deploymentData.get("artifacts")) {
												deploymentData << ["artifacts": []]				
										}
									 
										deploymentData.get("artifacts").addAll(artifacts)
									}
							}

							if (!deploymentData.isEmpty()) {
							
						//If deploymentData is not empty - store it in job property
									ef.setProperty(propertyName: "/myJob/ec_deployment_artifacts", value: JsonOutput.prettyPrint(JsonOutput.toJson(deploymentData)))
							}
						}
			"""

			// Searching for job steps 
			def sortSpecs = []
			sortSpecs << new Sort(propertyName: \'createTime\', order: \'ascending\')
			sortSpecs << new Sort(propertyName: \'stepIndex\', order: \'ascending\')

			def filters = []
			filters << new Filter(propertyName: \'jobId\', operator: \'equals\', operand1: \'$[/myJob/jobId]\')

			def stepsToHandle = """$[/myCall/stepsToHandle]"""
			if (stepsToHandle) {

				// Take only steps with specified names
					def stepNames = stepsToHandle.split(\'\\n\')
					def orFilter = new Filter(operator: \'or\')
					stepNames.each {
						orFilter.filter << new Filter(propertyName: \'stepName\', operator: \'equals\', operand1: it)
					}
					filters << orFilter
			}

			def res = ef.findObjects(objectType: \'jobStep\', filters: filters, sorts: sortSpecs)

			def objects = res.object
			if (objects) {

					objects.each {
							def jobStep = it.jobStep
							def resourceToUse = jobStep.assignedResourceName
							def stepName = jobStep.stepName

							if (!resourceToUse) {
									print "Skipping non-existent or not run job step \'$stepName\'."
							} else if (jobStep.jobStepId != \'$[/myJobStep/jobStepId]\' /*skip the current step itself*/) {
									def stepId = jobStep.jobStepId

									def commandToRun = String.format(commandToRunPatters, stepId)

									// create job step to run on the same resource as the step which log we process
									ef.createJobStep(
													jobStepName: "Process $stepName log",
													shell: "ec-groovy",
													command: commandToRun,
													jobStepId: \'$[/myJobStep/jobStepId]\',
													resourceName: resourceToUse
									)

							}

					}
			}

			'''.stripIndent()
			shell = 'ec-groovy'
		}
	}


	procedure 'Test Hook - Parse Logs in shared workspace', {
		description = 'The procedure can be used as a Task Hook to parse deployment artifact information from job logs whether all job steps run on the same resource or on different resources that share the same workspace. I will to store the artifact information it finds in JSON format to the property "/myJob/ec_deployment_artifacts" so that will be used to seed environment inventories.'

		formalParameter 'stepsToHandle', {
			description = 'A list of end-of-line-separated step names. If empty - all steps will be processed'
			label = 'Steps to process logs'
			orderIndex = '1'
			type = 'textarea'
		}
		formalParameter 'parsingScript', defaultValue: '''\
		/*	
			 - `line` - contains a line of log file
			 - `deploymentData` - an object that is convered into JSON and stored on "/myJob/ec_deployment_artifacts"
			 {
			"deploymentSystem": "name of the 3rd-party tool, e.g., Ansible, G3, Gitlab",
			"deploymentUrl": "Url for this deployment job in the 3rd-party tool",
			"applicationVersion": "1.2", 
			"artifacts": [
				{
				"artifactName": "artifact1", 
				"artifactVersion": "1.2.4", 
				"artifactUrl": "https://repository.sonatype.org/service/local/repositories/atlassian-maven-external/content/org/apache/tomcat/apache-tomcat/10.0.14-atlassian-hosted/apache-tomcat-10.0.14-atlassian-hosted.pom",
				"additionalDetails": {
					"deploymentTargets": "target1, target2",
					"detail1": "value1",
					"detail2": "value2"
				}
				},
				{
				"artifactName": "artifact2",
				"artifactVersion": "2.3.5",
				"artifactUrl": "https://repository.sonatype.org/service/local/repositories/atlassian-maven-external/content/org/apache/tomcat/apache-tomcat/10.0.14-atlassian-hosted/apache-tomcat-10.0.14-atlassian-hosted.pom",
				"additionalDetails": {
					"deploymentTargets": "target2, target4",
					"detail1": "value1",
					"detail2": "value2"
				}
				}
			]
			}
			 - `artifacts` - a list of new artifacts data that will be added into `deploymentData.artifacts` 
			 
		 */
		 
		if (match = line =~/deploymentSystem (.*)/) {

			deploymentData << ["deploymentSystem", match.group(1)]

		} if (match = line =~/deploymentUrl (.*)/) {

			deploymentData << ["deploymentUrl", match.group(1)]

		} else if (match = line =~ /Deploying ([\\\\w-_.]+:)?([\\\\w-_.]+):([\\\\w-_.]+).*/) {

			def artifactNamePart1 = match.group(1)
			def artifactNamePart2 = match.group(2)
			def artifactName = artifactNamePart1? artifactNamePart1 + artifactNamePart2 :artifactNamePart2
			artifacts << [
			"artifactName" : artifactName,
			"artifactVersion" : match.group(3)
			]

		}
		'''.stripIndent(), {
			description = 'Groovy code that handles parsing of one line of log file and fills a deploymentData object that is finally stored in `/myJob/ec_deployment_artifacts` property'
			label = 'Parsing script'
			orderIndex = '2'
			required = '1'
			type = 'textarea'
		}

		step 'Process Logs', {
			command = '''\
				import groovy.json.JsonOutput
				import groovy.json.JsonSlurper
				import com.electriccloud.client.groovy.models.Filter
				import com.electriccloud.client.groovy.models.Sort

				import com.electriccloud.client.groovy.ElectricFlow

				ElectricFlow ef = new ElectricFlow()

				// Searching for job steps 
				def sortSpecs = []
				sortSpecs << new Sort(propertyName: \'createTime\', order: \'ascending\')
				sortSpecs << new Sort(propertyName: \'stepIndex\', order: \'ascending\')

				def filters = []
				filters << new Filter(propertyName: \'jobId\', operator: \'equals\', operand1: \'$[/myJob/jobId]\')

				def stepsToHandle = """$[/myCall/stepsToHandle]"""
				if (stepsToHandle) {

					// Take only steps with specified names
						def stepNames = stepsToHandle.split(\'\\n\')
						def orFilter = new Filter(operator: \'or\')
						stepNames.each {
							orFilter.filter << new Filter(propertyName: \'stepName\', operator: \'equals\', operand1: it)
						}
						filters << orFilter
				}

				def res = ef.findObjects(objectType: \'jobStep\', filters: filters, sorts: sortSpecs)

				def objects = res.object
				if (objects) {

						objects.each {
								def jobStep = it.jobStep
								def stepName = jobStep.stepName

								if (jobStep.jobStepId != \'$[/myJobStep/jobStepId]\' /*skip the current step itself*/) {
										def stepId = jobStep.jobStepId

							println "Parsing step \'$stepName\'"
										parseLog(ef, stepId)
								}

						}
				}


				def parseLog(def ef, def stepId) {

					
						def logFileName = ef.getProperty(propertyName: String.format("/jobSteps/%s/logFileName", stepId)).property.value

							File logFile = new File(logFileName)

							if (!logFile.exists()) {
									 println "File does not exist"

							} else {
							
								def deploymentData = [:]
								JsonSlurper jsonSlurper = new JsonSlurper()

								try {
									def dataString = ef.getProperty(propertyName: "/myJob/ec_deployment_artifacts").property.value
									deploymentData = jsonSlurper.parseText(dataString) 

								} catch (def ignore) {}


								logFile.eachLine { line ->
								 
							def artifacts = []
										
										$[parsingScript]
										
										if (!artifacts.isEmpty()) { 
											// add artifacts data into data object to store in \'ec_deployment_artifacts\' job property									 
											if (!deploymentData.get("artifacts")) {
													deploymentData << ["artifacts": []]				
											}
										 
											deploymentData.get("artifacts").addAll(artifacts)
										}
								}
								

								if (!deploymentData.isEmpty()) {
								
							//If deploymentData is not empty - store it in job property
										ef.setProperty(propertyName: "/myJob/ec_deployment_artifacts", value: JsonOutput.prettyPrint(JsonOutput.toJson(deploymentData)))
								}
							} 

				}
				'''.stripIndent()
			shell = 'ec-groovy'
		}
	}

	procedure 'Test Task Hook Procedure', {

		step 'Initialization - no logs', {
			command = '// do nothing'
			shell = 'ec-groovy'
		}

		step 'Run Deploy', {
			subprocedure = 'Test Task Hook Subprocedure'
		}

		step 'Deploy artifact2', {
			command = 'echo "Deploying artifact2:v2.2"'
		}
	}

	procedure 'Test Task Hook Subprocedure', {

		step 'print general properties', {
			command = '''\
				echo "deploymentSystem: TaskHookTest"
				echo "deploymentUrl: https://github.com/electric-cloud-community/BringYourOwnDeployerPlugin"
				'''.stripIndent()
		}

		step 'print artifact1', {
			command = 'echo "Deploying artifact1:v1.1"'
		}
	}

	release 'Task Hook Example Release', {
		plannedEndDate = '2023-09-08T15:20'
		plannedStartDate = '2023-08-25T15:20'

		pipeline 'pipeline_Example Release', {

			stage 'Dev', {

				task 'Deploy-Procedure', {
				description = '''
					The task is run a test procedure that print in the console information about artifacts and versions which are supposed to be deployed. 
					A task hook procedure parses a log of several steps and creates `/myJob/ec_deployment_artifacts` property to be processed by BYOD logic.
					The example shows how can be organize parsing in case when one resource is assigned for a task so that workspace is accessible from any job step.
					'''.stripIndent()
				applicationName = 'procedure-app'
				applicationProjectName = projectName
				environmentName = 'Dev'
				environmentProjectName = projectName
				hookActualParameter = [
					'parsingScript': '''\
						if (match = line =~/deploymentSystem: (.*)/) {

							deploymentData << ["deploymentSystem": match.group(1)]

						} else if (match = line =~/deploymentUrl: (.*)/) {

							deploymentData << ["deploymentUrl": match.group(1)]

						} else if (match = line =~ /Deploying ([\\w-_.]+:)?([\\w-_.]+):([\\w-_.]+).*/) {

							def artifactNamePart1 = match.group(1)
							def artifactNamePart2 = match.group(2)
							def artifactName = artifactNamePart1? artifactNamePart1 + artifactNamePart2 :artifactNamePart2
							artifacts << [
							"artifactName" : artifactName,
							"artifactVersion" : match.group(3)
							]

						}
						'''.stripIndent(),
					'stepsToHandle': '''\
					print artifact1
					print general properties
					Deploy artifact2
					'''.stripIndent(),
				]
				hookProcedureName = 'Test Hook - Parse Logs in shared workspace'
				hookProjectName = projectName
				resourceName = 'local'
				subTaskType = 'DEPLOY'
				subprocedure = 'Test Task Hook Procedure'
				subproject = 'Task Hook Example'
				taskType = 'PROCEDURE'
				}
			}

			stage 'QA', {
			colorCode = '#ff7f0e'

			task 'Deploy-Procedure', {
				description = '''\
					The task is run a test procedure that print in the console information about artifacts and versions which are supposed to be deployed. 
					A task hook script parses a log of several steps and creates `/myJob/ec_deployment_artifacts` property to be processed by BYOD logic.
					Example show how to organize parsing in case if `default` pool that used to run procedure contains more than one resources and they don\'t share same workspace.
					'''.stripIndent()
				applicationName = 'procedure-app'
				applicationProjectName = projectName
				environmentName = 'QA'
				environmentProjectName = projectName
				hookActualParameter = [
					'parsingScript': '''\
						/*	
						 - `line` - contains a line of log file
						 - `deploymentData` - an object that is convered into JSON and stored on "/myJob/ec_deployment_artifacts"
						 {
						"deploymentSystem": "name of the 3rd-party tool, e.g., Ansible, G3, Gitlab",
						"deploymentUrl": "Url for this deployment job in the 3rd-party tool",
						"applicationVersion": "1.2", 
						"artifacts": [
							{
							"artifactName": "artifact1", 
							"artifactVersion": "1.2.4", 
							"artifactUrl": "https://repository.sonatype.org/service/local/repositories/atlassian-maven-external/content/org/apache/tomcat/apache-tomcat/10.0.14-atlassian-hosted/apache-tomcat-10.0.14-atlassian-hosted.pom",
							"additionalDetails": {
								"deploymentTargets": "target1, target2",
								"detail1": "value1",
								"detail2": "value2"
							}
							},
							{
							"artifactName": "artifact2",
							"artifactVersion": "2.3.5",
							"artifactUrl": "https://repository.sonatype.org/service/local/repositories/atlassian-maven-external/content/org/apache/tomcat/apache-tomcat/10.0.14-atlassian-hosted/apache-tomcat-10.0.14-atlassian-hosted.pom",
							"additionalDetails": {
								"deploymentTargets": "target2, target4",
								"detail1": "value1",
								"detail2": "value2"
							}
							}
						]
						}
						 - `artifacts` - a list of artifacts data in `deploymentData` 
						 
						 */
						 
						if (match = line =~/deploymentSystem: (.*)/) {

							deploymentData << ["deploymentSystem": match.group(1)]

						} else if (match = line =~/deploymentUrl: (.*)/) {

							deploymentData << ["deploymentUrl": match.group(1)]

						} else if (match = line =~ /Deploying ([\\\\w-_.]+:)?([\\\\w-_.]+):([\\\\w-_.]+).*/) {

							def artifactNamePart1 = match.group(1)
							def artifactNamePart2 = match.group(2)
							def artifactName = artifactNamePart1? artifactNamePart1 + artifactNamePart2 :artifactNamePart2
							artifacts << [
							"artifactName" : artifactName,
							"artifactVersion" : match.group(3)
							]

						}'''.stripIndent(),
						'stepsToHandle': '''\
							print artifact1
							print general properties
							Deploy artifact2
							'''.stripIndent(),
					]
					hookProcedureName = 'Task Hook - Parse Deployment Artifacts'
					hookProjectName = projectName
					subTaskType = 'DEPLOY'
					subprocedure = 'Test Task Hook Procedure'
					subproject = projectName
					taskType = 'PROCEDURE'
				}
			}
		}
	}
}