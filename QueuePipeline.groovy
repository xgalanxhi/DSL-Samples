/*

CloudBees CD/RO DSL: Queued Pipeline

This example pipeline illustrates how pipeline queuing can be implemented with
CD/RO. It involves a task at the beginning of the pipeline that identifies any
running pipelines that started before the current one and waits for those to
complete. Note that other "queued" pipeline runs will be "running" but waiting
their turn to advance.

Instructions
0. Apply this DSL using the command line or the DSL IDE
	ectool evalDsl --dslFile QueuePipeline.groovy
1. Runs several instances of the pipeline from the UI or commandline,
   DSL-Samples :: Queued Pipeline
   	ectool runPipeline "DSL-Samples" "Queued Pipeline"
2. Examine the pipeline runs. Note that one of them has advanced beyond the
   queuing task "Wait for previous", while the others are still running this
   task.
3. Allow the advanced pipeline to complete by approving the manual task.
4. Note that the next pipeline has started in sequence. The sequence can be
   seen in name of the pipeline run, for example,
   "Queued Pipeline_3_20230818160652" was the third pipeline run.

*/

project "DSL-Samples",{
	pipeline "Queued Pipeline",{
		stage "Stage 1",{
			task "Wait for previous", shell: 'ec-groovy', command: '''\
				import com.electriccloud.client.groovy.ElectricFlow
				import com.electriccloud.client.groovy.models.Filter
				import com.electriccloud.client.groovy.models.Select
				import com.electriccloud.client.groovy.models.Sort

				ElectricFlow ef = new ElectricFlow()
				
				Filter finish = new Filter('finish', 'isNull')
				Filter project = new Filter('projectName', 'equals', '$[/myPipelineRuntime/projectName]')
				Filter pipeline = new Filter('pipelineName', 'equals', '$[/myPipelineRuntime/pipelineName]')
				
				// Find other queued pipeline runs, including queued
				runningPipelineIds = []
				myStartTime = ef.getPipelineRuntimeDetails(flowRuntimeIds: ['$[/myPipelineRuntime/flowRuntimeId]']).flowRuntime[0].startTime
				ef.findObjects(
						objectType: 'flowRuntime',
						filters: [finish, project, pipeline],
					).object.each {
						if (it.flowRuntime.flowRuntimeId != '$[/myPipelineRuntime/flowRuntimeId]' && it.flowRuntime.start < myStartTime) runningPipelineIds.push(it.flowRuntime.flowRuntimeId)
					}
					
				// Wait for all other pipeline runs to complete
				if (! runningPipelineIds.empty) {
					def othersRunning = true
					while(othersRunning) {
						othersRunning = false
						runningPipelineIds.each {
							def pipelineCompleted = ef.getPipelineRuntimeDetails(flowRuntimeIds: [it]).flowRuntime[0].completed.toBoolean()
							othersRunning |= !(pipelineCompleted)
						}
						if (othersRunning) {
							println "Waiting on existing pipeline run"
							sleep(5000)
						}
					}
				} else {
					println "No running pipelines"
				}
				

				'''.stripIndent()
			task "Wait for manual response",{
				taskType = 'MANUAL'
				notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
				approver = ['Everyone']
			}
		}
	}
}
