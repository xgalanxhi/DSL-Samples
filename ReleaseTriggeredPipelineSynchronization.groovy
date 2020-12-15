/*

CloudBees CD DSL: Release triggered subpipeline synchronization

1. Run the release
2. Approve the wait gates
3. The subpipeline stages should wait for the respective release stages to be complete

*/

def randomRgbCode() {
		def rgb = new Random().nextInt(1 << 24) // A random 24-bit integer
		'#' + Integer.toString(rgb, 16).padLeft(6, '0')
}

["QA","UAT","Preprod","Prod"].reverse().each { _Stage ->
	project "DSL-Samples",{
		pipeline "Subpipeline",{
			stage _Stage, colorCode: randomRgbCode(),{
				gate 'POST', {
					task 'Wait on release', {
						gateType = 'POST'
						gateCondition = 'true'
						precondition = '$[/myTriggeringPipelineRuntime/stages[$[/myStageRuntime/stageName]]/completed]'
						taskType = 'CONDITIONAL'
					}
				}
			}
		}
		release "Main release",{
			pipeline "Main release pipeline",{
				stage "Start", colorCode: randomRgbCode(), {
					task "Subpipeline",{
						subpipeline = 'Subpipeline'
						taskType = 'PIPELINE'
						triggerType = 'async'

					}
				}
				stage _Stage, colorCode: randomRgbCode(),{
					gate 'PRE', {
						task 'wait', {
							gateType = 'PRE'
							notificationTemplate = 'ec_default_gate_task_notification_template'
							taskType = 'APPROVAL'
							approver = [ 'admin' ]
						}
					}
				}
			}
		} // release
	} // project
} // each