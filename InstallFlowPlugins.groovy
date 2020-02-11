/*

	CloudBees Flow DSL: Install Plugins from PluginsToInstall list below
	
	ectool evalDsl --dslFile InstallFlowPlugins.groovy --parameters '{"FlowUrl":"https://flow.example.com"}'
	
	Where flow.example.com is the actual Flow URL


*/

import groovy.util.XmlParser

def PluginsToInstall = [
	"unplug",
	"EC-AuditReports",
	"EC-DSLIDE",
	"EC-Slack"
]

def FlowUrl = args.FlowUrl

// Get currently installed and promoted plugins
def InstalledPlugins = []
def PromotedPlugins = []
getPlugins().each { Plugin ->
	InstalledPlugins.push(Plugin.pluginKey)
	if (Plugin.promoted) {
		PromotedPlugins.push(Plugin.pluginName)
	}
}

// Get list of target plugins that aren't installed
def PluginsToInstallPruned = []
PluginsToInstall.each { PluginToInstall ->
	if (! (PluginToInstall in InstalledPlugins)) {
		PluginsToInstallPruned.push(PluginToInstall)
	}
}

// Get Catalog details
def CatalogUrl = getProperty("/myProject/catalogUrl", pluginName: "EC-PluginManager").value
def CatalogXml = new URL(CatalogUrl).getText()
def plugins = new XmlParser().parseText(CatalogXml)

// Create procedure to install and promote plugins
project "Default", {
	procedure "Install Plugins", {
	PluginsToInstallPruned.each { Plugin ->
		def version
		def url
		plugins.plugin.each { plugin ->
			if (plugin["pluginName"].text()==Plugin) {
				version = plugin["pluginVersion"].text()
				url = plugin["downloadUrl"].text()
				return true // break out of the loop
			}
		}
		println "Creating steps to install ${Plugin}"
		step "Install ${Plugin}", 
			command : "ectool installPlugin ${url}"
		step "Promote ${Plugin}",
			command: "ectool promotePlugin ${Plugin}-${version} --promoted 1"
		}
	}	
}

def JobId = runProcedure(projectName: "Default", procedureName: "Install Plugins").jobId
println "Install Plugin Job: ${FlowUrl}/commander/link/jobDetails/jobs/${JobId}"
