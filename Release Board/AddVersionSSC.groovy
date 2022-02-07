project 'Bill of Materials',{
	catalog 'Release Board', {
		iconUrl = null

		catalogItem 'Add or Modify Package', {
			description = '''\
				<xml>
					<title>
			
					</title>

					<htmlData>
						<![CDATA[
							
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
							}
						}
				}
			'''.stripIndent()
			endTargetJson = null
			iconUrl = 'icon-catalog-item.svg'
			useFormalParameter = '1'

			formalParameter 'proj', defaultValue: null, {
				expansionDeferred = '0'
				label = null
				orderIndex = '1'
				required = '1'
				type = 'project'
			}

			formalParameter 'app', defaultValue: null, {
				dependsOn = 'proj'
				label = null
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
				label = null
				orderIndex = '3'
				required = '1'
				type = 'entry'
			}
			formalParameter 'compA', defaultValue: null, {
				label = null
				orderIndex = '4'
				required = '1'
				type = 'entry'
			}

			formalParameter 'compB', defaultValue: null, {
				label = null
				orderIndex = '5'
				required = '1'
				type = 'entry'
			}
		}
	}
}