/*

CloudBees CD DSL: On-demand pipeline task execution

There are situations where actions need to be run on-demand or asynchronously
to other pipeline task, such as, running regression tests, creating a pull
request, or viewing current release notes. This functionality can be
implemented using the CloudBees CD out-of-order task execution feature.

Instructions
1. Apply this DSL
	ectool evalDsl --dslFile On-demand pipeline tasks.groovy
	or run in DSLIDE
2. Run the pipeline, On-demand Pipeline Tasks::Delivery pipeline
3. Note that any of the tasks in the "On-demand tasks" stage can be run
    before the prior stage tasks have been run

*/


project "On-demand Pipeline Tasks",{
	pipeline "Delivery pipeline",{
		stage "QA",{
			colorCode = '#ff7f0e'
			task "Deploy", actualParameter: [commandToRun: "echo running Deploy"], taskType: 'COMMAND'
			task "Smoke test", actualParameter: [commandToRun: "echo running Smoke test"], taskType: 'COMMAND'
			gate 'POST', {
				task 'Promote', {
					notificationTemplate = 'ec_default_gate_task_notification_template'
					taskType = 'APPROVAL'
					approver = ['admin']
				}
			}
		}
		stage "Staging",{
			colorCode = '#2ca02c'
			task "Deploy", actualParameter: [commandToRun: "echo running Deploy"], taskType: 'COMMAND'
			task "Smoke test", actualParameter: [commandToRun: "echo running Smoke test"], taskType: 'COMMAND'
		}
		stage "On-demand tasks",{
			//condition = '$[/javascript !(myPipelineRuntime.stages.Staging.completed)]'
			colorCode = '#000000'
			task "Run regression tests", actualParameter: [commandToRun: "echo running Run regression tests"], taskType: 'COMMAND', allowOutOfOrderRun: '1', condition: '$[/javascript !(myPipelineRuntime.stages.Staging.completed)]'
			task "Create pull request", actualParameter: [commandToRun: "echo running Create pull request"], taskType: 'COMMAND', allowOutOfOrderRun: '1', condition: '$[/javascript !(myPipelineRuntime.stages.Staging.completed)]'
			task "View current release notes", actualParameter: [commandToRun: "echo running View current release notes"], taskType: 'COMMAND', allowOutOfOrderRun: '1', condition: '$[/javascript !(myPipelineRuntime.stages.Staging.completed)]'
		}
	}
}