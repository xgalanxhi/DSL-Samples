project "Pipeline Analytics",{
	report 'PipelineEfficiency', {
		description = 'Pipeline efficiency - # of Automated/Manual tasks, and the total duration for the pipeline tasks'
		definition = '''\
		{
		"size": 0,
			"query": {
				"bool" : {
					"filter": [
						{ "term" : { "reportEventType" : "ef_pipeline_run_task_completed" }},
						{ "exists" : { "field" : "flowRuntimeStateFinish" }},
						{ "exists" : { "field" : "flowRuntimeStateStart" }},
						{ "exists" : { "field" : "pipelineId" }},
						{ "bool" : {
							"must_not" : [
							  { "exists" : { "field" : "releaseId" }}
							]
						}}
					],
					"must_not": [
						{ "term" : { "taskType" : "DEPLOYER" }},
						{ "term" : { "taskType" : "GROUP" }}
					]
				}
			},
			"aggregations" : {
				 "tasks" : {
					 "filters" : {
						 "filters" : [
						 { "match" : { "reportEventType" : "ef_pipeline_run_task_completed"	 }}
						 ]
					 },
					 "aggregations" : {
						 "total_duration" : {
							 "sum" : {
								 "script" : "doc.flowRuntimeStateFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStateStart.value.toInstant().toEpochMilli()"
							 }
						 },
						 "efficiency_type": {
							 "terms": {
								 "script" : "doc.containsKey(\'manual\') && doc[\'manual\'].value == true ? \'Manual\' : \'Automated\'"
							 },
							 "aggregations" : {
								 "duration" : {
									 "sum" : {
										 "script" : "doc.flowRuntimeStateFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStateStart.value.toInstant().toEpochMilli()"
									 }
								 }
							 }
						 }
					 }
				 }
			}
		}
		'''.stripIndent()
		reportObjectTypeName = 'pipelinerun'
		title = 'Total Pipeline Duration - Automated vs Manual Tasks %'
		uri = 'ef-pipelinerun-*/_search?pretty'
	}
	report 'PipelineAutomationOverTime', {
		description = 'Pipeline automation broken down by time'
		definition = '''\
		{
		"size": 0,
			"query": {
				"bool" : {
					"filter": [
						{ "term" : { "reportEventType" : "ef_pipeline_run_task_completed" }},
						{ "exists" : { "field" : "flowRuntimeStateFinish" }},
						{ "exists" : { "field" : "flowRuntimeStateStart" }},
						{ "exists" : { "field" : "pipelineId" }},
						{ "bool" : {
							"must_not" : [
							  { "exists" : { "field" : "releaseId" }}
							]
						}}
					],
					"must_not": [
						{ "term" : { "taskType" : "DEPLOYER" }},
						{ "term" : { "taskType" : "GROUP" }}
					]
				}
			},
			"aggregations" : {
				"pipeline_date": {
					 "date_histogram": {
						 "field": "flowRuntimeStateFinish",
						 "interval": "day",
						 "format": "yyyy-MM-dd",
						 "min_doc_count": "1"
					 },
					 "aggregations" : {
						"total_duration" : {
							"sum" : {
								"script" : "doc.flowRuntimeStateFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStateStart.value.toInstant().toEpochMilli()"
							}
						},
						"automated_tasks_duration" : {
							"sum" : {
								"script" : "doc.containsKey(\'manual\') && doc[\'manual\'].value == true ? 0 : (doc.flowRuntimeStateFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStateStart.value.toInstant().toEpochMilli())"
							}
						},
						"automation_percentage": {
							"bucket_script": {
								"buckets_path": {
									"automatedTasksDuration": "automated_tasks_duration",
									"totalDuration": "total_duration"
								},
								"script": "params.automatedTasksDuration / params.totalDuration * 100"
							}
						}
					}
				}
			}
		}
		'''.stripIndent()
		reportObjectTypeName = 'pipelinerun'
		title = '% of Automation in Pipelines Over Time'
		uri = 'ef-pipelinerun-*/_search?pretty'
	}
	report 'AveragePipelineDuration', {
		description = 'Average duration of pipelines over time'
		definition = '''
		{
			"query": {
				"bool" : {
					"filter": [
						{"exists" : {	"field" : "flowRuntimeFinish" }},
						{"exists" : {	"field" : "flowRuntimeStart" }},
						{ "bool" : {
							"must_not" : [
							  { "exists" : { "field" : "releaseId" }}
							]
						}}
					]
				}
			},
			"size": 0,
			"aggregations": {
				"pipeline_date" : {
					"date_histogram" : {
						"field" : "flowRuntimeFinish",
						"interval": "day",
						"format": "yyyy-MM-dd",
						"min_doc_count": "1"
					},
					"aggregations" : {
						"avg_duration" : {
							"avg" : {
								"script" : "doc.flowRuntimeFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStart.value.toInstant().toEpochMilli()"
							}
						},
						"pipeline_date_max" : { "max" : { "field" : "flowRuntimeFinish", "format" : "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'" } },
						"pipeline_date_min" : { "min" : { "field" : "flowRuntimeFinish", "format" : "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'" } }
					}
				}
			}
		}
		'''.stripIndent()
		reportObjectTypeName = 'pipelinerun'
		title = 'Average Duration of pipelines Over Time'
		uri = 'ef-pipelinerun-*/_search?pretty'
	}
	report 'PipelinesPerMonth', {
		description = 'Number of pipelines closed by month'
		definition = '''
		{
			"size": 0,
			"query": {
				"bool" : {
					"filter": [
						{ "exists" : { "field" : "flowRuntimeFinish" } },
						{ "bool" : {
									"must_not" : [
									  { "exists" : { "field" : "releaseId" }}
									]
						}}
					]
				}
			},
			"aggregations": {
				"pipeline_date" : {
					"date_histogram" : {
						"field" : "flowRuntimeFinish",
						"interval": "month",
						"format": "yyyy-MM-dd"
					},
					"aggregations" : {
						"pipeline_date_max" : { "max" : { "field" : "flowRuntimeFinish", "format" : "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'" } },
						"pipeline_date_min" : { "min" : { "field" : "flowRuntimeFinish", "format" : "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'" } }
					}
				}
			}
		}
		'''
		reportObjectTypeName = 'pipelinerun'
		title = 'Number of Pipelines Per Month'
		uri = 'ef-pipelinerun-*/_search?pretty'
	}
	report 'TopLongestPipelines', {
		description = 'The top 10 longest running pipelines'
		definition = '''\
			{
				"query": {
						"bool" : {
							"filter": [
								{"exists" : {	"field" : "flowRuntimeFinish" }},
								{"exists" : {	"field" : "flowRuntimeStart" }},
								{ "bool" : {
									"must_not" : [
									  { "exists" : { "field" : "releaseId" }}
									]
								}}
							]
						}
				},
				"size": 10,
				"script_fields": {
						"duration" : {
					"script" : {
							"inline": "doc.flowRuntimeFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStart.value.toInstant().toEpochMilli()"
					}
						}
				},
				"_source": {
						"includes": ["pipelineName", "pipelineId", "pipelineProjectName"]
				},
				"sort" : {
						"_script" : {
					"type" : "number",
					"script" : {
							"inline": "doc.flowRuntimeFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStart.value.toInstant().toEpochMilli()"
					},
					"order" : "desc"
						}
				}
			}
		'''.stripIndent()
		reportObjectTypeName = 'pipelinerun'
		title = 'Pipelines with Longest Duration'
		uri = 'ef-pipelinerun-*/_search?pretty'
	}
	report 'TopLongestTasks', {
		description = 'Top 10 longest pipeline tasks'
		definition = '''\
		{
			"size": 10,
			"query": {
				"bool" : {
					"filter": [
						{ "term" : { "reportEventType" : "ef_pipeline_run_task_completed" }},
						{ "exists" : { "field" : "flowRuntimeName" }},
						{ "exists" : { "field" : "flowRuntimeStateFinish" }},
						{ "exists" : { "field" : "flowRuntimeStateStart" }},
						{ "bool" : {
							"should" : [
								{ "exists" : { "field" : "taskName" }},
								{ "exists" : { "field" : "applicationName" }},
								{ "exists" : { "field" : "serviceName" }}
							],
							"must_not" : [
							  { "exists" : { "field" : "releaseId" }}
							]
						}}
					]
				}
			},
			"script_fields": {
				"duration" : {
					"script" : {
						"inline": "doc.flowRuntimeStateFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStateStart.value.toInstant().toEpochMilli()"
					}
				},
				"task_or_app" : {
					"script" : {
						"inline": "doc.containsKey(\'taskName\') && doc[\'taskName\'].value != null ? doc.taskName.value : ((doc.containsKey(\'serviceName\') && doc[\'serviceName\'].value != null ? doc.serviceName.value : doc.applicationName.value) + \' - Deployer\' )"
					}
				},
				"manual" : {
					 "script" : {
						 "inline": "doc.containsKey(\'manual\') && doc[\'manual\'].value == true ? \'Manual\': \'\'"
					 }
				},
				"taskPath" : {
					 "script" : {
						 "inline": "(doc.containsKey(\'groupName\') && doc[\'groupName\'].value != null ? (doc[\'groupName\'].value + \'/\') : \'\') + (doc.containsKey(\'parentTaskName\') && doc[\'parentTaskName\'].value != null ? (doc[\'parentTaskName\'].value + \'/\') : \'\') + (doc.containsKey(\'taskName\') && doc[\'taskName\'].value != null ? doc[\'taskName\'].value : (doc.containsKey(\'serviceName\') && doc[\'serviceName\'].value != null ? doc.serviceName.value : doc.applicationName.value))"
					 }
				}
			},
			"_source": {
				"includes": ["taskType", "taskName", "stageName", "flowRuntimeName", "applicationName", "projectName",
							 "parentTaskName", "parentTaskType", "groupName"]
			},
			"sort" : {
				"_script" : {
					"type" : "number",
					"script" : {
						"inline": "doc.flowRuntimeStateFinish.value.toInstant().toEpochMilli() - doc.flowRuntimeStateStart.value.toInstant().toEpochMilli()"
					},
					"order" : "desc"
				}
			}
		}
		'''.stripIndent()
		reportObjectTypeName = 'pipelinerun'
		title = 'The top 10 longest running pipeline tasks'
		uri = 'ef-pipelinerun-*/_search?pretty'
	}

	dashboard 'Pipelines', {
		description = 'Pipelines Dashboard'
		layout = 'FLOW'
		type = 'STANDARD'

		reportingFilter 'DateFilter', {
			description = 'Filter pipelines and tasks by @timestamp field which is mapped from pipeline actual end date and task finish date.'
			operator = 'BETWEEN'
			orderIndex = '1'
			parameterName = '@timestamp'
			required = '1'
			type = 'DATE'
		}

		reportingFilter 'ProjectFilter', {
			description = 'Filter pipelines by project'
			operator = 'IN'
			orderIndex = '2'
			required = '0'
			type = 'PROJECT'
		}
		reportingFilter 'Pipeline Name', {
			operator = 'IN'
			orderIndex = '3'
			parameterName = 'pipelineName'
			reportObjectTypeName = 'pipelinerun'
			required = '0'
			type = 'CUSTOM'
		}
/*
**** No pipeline type? ****
		reportingFilter 'PipelineFilter', {
			description = 'Filter pipelines by pipeline'
			operator = 'IN'
			orderIndex = '3'
			required = '0'
			type = 'pipeline'
		}
*/
		reportingFilter 'TagFilter', {
			description = 'Filter pipelines by tag.'
			operator = 'IN'
			orderIndex = '4'
			parameterName = 'tags'
			required = '0'
			type = 'TAG'
		}

		widget 'PipelineEfficiency', {
			description = 'Pipeline efficiency - # of Automated/Manual tasks, and the total duration for the pipeline tasks'
			attributeDataType = [
				'total': 'DURATION',
				'yAxis': 'DURATION',
				'xAxis': 'STRING',
			]
			attributePath = [
				'total': 'total_duration',
				'yAxis': 'duration',
				'xAxis': 'efficiency_type',
			]
			color = [
				'Automated': '#223F9B',
				'Manual': '#00ADF1',
			]
			orderIndex = '1'
			reportName = 'PipelineEfficiency'
			reportProjectName = projectName
			visualization = 'DONUT_CHART'
		}

		widget 'PipelineAutomationOverTime', {
			description = 'Pipeline automation broken down by time'
			attributeDataType = [
				'yAxis': 'PERCENT',
				'xAxis': 'DATE',
			]
			attributePath = [
				'yAxis': 'automation_percentage',
				'xAxis': 'pipeline_date_label',
			]
			orderIndex = '2'
			reportName = 'PipelineAutomationOverTime'
			reportProjectName = projectName
			visualization = 'AREA_CHART'
			visualizationProperty = [
				'defaultColor': '#00ADF1',
			]
		}

		widget 'AveragePipelineDuration', {
			description = 'Average duration of pipelines over time'
			attributeDataType = [
				'yAxis': 'DURATION',
				'xAxis': 'DATE',
			]
			attributePath = [
				'yAxis': 'avg_duration',
				'xAxis': 'pipeline_date_label',
			]
			orderIndex = '3'
			reportName = 'AveragePipelineDuration'
			reportProjectName = projectName
			visualization = 'LINE_CHART'
			visualizationProperty = [
				'defaultColor': '#223F9B',
			]
		}

		widget 'PipelinesPerMonth', {
			description = 'Number of pipelines closed by month'
			attributeDataType = [
				'yAxis': 'NUMBER',
				'xAxis': 'DATE',
			]
			attributePath = [
				'yAxis': 'pipeline_date_count',
				'xAxis': 'pipeline_date_label',
			]
			orderIndex = '4'
			reportName = 'PipelinesPerMonth'
			reportProjectName = projectName
			visualization = 'VERTICAL_BAR_CHART'
			visualizationProperty = [
				'defaultColor': '#223F9B',
			]
		}

		widget 'TopLongestPipelines', {
			description = 'The top 10 longest running pipelines'
			attributeDataType = [
				'column1': 'STRING',
				'column2': 'DURATION',
			]
			attributePath = [
				'column1': 'pipelineName',
				'column2': 'duration',
			]
			iconUrl = 'icon-pipeline.svg'
			orderIndex = '6'
			reportName = 'TopLongestPipelines'
			reportProjectName = projectName
			visualization = 'TABLE'
		}

		widget 'TopLongestTasks', {
			description = 'The top 10 longest running pipeline'
			attributeDataType = [
				'column1': 'STRING',
				'column3': 'DURATION',
				'column2': 'STRING',
			]
			attributePath = [
				'column1': 'task_or_app',
				'column3': 'duration',
				'column2': 'manual',
			]
			iconUrl = 'icon-task.svg'
			orderIndex = '7'
			reportName = 'TopLongestTasks'
			reportProjectName = projectName
			visualization = 'TABLE'
		}
	}
}