/*

CloudBees Flow DSL: Blue Green deployment pipeline using a property

This pipeline example illustrates how blue-green deployments can be managed in a pipeline. There is a property attached to the pipeline definition, ProdutionColor. It is either "blue" or "green" based on the last deployment to production. When this pipeline is run, the current value of ProductionColor is used to label the pipeline run. The pipeline first deployments to the non-active enviroment (!ProductionColor), then in the Production stage, traffic is routed to the Staging environment and finally, the ProductionColor is updated.


*/

project "Blue-Green Example",{
	pipeline 'Blue Green Deployments', {
		property "ProductionColor", value: "blue"
		disableMultipleActiveRuns = true // We don't want overlapping pipeline runs

		pipelineRunNameTemplate = 'Production is $[/myPipeline/ProductionColor] - $[/timestamp]'

		stage 'Staging', {
			colorCode = '#ff8040'

			task 'Identify Standby Color', {
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
				actualParameter = [
					commandToRun: '''\
						TARGETCOLOR=$[/javascript
										getProperty("/myPipeline/ProductionColor")=="blue"?
											api.setProperty({"propertyName":"/myPipelineRuntime/TargetColor","value":"green"}):
											api.setProperty({"propertyName":"/myPipelineRuntime/TargetColor","value":"blue"})
										myPipelineRuntime.TargetColor
									]
						ectool setProperty "/myStageRuntime/ec_summary/Standby Color" "<html><p style=\\"color:${TARGETCOLOR};\\">${TARGETCOLOR}</p></html>"
					'''.stripIndent(),
				]

			} // task

			task 'Deploy to Standby', {
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
				actualParameter = [
					commandToRun: '''\
						ectool setProperty "/myStageRuntime/ec_summary/Deploying to " "<html><p style=\\"color:$[/myPipelineRuntime/TargetColor];\\">$[/myPipelineRuntime/TargetColor]</p></html>"
					'''.stripIndent(),
				]
			}
			gate 'POST', {
				task 'Promotion Approval', {
					taskType = 'APPROVAL'
					approver = [
						'admin',
					]
				}
			}
		}

		stage 'Production', {
			colorCode = '#ffff00'

			task 'Route Traffic to Standby', {
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
				actualParameter = [
					commandToRun: '''\
						ectool setProperty "/myStageRuntime/ec_summary/Routing traffic to " "<html><p style=\\"color:$[/myPipelineRuntime/TargetColor];\\">$[/myPipelineRuntime/TargetColor]</p></html>"
					'''.stripIndent(),
				]
			}

			task 'Swap Standby-Production Labels', {
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
				actualParameter = [
					commandToRun: '''\
						PRODUCTIONCOLOR=$[/myPipeline/ProductionColor]
						TARGETCOLOR=$[/myPipelineRuntime/TargetColor]
						ectool setProperty /myPipeline/ProductionColor ${TARGETCOLOR}
						ectool setProperty "/myStageRuntime/ec_summary/Standby is now " "<html><p style=\\"color:${PRODUCTIONCOLOR};\\">${PRODUCTIONCOLOR}</p></html>"
						ectool setProperty "/myStageRuntime/ec_summary/Production is now " "<html><p style=\\"color:${TARGETCOLOR};\\">${TARGETCOLOR}</p></html>"
					'''.stripIndent(),
				]
			}
		}
	}
}