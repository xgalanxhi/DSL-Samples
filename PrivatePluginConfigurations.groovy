/*

CloudBees CD DSL: Service Catalog Item - Private Plugin Configurations

By default, if a principals (user, group, project, or service acccount) has access to a plugin configuration, it has access to all of the configurations for that plugin. This self service catalog (SSC) item can be used to restrict access to a limited set of principals. It accomplishes this be breaking inheritance on a selected plugin configuration and adds ACL entries for a set of principals. This SSC can be run multiple times to add more principals ACL entries.

*/

project "Administration",{
	catalog 'Administration', {
		iconUrl = null
		catalogItem 'Private Plugin Configurations', {
			description = '''\
				<xml>
					<title>
						Make plugin configurations visible and usable by a select set of groups, users, projects, and/or service accounts
					</title>

					<htmlData>
						<![CDATA[
							By default, if a principals (user, group, project, or service acccount) has access to a plugin configuration, it has access to all of the configurations for that plugin. This self service catalog (SSC) item can be used to restrict access to a limited set of principals. It accomplishes this be breaking inheritance on a selected plugin configuration and adds ACL entries for a set of principals. This SSC can be run multiple times to add more principals ACL entries.
						]]>
					</htmlData>
				</xml>
			'''.stripIndent()
			allowScheduling = '0'
			buttonLabel = 'Execute'
			dslParamForm = null
			dslString = '''\
				def Plugin=args.Plugin
				def Config=args.Config
				def Group=args.Group
				def User=args.User
				def Project=args.Project
				def Service=args.Service
				
				def Location=getProperty("/plugins/${Plugin}/project/ec_config/configLocation").value
				def ConfigId=getProperty("/plugins/${Plugin}/project/${Location}/${Config}").propertySheetId
				
				breakAclInheritance(propertySheetId: ConfigId)
				
				if (Group) {
					aclEntry principalName: Group, principalType: "group", objectType: "propertySheet", propertySheetId: ConfigId, readPrivilege: "allow", executePrivilege: "allow"
				}
				
				if (User) {
					aclEntry principalName: User, principalType: "user", objectType: "propertySheet", propertySheetId: ConfigId, readPrivilege: "allow", executePrivilege: "allow"
				}

				if (Project) {
					aclEntry principalName: "project: ${Project}", principalType: "user", objectType: "propertySheet", propertySheetId: ConfigId, readPrivilege: "allow", executePrivilege: "allow"
				}
				
				if (Service) {
					aclEntry principalName: Service, principalType: "serviceAccount", objectType: "propertySheet", propertySheetId: ConfigId, readPrivilege: "allow", executePrivilege: "allow"
				}	
			
			'''.stripIndent()
			endTargetJson = null
			iconUrl = 'icon-catalog-item.svg'
			subpluginKey = null
			subprocedure = null
			subproject = null
			useFormalParameter = '1'

			formalParameter 'Plugin', defaultValue: '', {
				expansionDeferred = '0'
				label = null
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def Plugins = getPlugins()
					Plugins.each { Plugin ->
						def MyPluginKey = Plugin.pluginKey
						def MyPluginName = Plugin.pluginName
						if (getCredentials(projectName: MyPluginName)?.credentialName) {
							options.add(/*value*/ MyPluginKey, /*displayString*/ MyPluginKey)
						}
					}
					return options
				'''.stripIndent()
				orderIndex = '1'
				required = '1'
				type = 'select'
			}

			formalParameter 'Config', defaultValue: null, {
				dependsOn = 'Plugin'
				expansionDeferred = '0'
				label = null
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def Plugin= args.parameters[\'Plugin\']?:"EC-Jenkins"
					def Location=getProperty("/plugins/${Plugin}/project/ec_config/configLocation").value
					def ConfigsId=getProperty("/plugins/${Plugin}/project/${Location}").propertySheetId
					def Configs=getProperties(propertySheetId: ConfigsId).property
					Configs.each { Config ->
						options.add(/*value*/ Config.name, /*displayString*/ Config.name)
					}
					return options
				'''.stripIndent()
				orderIndex = '2'
				required = '1'
				type = 'select'
			}

			formalParameter 'Add Read+Execute Permissions', defaultValue: null, {
				expansionDeferred = '0'
				label = null
				orderIndex = '3'
				required = '0'
				type = 'header'
			}

			formalParameter 'Group', defaultValue: null, {
				expansionDeferred = '0'
				label = null
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					getGroups().each { Group ->
						options.add(/*value*/ Group.groupName, /*displayString*/ Group.groupName)
					}
					return options
				'''.stripIndent()
				orderIndex = '4'
				required = '0'
				type = 'select'
			}

			formalParameter 'User', defaultValue: null, {
				expansionDeferred = '0'
				label = null
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					getUsers().each { User ->
						options.add(/*value*/ User.userName, /*displayString*/ User.userName)
					}
					return options
				'''.stripIndent()
				orderIndex = '5'
				required = '0'
				type = 'select'
			}

			formalParameter 'Project', defaultValue: null, {
				expansionDeferred = '0'
				label = null
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					getProjects().each { Project ->
						options.add(/*value*/ Project.projectName, /*displayString*/ Project.projectName)
					}
					return options
				'''.stripIndent()
				orderIndex = '6'
				required = '0'
				type = 'select'
			}
			
			formalParameter 'Service', defaultValue: null, {
				expansionDeferred = '0'
				label = null
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					getServiceAccounts().each { Service ->
						options.add(/*value*/ Service.serviceAccountName, /*displayString*/ Service.serviceAccountName)
					}
					return options
				'''.stripIndent()
				orderIndex = '7'
				required = '0'
				type = 'select'
			}
		}
	}
}