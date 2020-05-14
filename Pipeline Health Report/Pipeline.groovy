/*

CloudBees CD DSL: Create and run a sample pipeline

This pipeline generates random code coverage and integrated test results that are used by the UnplugPipelineHealth.groovy code to create a report.

*/

project "Pipeline Health",{
	pipeline "Release Candidate",{
		formalParameter "ReleaseCandidate", defaultValue: 'RC-$[/increment /myPipeline/RC]'
		pipelineRunNameTemplate = 'Release Candidate $[ReleaseCandidate]'
		stage "Dev",{
			task 'Code Coverage', {
				actualParameter = [
					commandToRun: '''\
						ectool setProperty /myPipelineRuntime/CodeCoverage "$[/javascript
							(Math.random()*10+90).toFixed(1).toString()
							]"
						'''.stripIndent()
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}			
		}
		stage "QA",{
			task 'Itegrated Tests', {
				actualParameter = [
					commandToRun: '''\
						echo "$[/javascript
							var TotalTest=50;
							var Passing=(Math.floor((Math.random()*15))+36).toFixed(0)
							var Failing=(50-Passing).toFixed(0)
							api.setProperty({"propertyName": "/myPipelineRuntime/PassingIntegratedTests", "value": Passing})
							api.setProperty({"propertyName": "/myPipelineRuntime/FailingIntegratedTests", "value": Failing})
							"Passing: " + Passing + " " + "Failing: " + Failing
						]"
					'''.stripIndent()
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}
		}
		stage "UAT",{
			gate 'PRE', {
				task 'Promotion Approval', {
					gateType = 'PRE'
					notificationTemplate = 'ec_default_gate_task_notification_template'
					taskType = 'APPROVAL'
					approver = [
						'admin',
					]
				}
			}
		}
		stage "Prod"
	}
}

(1..5).each {
	runPipeline projectName: "Pipeline Health",	pipelineName: "Release Candidate", actualParameter: [ReleaseCandidate: 'RC-$[/increment /myPipeline/RC]']
}