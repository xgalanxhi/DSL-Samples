/*

CloudBees SDA CD/RO DSL: Report on user logins

When applied, this DSL creates:
- Report object type "user"
- A report "User Login"
- A Dashboard with a widget showing the number of user logins as a function of time
- A procedure to push the loging data based on the property user property lastLoginTime
- A schedule run daily that executes the data gathering procedure

*/

import groovy.json.JsonOutput

reportObjectType "user",{
	displayName: "User"
	reportObjectAttribute "userName", 
		displayName: "User Name",
		required: true,
		type: "STRING"
	reportObjectAttribute "lastLoginTime",
		displayName: "Last login time",
		required: true,
		type: "DATETIME"
}

project "User Reporting",{
	report 'User Login', {
		description = ''
		reportObjectTypeName = 'user'
		def query = [
			searchCriteria: [],
			groupBy: [
				[field: "lastLoginTime"],
				[field: "userName"]
			],
			aggregationFunctions: [
				[ field: "lastLoginTime", function: "DISTINCT_COUNT"]
			]
		]
		reportQuery = JsonOutput.toJson(query)
	}
	
	dashboard 'Users', {
		layout = 'FLOW'
		type = 'STANDARD'

		reportingFilter 'DateFilter', {
			operator = 'BETWEEN'
			orderIndex = '0'
			parameterName = 'lastLoginTime'
			required = '1'
			type = 'DATE'
		}

		reportingFilter 'User', {
			operator = 'IN'
			orderIndex = '1'
			parameterName = 'userName'
			reportObjectTypeName = 'user'
			required = '0'
			type = 'CUSTOM'
		}

		widget 'Logins over time', {
			attributeDataType = [
				'yAxis': 'NUMBER',
				'xAxis': 'DATE',
			]
			attributePath = [
				'yAxis': 'lastLoginTime_count',
				'xAxis': 'lastLoginTime_label',
			]
			dashboardName = 'Users'
			reportName = 'User Login'
			title = 'Logins'
			visualization = 'VERTICAL_BAR_CHART'
		}
		widget 'User List', {
			attributeDataType = [
			  'column1': 'STRING',
			  'column3': 'NUMBER',
			  'column2': 'DATE',
			]
			attributePath = [
			  'column1': 'userName',
			  'column2Label': 'Last Login',
			  'column3': 'lastLoginTime_count',
			  'column2': 'lastLoginTime_label',
			  'column3Label': 'Number of logins',
			  'column1Label': 'User',
			]
			dashboardName = 'Users'
			reportName = 'User Login'
			title = 'User List'
			visualization = 'TABLE'
		}
	}


	procedure "Push login data",{
		step "Get and push", shell: "ec-groovy", command: '''\
			import groovy.json.JsonOutput
			import com.electriccloud.client.groovy.ElectricFlow
			import java.text.SimpleDateFormat 
			import java.util.Date

			ElectricFlow ef = new ElectricFlow()
			
			def lastRunString="1970-01-01T00:00:00.000Z"
			try {
				lastRunString = ef.getProperty(
					propertyName: "/myProcedure/lastRun")
					.property.value
			} catch (Exception e)  {
				println "No last run timestamp, moving on"
			}
			
			Date lastRun = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(lastRunString)
			
			def AllUsers = ef.getUsers().user
			AllUsers.each { U ->
				if (U.lastLoginTime) {
					Date lastLoginTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(U.lastLoginTime)
					if (lastLoginTime > lastRun) {
						def payload = [
							userName: U.userName,
							lastLoginTime: U.lastLoginTime,
						]
						ef.sendReportingData(
							reportObjectTypeName:"user",
							payload: JsonOutput.toJson(payload),
						)
						println payload
					} else {
						println "User ${U.userName} has not logged in since ${lastRun}"
					}
				}
			}
			
			ef.setProperty(
				propertyName: "/myProcedure/lastRun",
				value: ef.getProperty(propertyName:"/myJob/createTime").property.value
			)

		'''.stripIndent()
	}
	
	schedule 'Gather Login Data', {
		procedureName = 'Push login data'
		projectName = 'User Reporting'
		startTime = '00:00'
	}
}