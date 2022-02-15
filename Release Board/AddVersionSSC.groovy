project 'Bill of Materials',{
	catalog 'Release Board', {
		iconUrl = null

		catalogItem 'Add or Modify Package', {
			description = '''\
				<xml>
					<title>
						Add or Modify Package
					</title>

					<htmlData>
						<![CDATA[
							Specify an application package to create or modify
						]]>
					</htmlData>
				</xml>
			'''.stripIndent()
			buttonLabel = 'Create'
			dslString = '''\
				["compA","compB"].each { comp ->
					property 'releaseBoard',
						projectName: args['proj'],
						applicationName: args['app'],{
							property args['appVersion'],{
								property comp,
									value: args[comp]
								property "_status",
									value: args['stat']
							}
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
				label = "Application package version"
				orderIndex = '3'
				required = '1'
				type = 'entry'
			}
			formalParameter 'stat', defaultValue: "candidate", {
				label = "Package status"
				orderIndex = '4'
				required = '1'
				simpleList = 'production|candidate|archived'
				type = 'select'
			}
			formalParameter 'compA', defaultValue: null, {
				label = "CompA version"
				orderIndex = '5'
				required = '1'
				type = 'entry'
			}

			formalParameter 'compB', defaultValue: null, {
				label = "CompB version"
				orderIndex = '6'
				required = '1'
				type = 'entry'
			}
		}
	}
}