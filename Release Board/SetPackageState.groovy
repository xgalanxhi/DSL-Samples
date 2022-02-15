project 'Bill of Materials',{
	catalog 'Release Board', {
		iconUrl = null

		catalogItem 'Set package stage', {
			description = '''\
				<xml>
					<title>
						Set an application package status
					</title>

					<htmlData>
						<![CDATA[
							Select an application package for status modification
						]]>
					</htmlData>
				</xml>
			'''.stripIndent()
			buttonLabel = 'Set'
			dslString = '''\
				property 'releaseBoard',
					projectName: args['proj'],
					applicationName: args['app'],{
						property args['appVersion'],{
							property "_status",
								value: args['stat']
						}
					}
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
			formalParameter 'appVersion', defaultValue: null, {
				dependsOn = 'proj,app'
				label = "Application package version"
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def proj = args.parameters['proj']
					def app = args.parameters['app']
					if (app) {
						//def packages = getProperties(projectName : proj, applicationName : app, propertyName: "releaseBoard").property
						// Apparent bug in the above--works in DSL IDE, not here.
						def packages = getProperties(path: "/projects/${proj}/applications/${app}/releaseBoard").property
						packages.each {
							options.add(/*value*/it.propertyName, /*display text*/it.propertyName)
						}
					}
					return options
				'''.stripIndent()
				orderIndex = '3'
				required = '1'
				type = 'select'
			}			
			formalParameter 'stat', defaultValue: null, {
				label = "Package status"
				orderIndex = '4'
				required = '1'
				simpleList = 'production|candidate|archived'
				type = 'select'
			}
		}
	}
}