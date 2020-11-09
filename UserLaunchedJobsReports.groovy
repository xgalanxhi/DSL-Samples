/*

TODO: filter out project:, service and admin users

*/

project "User count",{
	report 'User jobs', {
		reportObjectTypeName = 'job'
		reportQuery = '''\
			{
				"searchCriteria":[],
				"groupBy":[
					{
						"field":"jobStart"
					}
				],
				"aggregationFunctions":[
					{
						"field":"launchedByUser",
						"function":"DISTINCT_COUNT"
					}
				]
			}'''.stripIndent()
	}
	report 'User list', {
        reportObjectTypeName = 'job'
        reportQuery = '''\
            {
                "searchCriteria":[],
                "groupBy":[
                    {
                        "field":"launchedByUser"
                    }
                ],
                "aggregationFunctions":[]
            }
        '''.stripIndent()
    }
	dashboard 'User jobs', {
		layout = 'FLOW'
		type = 'STANDARD'
		reportingFilter 'DateFilter', {
			operator = 'BETWEEN'
			parameterName = 'jobStart'
			required = '1'
			type = 'DATE'
		}
		reportingFilter 'Projects', {
			operator = 'IN'
			parameterName = 'pipelineName'
			reportObjectTypeName = 'job'
			required = '0'
			type = 'CUSTOM'
		}
		widget 'Users', {
			attributeDataType = [
				'column1': 'STRING',
			]
			attributePath = [
				'column1': 'launchedByUser',
				'column1Label': 'User name',
			]
			reportName = 'User list'
			reportProjectName = 'User count'
			title = 'Users'
			visualization = 'TABLE'
        }
        widget 'Users total jobs', {
			attributeDataType = [
				'column1': 'STRING',
				'column2': 'NUMBER',
			]
			attributePath = [
				'column1': 'launchedByUser',
				'column2Label': 'Total Jobs',
				'column2': 'launchedByUser_count',
				'column1Label': 'User name',
			]
			reportName = 'User list'
			reportProjectName = projectName
			title = 'Users total jobs'
			visualization = 'TABLE'
        }
		widget 'User job count', {
			attributeDataType = [
				'yAxis': 'NUMBER',
				'xAxis': 'DATE',
			]
			attributePath = [
				'yAxis': 'distinct_count_launchedByUser',
				'xAxis': 'jobStart_label',
				'xAxisLabel': 'jobStart_label',
			]
			dashboardName = 'User jobs'
			reportProjectName = projectName
			title = 'User job count'
			visualization = 'VERTICAL_BAR_CHART'
		}
	}
}

