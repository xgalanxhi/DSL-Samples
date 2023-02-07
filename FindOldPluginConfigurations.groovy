/*

Find old plugin configurations
Run this DSL in the DSL IDE


*/


//def proj = [projectName: "BiogenTester-1.0.0.0",pluginKey: "BiogenTester"]
def confs = []
getProjects().each { proj ->
  if (proj.pluginKey) {
    def ec_config = getProperty(projectName: proj.projectName, propertyName: "ec_config")?true:false
    if (ec_config) {
      def configLocation = getProperty(projectName: proj.projectName, propertyName: "ec_config/configLocation")?.value
      if (configLocation) if (configLocation && getProperty(projectName: proj.projectName, propertyName: configLocation)) {
        getProperties(projectName: proj.projectName, propertyName: configLocation).property.each { conf ->
          confs.add([(proj.pluginKey):conf.name])
        }
      }
    }
  }
}
confs
