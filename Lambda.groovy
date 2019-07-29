/*

Flow DSL Code
Sample Application to do an AWS Lambda deployment

- Creates an EC-AWS-Lambda configuration
- Creates an application model with
	- Create lambda function
	- Run lambda function
- Creates resource to issue EC-AWS-Lambda API commands
- Creates one environment for deployment

Dependencies:
	EC-AWS-Lambda plugin
	Artifact (zip file to be pushed to Lambda), published to EC-Artifact
	AWS Tokens
	
To do:
- Implement undeploy function (not currently available in the EC-AWS-Lambda plugin)

*/

//____________Make changes here to reflect your data______________

def uName='XXXXXXXXXXXXXXXXXXXX'
def pwd='YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY'
def ArtifactName = "com.cloudbees:myHelloWorld"

def ProjectName = "Demo"

//________________________________________________________________

/*

Create Lambda function artifact

*/

project ProjectName,{
	procedure 'Create Artifact', {

		step 'Create Function File', {
			subprocedure = 'AddTextToFile'
			subproject = '/plugins/EC-FileOps/project'
			actualParameter 'AddNewLine', '1'
			actualParameter 'Append', '0'
			actualParameter 'Content', '''\
			import json

			def lambda_handler(event, context):
				x = \'Hello from CloudBees Lambda\'
				return x
		'''.stripIndent()
			actualParameter 'Path', 'lambda_function.py'
		}

		step 'Zip file', {
			subprocedure = 'Create Zip File'
			subproject = '/plugins/EC-FileOps/project'
			actualParameter 'sourceFile', 'lambda_function.py'
			actualParameter 'zipFile', 'myHelloWorld.zip'
		}

		step 'Publish artifact', {
			subprocedure = 'Publish'
			subproject = '/plugins/EC-Artifact/project'
			actualParameter 'artifactName', 'com.cloudbees:myHelloWorld'
			actualParameter 'artifactVersionVersion', '2.0.0-$[/increment /server/ec_counters/artifactCounter]'
			actualParameter 'compress', '0'
			actualParameter 'includePatterns', 'myHelloWorld.zip'
			actualParameter 'repositoryName', 'default'
		}
	}
} // project

runProcedure(projectName: ProjectName, procedureName: 'Create Artifact')

/*

Create EC-AWS-Lambda plugin configuration / credentials

*/

def conf="Lambda"
def proj="/plugins/EC-AWS-Lambda/project"

// Create a Transient credential
def Cred = new RuntimeCredentialImpl()		
Cred.name = conf					
Cred.userName = uName		
Cred.password = pwd
def Creds=[Cred]

// Call the config creation procedure
// if it does not already exists
// by checking if the config property (name may be different in different plugin)
if (! getProperty("$proj/ec_plugin_cfgs/$conf")) {
	runProcedure(
		projectName : proj,
		procedureName : "CreateConfiguration",
		actualParameter : [
			config: conf,							 // required
			aws_region: "us-west-1",	// required
			credential: conf,					 // Credential has the same name than the config
		],
		 credential: Creds
	)
} else {
	// overwrite the	credential
	credential(
		projectName: proj,
		userNane: uName,
		password: pwd,
		credentialName: conf
	)
	// overtrite properties
	//setProperty("$proj/ServiceNow_cfgs/$conf/host": value: "http://myNewHost"
}


/*

Create application and environments

*/

resource "lambda", hostName: "localhost"

