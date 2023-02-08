/*

Cloudbees CDRO DSL: Releases created over time dashboard and report

*/

project "DSL-Samples",{

	report 'Release created', {
		reportObjectTypeName = 'release'
		definition = '''\
			{ 
				"size": 0,
					"aggregations": {
						"CreatedOn": {
							"date_histogram": {
								"field": "@timestamp",
								"format": "yyyy-MM-dd",
								"calendar_interval": "1M",
								"offset": 0,
								"order": {
									"_key": "asc"
								},
								"keyed": false,
								"min_doc_count": 1
							}
						}
					}
			}
		'''.stripIndent()
	}

	dashboard 'Created Releases', {
		reportingFilter 'DateFilter', {
			dashboardName = 'Created Releases'
			operator = 'BETWEEN'
			orderIndex = '1'
			parameterName = '@timestamp'
			required = '1'
			type = 'DATE'
		}

		widget 'Created Releases', {
			attributeDataType = [
				'yAxis': 'NUMBER',
				'xAxis': 'DATE',
			]
			attributePath = [
				'yAxis': 'CreatedOn_count',
				'xAxis': 'CreatedOn_label',
				'xAxisLabel': 'CreatedOn_label',
			]
			dashboardName = 'Created Releases'
			orderIndex = '4'
			reportName = 'Release created'
			reportProjectName = projectName
			title = 'Created Releases over time'
			visualization = 'VERTICAL_BAR_CHART'
		}
	}
}