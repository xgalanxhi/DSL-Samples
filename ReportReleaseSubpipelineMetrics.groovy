/*

CloudBees SDA CD/RO DSL: Release metrics from subpipelines

This example illustrates how to use Release Pipeline output parameters to capture metrics from
subpipelines kicked off by the Release. This is done by having the subpipelines set these Release
Pipeline output parameters. Pipeline output parameters are automatically pushed to the reporting
database for are pipeline runtime events once the parameters have been set.

Instructions
1. Apply this DSL file
	ectool evalDsl --dslFile ReportReleaseSubpipelineMetrics.groovy
	or copy and paste to the DSLIDE and runtime
2. Examine the Dashboard DSL-Samples :: Release SubPipeline Metrics

What the DSL does
1. Creates a Release with output parameters (Sub1 and Sub2) and kicks off two subpipelines (Sub1 and Sub2)
2. Creates the subpipelines (Sub1 and Sub2) which set the parent Release output paramters, Sub1 and Sub2
	respectively
3. Creates a Report that filters for pipeline runtime events where these parameters are set and aggregations
	the Releases and the parameters, only returning one entry per parameter
4. Creates a dashboard with a widget that displays each Release with its respective output parameter values
5. Runs the releases

*/

project "DSL-Samples",{
	["Sub1","Sub2"].each { SubPipeline ->
		pipeline SubPipeline,{
			stage "Stage 1",{
				task "Set release output parameter",{
					actualParameter = [
						commandToRun: (String) "ectool setOutputParameter ${SubPipeline} \"${SubPipeline} output\" --flowRuntimeId \$[/myTriggeringPipelineRuntime/flowRuntimeId]",
					]
					subpluginKey = 'EC-Core'
					subprocedure = 'RunCommand'
					taskType = 'COMMAND'
				}
			}
		}
		["Rel1","Rel2"].each { Rel ->
			release Rel,{
				pipeline Rel,{
					formalOutputParameter SubPipeline
					stage "Dev",{
						task SubPipeline,{
							taskType = "PIPELINE"
							subpipelineProject = projectName
							subpipeline = SubPipeline
						}
					}
				}
			}
		}		
	}
	report "Release Subpipelines",{
		reportObjectTypeName = 'pipelinerun'
		reportQuery = '{"searchCriteria":[{"criterion":"MUST","conditions":[{"field":"ec_param_Sub1","operator":"EXISTS"},{"field":"ec_param_Sub2","operator":"EXISTS"}]}],"groupBy":[{"field":"releaseName","bucketSize":"","name":"Release Name"},{"field":"ec_param_Sub1","bucketSize":"1"},{"field":"ec_param_Sub2","bucketSize":"1"}],"aggregationFunctions":[]}'
	}
	dashboard 'Release SubPipeline Metrics', {
		layout = 'FLOW'
		type = 'STANDARD'

		reportingFilter 'DateFilter', {
			operator = 'BETWEEN'
			orderIndex = '0'
			parameterName = '@timestamp'
			required = '1'
			type = 'DATE'
		}

		reportingFilter 'ProjectFilter', {
			operator = 'IN'
			orderIndex = '1'
			required = '0'
			type = 'PROJECT'
		}

		reportingFilter 'ReleaseFilter', {
			operator = 'IN'
			orderIndex = '2'
			required = '0'
			type = 'RELEASE'
		}

		reportingFilter 'TagFilter', {
			operator = 'IN'
			orderIndex = '3'
			parameterName = 'tags'
			required = '0'
			type = 'TAG'
		}

		widget 'Release Metrics Table', {
			description = ''
			attributeDataType = [
				'column1': 'STRING',
				'column3': 'STRING',
				'column2': 'STRING',
			]
			attributePath = [
				'column1': 'Release Name',
				'column2Label': 'Sub1',
				'column3': 'ec_param_Sub2',
				'column2': 'ec_param_Sub1',
				'column3Label': 'Sub2',
				'column1Label': 'Release',
			]
			orderIndex = '2'
			reportName = 'Release Subpipelines'
			reportProjectName = 'DSL-Samples'
			title = 'Release Meterics'
			visualization = 'TABLE'
		}
	}
}

["Rel1","Rel2"].each { Rel ->
	startRelease projectName: "DSL-Samples", releaseName: Rel
}
