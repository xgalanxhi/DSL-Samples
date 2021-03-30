/*

CloudBees SDA CD/RO DSL: Create release subpipeline report and dashboard

*/

project "DSL-Samples",{
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
