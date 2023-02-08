/*

CloudBees CDRO DSL: SSC Item to attach ACLs to Plugin Configuration

Instructions
1. Apply this DSL to create the catalog item
	ectool evalDsl --dslFile  AttachAclsToPluginConfiguration.groovy
2. Run the Catalog Item DSL-Samples :: Administration :: Apply ACLs to Plugin Config
	- Choose the plugin (only plugins with configurations will be displayed)
	- Choose the configuration
	- Optionally choose a group, user and/or project to give read/execute privileges
	- Run
3. Examine the configuration access control list in the UI

This example exercises
- Dependent parameters
- Filtering down list of objects
- Plugin configuration ACLs

*/

project "DSL-Samples",{
	catalog 'Administration', {
		iconUrl = null
		catalogItem 'Apply ACLs to Plugin Config', {
			description = '''\
				<xml>
					<title>
						Make plugin credentials visible and usable by a select set of groups, users and projects
					</title>

					<htmlData>
						<![CDATA[
							
						]]>
					</htmlData>
				</xml>
			'''.stripIndent()
			buttonLabel = 'Execute'
			iconUrl = 'icon-catalog-item.svg'
			useFormalParameter = '1'
			formalParameter 'Plugin', {
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					getPlugins().each {
						if (getPluginConfigurations(pluginKey: it.pluginKey).size()>0) 		options.add((String) it.pluginKey,(String) it.pluginKey)
					}
					return options
				'''.stripIndent()
				orderIndex = '1'
				required = '1'
				type = 'select'
			}
			formalParameter 'Config', {
				dependsOn = 'Plugin'
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def Plugin= args.parameters['Plugin']
					if (Plugin) {
						def Configs = getPluginConfigurations(pluginKey: Plugin)
						Configs.each {
							options.add((String) it.name,(String) it.name)
						}
					}
					return options
				'''.stripIndent()
				orderIndex = '2'
				required = '1'
				type = 'select'
			}

			formalParameter 'Add Read+Execute Permissions',{
				orderIndex = '3'
				type = 'header'
			}

			formalParameter 'Group',{
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					getGroups().each {
						options.add((String) it.groupName,(String) it.groupName)
					}
					return options
				'''.stripIndent()
				orderIndex = '4'
				type = 'select'
			}

			formalParameter 'User', {
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					getUsers().each {
						options.add((String) it.userName,(String) it.userName)
					}
					return options
				'''.stripIndent()
				orderIndex = '5'
				type = 'select'
			}
			formalParameter 'Project',{
				orderIndex = '6'
				type = 'project'
			} // formalParameter
			
			dslString='''\
				def setAcls(principal, type, plugin, config, pluginProject) {
					aclEntry principalName : principal,
						principalType : type,
						objectType: "pluginConfiguration",
						pluginConfigurationName: config,
						pluginName : plugin,
						projectName : pluginProject,
						readPrivilege : 'allow',
						modifyPrivilege : 'inherit',
						executePrivilege : 'allow',
						changePermissionsPrivilege : 'inherit'
				}
				def getPluginConfigurationProject(pluginKeyName,pluginConfigurationName) {
					def proj=null
						getPlugins().each { pkey ->
						if (pkey.pluginKey==pluginKeyName) getPluginConfigurations(pluginKey:pluginKeyName).each { confs ->
							if (confs.pluginConfigurationName==pluginConfigurationName) proj=confs.projectName
						}
					}
					return proj
				}
				def Project = args['Project']
				def User = args['User']
				def Group = args['Group']
				def Config = args['Config']
				def Plugin = args['Plugin']
				def PluginProject = getPluginConfigurationProject(Plugin,Config)
				if (Project) setAcls ("project: ${Project}","user",Plugin,Config,PluginProject)
				if (User)    setAcls (User,"user",Plugin,Config,PluginProject)
				if (Group)   setAcls (Group,"group",Plugin,Config,PluginProject)
			
			'''.stripIndent()
			
			
		} // catalogItem
	} // Catalog
} // Project