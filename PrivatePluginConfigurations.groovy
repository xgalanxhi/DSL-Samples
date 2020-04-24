/*

CloudBees CD (Flow) DSL: Service Catalog Item - Private Plugin Configurations

By default, if a principals (user, group, project, or service acccount) has access to a plugin configuration, it has access to all of the configurations for that plugin. This self service catalog (SSC) item can be used to restrict access to a limited set of principals. It accomplishes this be breaking inheritance on a selected plugin configuration and adds ACL entries for a set of principals. This SSC can be run multiple times to add more principals ACL entries.

Assuptions
- /plugins/${Plugin}/project/ec_config/configLocation points to the property sheet containing the configuration property sheets, where this is not true, there is a look up table for exceptions, currently for ECSCM and EC-MYSQL.

Known issues

Manual Steps to handle plugins that fail with this SSC
1. Ideally, log in as admin. If not, be very careful that you maintain Read-Modify permissions while making changes to to plugin configuration property sheets
2. Navigate to the configuration property sheet of interest (Platform Home page > Administration > Plugin > <top-level configuration property sheet>)
3. Click into the configuration property sheet
4. Select "Access Control"
5. Add Read-Execute access control entries for all of the principals desired
6. Select "Break Inheritance"

TODO
- Add ACL entries and break inheritance for the credential itself

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
							<p>By default, if a principals (user, group, project, or service acccount) has access to a plugin configuration, it has access to all of the configurations for that plugin. This self service catalog (SSC) item can be used to restrict access to a limited set of principals. It accomplishes this be breaking inheritance on a selected plugin configuration and adds ACL entries for a set of principals. This SSC can be run multiple times to add more principals ACL entries.
							
							<p>Allow time for the Configuration pull down to populate.
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
				
				def ConfigLocation = ["EC-MYSQL":"MYSQL_cfgs","ECSCM":"scm_cfgs"]
				def Location
				def ConfigId
				if (Plugin in ConfigLocation.keySet()) {
					ConfigId=getProperty("/plugins/${Plugin}/project/${ConfigLocation[Plugin]}/${Config}")?.propertySheetId
				} else {
					Location=getProperty("/plugins/${Plugin}/project/ec_config/configLocation")?.value
					ConfigId=getProperty("/plugins/${Plugin}/project/${Location}/${Config}")?.propertySheetId
				}
				
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
				label = "Plugin Name"
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def Plugins = getPlugins()
					def PluginKeys = []
					Plugins.each { Plugin ->
						if (getCredentials(projectName: Plugin.pluginName)?.credentialName) {
							PluginKeys.push(Plugin.pluginKey)
						}
					}
					PluginKeys.unique().each { MyPluginKey ->
						options.add(/*value*/ MyPluginKey, /*displayString*/ MyPluginKey)
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
				label = "Configuration Name"
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					/*
						When creating this SCC, the DSL evaluate will fail on the getProperty() below when ${Plugin} returns null. The ternary expression below ensures that ${Plugin} has a value at DSL evaluation time.
					*/
					def Plugin= args.parameters[\'Plugin\']?:"EC-Jenkins"
					def ConfigLocation = ["EC-MYSQL":"MYSQL_cfgs","ECSCM":"scm_cfgs"]
					def Location
					def ConfigsId
					if (Plugin in ConfigLocation.keySet()) {
						ConfigsId=getProperty("/plugins/${Plugin}/project/${ConfigLocation[Plugin]}")?.propertySheetId
					} else {
						Location=getProperty("/plugins/${Plugin}/project/ec_config/configLocation")?.value
						ConfigsId=getProperty("/plugins/${Plugin}/project/${Location}")?.propertySheetId
					}
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
				label = "Group Name"
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
				label = "User Name"
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
				label = "Project Name"
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
				label = "Service Account Name"
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