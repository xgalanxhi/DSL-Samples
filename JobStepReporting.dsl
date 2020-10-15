/*

CloudBees CD DSL: Implement Job Step reporting

- Creates a new report object type "job_step"
- Creates a report to aggregate unique hostnames vs. start times
- Creates a dashboard with a widget that displays the number of unique hosts vs. start times
- Creates a procedure that finds all job steps with an assigned resource in a user-provided time range and pushes these
    to the DevOps Insights reporting database
- Creates a self service catalog item to wrap this procedure

Instructions
0. Apply this DSL (ectool evalDsl --dslFile jobStepReporting.dsl, or import and run in DSLIDE)
1. Run the self service catalog item "Push reporting data". Choose a small time range such as one day.
    The "Dry run" flag is set by default. Press OK and note how many records were found. If this number is
    less than say 10,000, rerun the job with the "Dry Run" option deselected (Use the Run... option from the run Job
    pull down menu).
3. Navigate to the DevOps Insights Dashboards and select "Resource Utilization" to view the number of hosts being used
    over time.

 */
reportObjectType "job_step", displayName: "Job Steps"
/*
All job step fields
-------------------
jobStepId, stepName, alwaysRun, broadcast, combinedStatus, status, condition, createTime, duration, elapsedTime, environmentWaitTime, errorHandling, exclusive, exclusiveMode, exitCode, external, finish, jobId, jobName, lastModifiedBy, licenseReshareWaitTime, licenseWaitTime, liveProcedureStep, logFileName, modifyTime, outcome, owner, parallel, postExitCode, postLogFileName, procedureName, projectName, propertySheetId, releaseExclusive, releaseMode, resourceWaitTime, retries, runTime, runnable, start, status, stepIndex, subprocedure, subproject, timeLimit, totalWaitTime, waitTime, workspaceWaitTime
 */
def fields = [
    "resourceName":"STRING",
    "hostName":"STRING",
    "jobName":"STRING",
    "jobStepId":"STRING",
    "stepName":"STRING",
    "startTime":"DATETIME",
    "endTime":"DATETIME",
    "duration":"DURATION",
    "projectName":"STRING"
]

fields.each { field, type ->
    /*
        description, enumerationValues, required, type:
            <BOOLEAN|CONSTANT|DATE|DATETIME|DURATION|NUMBER|PERCENT|STRING>
     */
    reportObjectAttribute reportObjectTypeName: "job_step", field, type: type
}

