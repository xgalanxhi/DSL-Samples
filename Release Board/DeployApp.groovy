project 'Bill of Materials',{
	catalog 'Release Board', {
		iconUrl = null

		catalogItem 'Deploy Application', {
			description = '''\
				<xml>
					<title>
						Deploy Application package version
					</title>

					<htmlData>
						<![CDATA[
							Select that application package version to deploy
						]]>
					</htmlData>
				</xml>
			'''.stripIndent()
			buttonLabel = 'Deploy'
			dslString = '''\
				/*
				property 'releaseBoard',
					projectName: args['proj'],
					applicationName: args['app'],{
						property args['appVersion'],{
							property "_status",
								value: args['stat']
						}
					}
				*/
			'''.stripIndent()
			endTargetJson = null
			iconUrl = 'icon-catalog-item.svg'
			useFormalParameter = '1'

			formalParameter 'proj', defaultValue: projectName, {
				expansionDeferred = '0'
				label = "Application project name"
				orderIndex = '1'
				required = '1'
				type = 'project'
			}

			formalParameter 'app', defaultValue: "Sample App", {
				dependsOn = 'proj'
				label = "Application name"
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def selectedProjectName = args.parameters['proj']
					// If no project is selected then no apps to get
					if (selectedProjectName) {
						def applications = getApplications(projectName: selectedProjectName)
						applications.each {
							options.add(/*value*/it.applicationName, /*display text*/it.applicationName)
						}
					}

					return options
				'''.stripIndent()
				orderIndex = '2'
				required = '1'
				type = 'select'
			}
			formalParameter 'stat', defaultValue: "candidate", {
				expansionDeferred = '0'
				label = "Package status"
				orderIndex = '3'
				required = '1'
				simpleList = 'production|candidate|archived'
				type = 'radio'
			}
			formalParameter 'appVersion', defaultValue: null, {
				dependsOn = 'proj,app,stat'
				label = "Application package version"
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def proj = args.parameters['proj']
					def app = args.parameters['app']
					def stat = args.parameters['stat']
					if (app) {
						//def packages = getProperties(projectName : proj, applicationName : app, propertyName: "releaseBoard").property
						// Apparent bug in the above--works in DSL IDE, not here.
						def packages = getProperties(path: "/projects/${proj}/applications/${app}/releaseBoard").property
						packages.each {
							def appVersion = it.propertyName
							def appVersionStatus=getProperty("/projects/${proj}/applications/${app}/releaseBoard/${appVersion}/_status").value
							if (appVersionStatus==stat) options.add(appVersion,appVersion)
						}
					}
					return options
				'''.stripIndent()
				orderIndex = '4'
				required = '1'
				type = 'select'
			}		
		}
	}
}