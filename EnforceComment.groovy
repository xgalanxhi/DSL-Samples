/*

CloudBees SDA CD/RO DSL: Fail pipeline if a comment is not provided in the Manual task

*/

project "DSL-Samples",{
	pipeline 'Enforce Comment', {
		stage 'Stage 1', {
			task 'Manual task', {
				instruction='Comment field should be filled in, otherwise the pipeline will fail.'
				notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
				taskType = 'MANUAL'
				approver = [
					'Everyone',
				]
			}
			gate 'POST', {
				task 'Verify Comment Exists', {
					gateCondition = '$[/javascript myPipelineRuntime.stages["Stage 1"].tasks["Manual Task"]]'
					gateType = 'POST'
					taskType = 'CONDITIONAL'
				}
			}
		}
	}
}