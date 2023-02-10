/*

Find old plugin configurations
Run this DSL in the DSL IDE
Out put is a tab-delimited list of plugin names and configuration names; this can be cut and pasted to a spreadsheet


*/


def confs='Configuration\tPlugin\tSupport\n'
getProjects().each { proj ->
  if (proj.pluginKey) {
    def ec_config = getProperty(projectName: proj.projectName, propertyName: "ec_config")?true:false
    if (ec_config) {
      def configLocation = getProperty(projectName: proj.projectName, propertyName: "ec_config/configLocation")?.value
      if (configLocation) if (configLocation && getProperty(projectName: proj.projectName, propertyName: configLocation)) {
        def plug = getPlugin(pluginName: proj.pluginKey)
        getProperties(projectName: proj.projectName, propertyName: configLocation).property.each { conf ->
          confs +=  conf.name + '\t' + proj.pluginKey + '\t'  +  plug.supportedBy + '\n'
        }
      }
    }
  }
}
confs