project ProjectName, {

	environment 'Dev', {
		environmentEnabled = '1'

		environmentTier 'Tier 1', {
			resourceName = [
				'lambda',
			]
		}
	}

	application 'My Lambda', {
		description = ''

		applicationTier 'Tier 1', {
			applicationName = 'My Lambda'

			component 'Hello World', pluginName: null, {
				applicationName = 'My Lambda'
				pluginKey = 'EC-Artifact'

				process 'Create Function', {
					processType = 'OTHER'

					processStep 'Retrieve', {
						applicationTierName = null
						actualParameter = [
							'artifactName': '$[/myComponent/ec_content_details/artifactName]',
							'artifactVersionLocationProperty': '$[/myComponent/ec_content_details/artifactVersionLocationProperty]',
							'filterList': '$[/myComponent/ec_content_details/filterList]',
							'overwrite': '$[/myComponent/ec_content_details/overwrite]',
							'retrieveToDirectory': '$[/myComponent/ec_content_details/retrieveToDirectory]',
							'versionRange': '$[/myJob/ec_Hello World-version]',
						]
						dependencyJoinType = 'and'
						errorHandling = 'abortJob'
						processStepType = 'component'
						subprocedure = 'Retrieve'
						subproject = '/plugins/EC-Artifact/project'
					}

					processStep 'Create Lambda function', {
						applicationTierName = null
						actualParameter = [
							'aws_role': 'arn:aws:iam::547883162893:role/dmitriys_role',
							'config': 'Lambda',
							'deployment_package_path': '/tmp/myHelloWorld.zip',
							'function_name': '$[AppName]',
							'lambda_handler': 'lambda_function.lambda_handler',
							'runtime_name': 'python3.7',
						]
						dependencyJoinType = 'and'
						errorHandling = 'abortJob'
						processStepType = 'plugin'
						subprocedure = 'CreateLambdaFunction'
						subproject = '/plugins/EC-AWS-Lambda/project'
					}

					processDependency 'Retrieve', targetProcessStepName: 'Create Lambda function', {
						branchType = 'ALWAYS'
					}
				}

				process 'Run', {
					processType = 'DEPLOY'

					processStep 'Retrieve', {
						applicationTierName = null
						actualParameter = [
							'artifactName': '$[/myComponent/ec_content_details/artifactName]',
							'artifactVersionLocationProperty': '$[/myComponent/ec_content_details/artifactVersionLocationProperty]',
							'filterList': '$[/myComponent/ec_content_details/filterList]',
							'overwrite': '$[/myComponent/ec_content_details/overwrite]',
							'retrieveToDirectory': '$[/myComponent/ec_content_details/retrieveToDirectory]',
							'versionRange': '$[/myJob/ec_Hello World-version]',
						]
						dependencyJoinType = 'and'
						errorHandling = 'abortJob'
						processStepType = 'component'
						subprocedure = 'Retrieve'
						subproject = '/plugins/EC-Artifact/project'
					}

					processStep 'Run', {
						applicationTierName = null
						actualParameter = [
							'config': 'Lambda',
							'function_name': '$[AppName]',
							'function_parameters': '',
							'is_regexp': '0',
							'success_criteria': '"Hello from CloudBees Lambda"',
						]
						dependencyJoinType = 'and'
						errorHandling = 'abortJob'
						processStepType = 'plugin'
						subprocedure = 'RunLambdaFunction'
						subproject = '/plugins/EC-AWS-Lambda/project'
					}

					processDependency 'Retrieve', targetProcessStepName: 'Run', {
						branchType = 'ALWAYS'
					}
				}

				process 'Uninstall', {
					processType = 'UNDEPLOY'

					processStep 'Retrieve', {
					applicationTierName = null
					applicationName = null
						actualParameter = [
							'artifactName': '$[/myComponent/ec_content_details/artifactName]',
							'artifactVersionLocationProperty': '$[/myComponent/ec_content_details/artifactVersionLocationProperty]',
							'filterList': '$[/myComponent/ec_content_details/filterList]',
							'overwrite': '$[/myComponent/ec_content_details/overwrite]',
							'retrieveToDirectory': '$[/myComponent/ec_content_details/retrieveToDirectory]',
							'versionRange': '$[/myJob/ec_Hello World-version]',
						]
						dependencyJoinType = 'and'
						errorHandling = 'abortJob'
						processStepType = 'component'
						subprocedure = 'Retrieve'
						subproject = '/plugins/EC-Artifact/project'
					}
				}

				// Custom properties

				property 'ec_content_details', {

					// Custom properties

					property 'artifactName', value: ArtifactName, {
						expandable = '1'
					}
					artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
					filterList = ''
					overwrite = 'update'
					pluginProcedure = 'Retrieve'

					property 'pluginProjectName', value: 'EC-Artifact', {
						expandable = '1'
					}
					retrieveToDirectory = '/tmp'

					property 'versionRange', value: '', {
						expandable = '1'
					}
				}
			}
		}

		process 'Create', {
			applicationName = 'My Lambda'
			processType = 'OTHER'

			formalParameter 'AppName', defaultValue: 'myHelloWorld_', {
				orderIndex = '1'
				required = '1'
				type = 'entry'
			}

			formalParameter 'ec_Hello World-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_Hello World-version', defaultValue: '$[/myApplication/components/Hello World/ec_content_details/versionRange]', {
				expansionDeferred = '1'
				type = 'entry'
			}

			formalParameter 'ec_enforceDependencies', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_smartDeployOption', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_stageArtifacts', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			processStep 'Create Lambda', {
				applicationTierName = 'Tier 1'
				dependencyJoinType = 'and'
				errorHandling = 'abortJob'
				processStepType = 'process'
				subcomponent = 'Hello World'
				subcomponentApplicationName = 'My Lambda'
				subcomponentProcess = 'Create Function'

				// Custom properties

				property 'ec_deploy', {

					// Custom properties
				}
			}

			// Custom properties

			property 'ec_deploy', {

				// Custom properties
			}
		}

		process 'Run', {
			applicationName = 'My Lambda'
			processType = 'OTHER'

			formalParameter 'AppName', defaultValue: 'myHelloWorld_', {
				orderIndex = '1'
				required = '1'
				type = 'entry'
			}

			formalParameter 'ec_Hello World-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_Hello World-version', defaultValue: '$[/projects/IDB/applications/My Lambda/components/Hello World/ec_content_details/versionRange]', {
				expansionDeferred = '1'
				type = 'entry'
			}

			formalParameter 'ec_enforceDependencies', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_smartDeployOption', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_stageArtifacts', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			processStep 'Run Lambda', {
				applicationTierName = 'Tier 1'
				dependencyJoinType = 'and'
				errorHandling = 'abortJob'
				processStepType = 'process'
				subcomponent = 'Hello World'
				subcomponentApplicationName = 'My Lambda'
				subcomponentProcess = 'Run'

				// Custom properties

				property 'ec_deploy', {

					// Custom properties
				}
			}

			// Custom properties

			property 'ec_deploy', {

				// Custom properties
			}
		}

		process 'Undeploy', {
			applicationName = 'My Lambda'
			processType = 'OTHER'

			formalParameter 'ec_Hello World-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_Hello World-version', defaultValue: '$[/projects/IDB/applications/My Lambda/components/Hello World/ec_content_details/versionRange]', {
				expansionDeferred = '1'
				type = 'entry'
			}

			formalParameter 'ec_enforceDependencies', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_smartDeployOption', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_stageArtifacts', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			processStep 'Uninstall', {
				applicationTierName = 'Tier 1'
				dependencyJoinType = 'and'
				errorHandling = 'abortJob'
				processStepType = 'process'
				subcomponent = 'Hello World'
				subcomponentApplicationName = 'My Lambda'
				subcomponentProcess = 'Uninstall'

				// Custom properties

				property 'ec_deploy', {

					// Custom properties
				}
			}

			// Custom properties

			property 'ec_deploy', {

				// Custom properties
			}
		}

		tierMap 'Dev', {
			applicationName = 'My Lambda'
			environmentName = 'Dev'
			environmentProjectName = ProjectName

			tierMapping 'DevMap', {
				applicationTierName = 'Tier 1'
				environmentTierName = 'Tier 1'
			}
		}

		// Custom properties

		property 'ec_deploy', {

			// Custom properties
		}

		property 'jobCounter', value: '9', {
			expandable = '1'
			suppressValueTracking = '1'
		}
	}
}
