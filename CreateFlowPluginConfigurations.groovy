/*
    Create Flow Plugin configurations
	
	Usage: ectool evalDsl --dslFile CreateFlowPluginConfigurations.groovy --parameters '{"PluginConfigFilePath":"PluginConfigs.json"}'
	
	Where PluginConfigFilePath references a path on the Flow Server
	
	PluginConfigs.json format:
		[
			{
				"ConfigName": "A config name",
				"PluginName": "EC-PluginName",
				"Username": "a user",
				"Password": "a password",
				"Parameters": {
					"aParam": "a parameter value",
					"anotherParam": "Another param value"
				}
			},
			{
				"ConfigName": "Another config name",
				"PluginName": "EC-PluginName2",
				"Username": "another user",
				"Password": "another password",
				"Parameters": {
					"bParam": "b parameter value",
					"bnotherParam": "Bnother param value"
				}
			}
		]
	Where the parameter names come from the plugins's CreateConfiguration procedure
	
*/
import groovy.json.*

def PluginConfigFilePath = args.PluginConfigFilePath
def PluginConfigsJson = new File(PluginConfigFilePath).text
def jsonSlurper = new JsonSlurper()
def PluginConfigs = jsonSlurper.parseText(PluginConfigsJson)

PluginConfigs.each { conf ->

	def proj="/plugins/${conf.PluginName}/project"
	def confName = conf.ConfigName
	def uName= conf.Username
	def pwd= conf.Password

	def ConfigPropertySheet = getProperty(propertyName: "ec_config/configLocation", projectName: proj).value
	// Create a list of existing configurations in this plugin project
	def ExistingConfigs = []
	getProperties(propertySheetId: getProperty(ConfigPropertySheet, projectName: proj).propertySheetId).property.each {
		ExistingConfigs.push(it.name)
	}

	// Create a Transient credential
	def Cred = new RuntimeCredentialImpl()
	Cred.name = confName       
	Cred.userName = uName		
	Cred.password = pwd
	def Creds=[Cred]

	def Params = [ config:confName, credential: Cred.name] + conf.Parameters
	
	if (! (confName in ExistingConfigs)) {
		// Create a new configuration by running the plugin create configuration procedure
		runProcedure(
			projectName : proj,
			procedureName : "CreateConfiguration",
			actualParameter : Params,
			credential: Creds
		)
	} else {
	/*
		// Update the configuration
		// overwrite the  credential
		credential(
			projectName: proj,
			userNane: uName
			password: pwd
			credentialName: confName
		)
		// overtrite properties
		conf.Parameters.each { param, val ->
			property "${ConfigPropertySheet}/${confName}/${param}", projectName: proj, value: val
		}
	*/
	}
} // Each conf
