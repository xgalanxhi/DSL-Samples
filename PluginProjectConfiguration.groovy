/*

CloudBees CD DSL: Plugin Project Configuration

This example illustrates how to script the creation a new (as of v10.3) plugin
configuration in a way not to expose the credential values and to save it to
an external project (outside of the plugin project).

To hide the credential values, they are passed in a parameter data structure
from the command line using environment variables:

ectool evalDsl --dslFile PluginProjectConfiguration.groovy --parameters "{\"pluginConfiguration\":\"Jira\",\"userName\":\"$JIRA_USER\",\"passWord\":\"$JIRA_TOKEN\",\"url\":\"$JIRA_URL\"}"

*/

def plugin = 'EC-JIRA'

def cred = (String) "${args.pluginConfiguration}-${plugin}"

project "Default",{
	credential cred,
		userName: args.userName,
		passWord: args.passWord
	pluginConfiguration args.pluginConfiguration, {
		credentialReferenceParameter = [
			credential: (String) "/projects/${projectName}/credentials/${cred}",
		]
		pluginKey = plugin
		field = [
			auth: 'basic',
			credential: 'credential',
			debugLevel: '0',
			ignoreSSLErrors: '0',
			url: args.url
		]

	}
}
