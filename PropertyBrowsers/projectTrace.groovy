procedure 'projectTrace', {
  projectName = 'MyProject'
  timeLimit = '0'

  formalParameter 'userProjectName', defaultValue: 'GenerateReport', {
    label = 'Project Name'
    orderIndex = '1'
    required = '1'
    type = 'entry'
  }

  step 'trace', {
    command = '''import groovy.json.*
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*

/***************************************************************************************/
def printProperties( path ) {
	ElectricFlow ef = new ElectricFlow()
    try {
	  PropertyStucture = ef.getProperties(path: path, recurse: true, expand: false).propertySheet
      propList = PropertyStucture.property
      for (myProperty in propList) {
          if ( myProperty.propertySheet != null ) {
              //println ">>> property sheet " + myProperty.propertyName
              printProperties( path + "/" + myProperty.propertyName )
              //println "<<< property sheet " + myProperty.propertyName
          } else {
              println path + "/" + myProperty.propertyName + " = " + myProperty.value
          }
      }
    } catch( Exception ex ) {
    	println myProperty.propertyName + " = NOT FOUND"
    }
}

/***************************************************************************************/
def printObj( basePath, propList ) {
    if ( propList instanceof org.apache.groovy.json.internal.LazyMap ) {
      propList.each{ key, value -> 
        if ( value instanceof java.lang.String ) {
            println basePath + "/" + key + " = " + value
        } else {
            //println basePath + "/" + key + " Class = " + value.getClass()
            printObj( basePath + "/" + key, value )
        }
      }
    }
    /*
    if ( propList instanceof java.util.ArrayList ) {
        println "==== ARRAYLIST ====="
    	propList.each{ it ->
        	println basePath + " = " + it
            printObj( basePath, it )
        }
        println "==== ARRAYLIST ====="
    }
    */
}

/***************************************************************************************/
def printEnvironment( projectName, environmentName, basePath ) {
	printProperties( basePath )
    ElectricFlow ef = new ElectricFlow()
    def envResults = ef.getEnvironment(
    	projectName: projectName,
        environmentName: environmentName )
    //println JsonOutput.prettyPrint( JsonOutput.toJson( envResults ) )
    printObj( basePath, envResults.environment )
}
/***************************************************************************************
 ***************************************************************************************
 ***************************************************************************************
 ***
 ***
 ***                                M A I N
 ***
 ***
 ***************************************************************************************
 ***************************************************************************************
 ***************************************************************************************/
userProjectName = \'$[/myJob/userProjectName]\'
println "Project Name: " + userProjectName
/*****************************************************************
 *    Get Environment Object
 */
println "*****************************************************************"
println "*****************************************************************"
basePath="/myProject"
println ">>> " + basePath
printProperties( basePath )
basePath="/projects/" + userProjectName
println ">>> " + basePath
printProperties( basePath )
println "*****************************************************************"
println "*****************************************************************"
'''
    shell = 'ec-groovy'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }

  // Custom properties

  property 'ec_customEditorData', {

    // Custom properties

    property 'parameters', {
      propertyType = 'sheet'
    }
  }
}