project 'Job Step Reporting',{

    report 'Host count by startTime', {
        reportObjectTypeName = 'job_step'
        reportQuery = '''\
            {
                "searchCriteria":[],
                "groupBy":[
                    {
                        "field":"startTime"
                    }
                ],
                "aggregationFunctions":[
                    {
                        "field":"hostName",
                        "function":"DISTINCT_COUNT"
                    }
                ]
            }
        '''.stripIndent()
    }

    report 'Host list', {
        reportObjectTypeName = 'job_step'
        reportQuery = '''\
            {
                "searchCriteria":[],
                "groupBy":[
                    {
                        "field":"hostName"
                    }
                ],
                "aggregationFunctions":[]
            }
        '''.stripIndent()
    }

    dashboard 'Resource Utilization', {
        layout = 'FLOW'
        type = 'STANDARD'

        reportingFilter 'DateFilter', {
            operator = 'BETWEEN'
            parameterName = 'startTime'
            required = '1'
            type = 'DATE'
        }

        reportingFilter 'Host Name', {
            operator = 'IN'
            parameterName = 'hostName'
            reportObjectTypeName = 'job_step'
            type = 'CUSTOM'
        }

        widget 'Hosts in use', {
            description = ''
            attributeDataType = [
                    'yAxis': 'NUMBER',
                    'xAxis': 'DATE',
            ]
            attributePath = [
                    'yAxis': 'distinct_count_hostName',
                    'xAxis': 'startTime_label',
            ]
            orderIndex = '1'
            reportName = 'Host count by startTime'
            title = 'Hosts in use'
            visualization = 'VERTICAL_BAR_CHART'
        }
        widget 'Host list', {
            attributeDataType = [
                    'column1': 'STRING',
            ]
            attributePath = [
                    'column1': 'hostName',
                    'column1Label': 'Host name',
            ]
            orderIndex = '2'
            reportName = 'Host list'
            title = 'Host list'
            visualization = 'TABLE'
        }
    }

    procedure 'Push job step reporting data',{
        formalParameter "startTime",
            label: "Start time",
            required: true,
            description: "YYYY-MM-ddTHH:mm:ss.sssZ or YYYY-MM-dd, e.g., 2020-10-01T04:00:00.000Z or 2020-10-01",
            orderIndex: 1,
            type: 'date'
        formalParameter "endTime",
            label: "End time",
            required: true,
            description: "YYYY-MM-ddTHH:mm:ss.sssZ or YYYY-MM-dd, e.g., 2020-10-01T04:00:00.000Z or 2020-10-01",
            orderIndex: 2,
            type: 'date'
        formalParameter 'dryRun',
            label: "Dry run",
            defaultValue: 'true',
            description: 'When unchecked, data will be pushed to the DevOps Insights reporting server',
            checkedValue: 'true',
            orderIndex: 3,
            type: 'checkbox',
            uncheckedValue: 'false'

        step "Gather and push", shell: "ec-groovy", command: '''\
            import groovy.json.JsonOutput
            import com.electriccloud.client.groovy.ElectricFlow
            import com.electriccloud.client.groovy.models.Filter
            ElectricFlow ef = new ElectricFlow()
            
            def startTime = "$[startTime]"
            def endTime = "$[endTime]"
            
            if (startTime.length() == 10)   startTime += "T00:00:00.000Z"
            if (endTime.length() == 10)     endTime   += "T00:00:00.000Z"

            Filter begin = new Filter(
                propertyName: "start",
                operator: "greaterThan",
                operand1: startTime
            )
            Filter end = new Filter(
                propertyName: "start",
                operator: "lessThan",
                operand1: endTime
            )
            Filter resource = new Filter(
                propertyName: "assignedResourceName",
                operator: "isNotNull",
            )
           
            def result = ef.findObjects(
                objectType: 'jobStep',
                filters: [begin, end, resource],
                maxIds: 0
            )
            
            if ("$[dryRun]" == "true") {
                ef.setProperty(propertyName: "summary", value: "Dry Run: Found ${result.object.size()} records")
            } else {
                ef.setProperty(propertyName: "summary", value: "Found ${result.object.size()} records")
                result.object.eachWithIndex { it, index ->
                    def payload = [
                        resourceName: it.jobStep.assignedResourceName,
                        hostName: it.jobStep.hostName,
                        jobName: it.jobStep.jobName,
                        stepName: it.jobStep.stepName,
                        jobStepId: it.jobStep.jobStepId,
                        startTime: it.jobStep.start,
                        duration: it.jobStep.elapsedTime,
                    ]
                    if (it.jobStep.finish) payload << [endTime: it.jobStep.finish]
                    ef.sendReportingData(
                        payload: JsonOutput.toJson(payload),
                        reportObjectTypeName: 'job_step'
                    )
                    ef.setProperty(propertyName: "summary", value: "Sent ${index+1} of ${result.object.size()} records")
                }         
            }
        '''.stripIndent()
    }
    catalog 'Resource Utilization', {
        catalogItem 'Push reporting data', {
            description = '''\
		<xml>
			<title>This SSC item will push job step data to the reporting database. This data can be use to report on agent host utilization.</title>
			<htmlData>
				<![CDATA[
					Run this self service catalog item to push resource utilization data to the DevOps Insights reporting server.
					<p>Chose a data range and run; by default, Dry Run is enabled, so no data will be sent to the reporting server but the number of element to be sent will be displayed. If this number is less that 10000, rerun the job with Dry Run disabled (use the pull down menu next to the run button on the Job page).</p>
					<p>Once there is data in the reporting server, the "Resource Utilization" dashboard should have content.</p>
				]]>
			</htmlData>
		</xml>
	'''.stripIndent()
            buttonLabel = 'Execute'
            iconUrl = 'icon-resource.svg'
            subprocedure = 'Push job step reporting data'
        }
    }
}