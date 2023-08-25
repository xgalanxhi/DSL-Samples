/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------

Change the `CurrentProject` to the target project where  you want this procedure installed.

*/
def CurrentProject = 'dslsamples'

procedure 'pipelineTrace', {
  projectName = CurrentProject
  timeLimit = '0'

  formalParameter 'basePath', defaultValue: 'myPipeline', {
    label = 'Base Path'
    options = [
      'myPipeline': 'myPipeline',
      'myRelease': 'myRelease',
    ]
    orderIndex = '1'
    required = '1'
    type = 'select'
  }

  formalParameter 'pipelineName', defaultValue: 'javascript', {
    label = 'Pipeline Name'
    orderIndex = '2'
    required = '1'
    type = 'entry'
  }

  formalParameter 'myProjectName', defaultValue: '', {
    expansionDeferred = '0'
    label = 'Project Name'
    orderIndex = '3'
    required = '1'
    type = 'project'
  }

  step 'trace', {
    command = '''import groovy.json.*
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*

/***************************************************************************************/
def printProperties( path ) {
	ElectricFlow ef = new ElectricFlow()
	PropertyStucture = ef.getProperties(path: path, recurse: true, expand: false).propertySheet
    try {
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
def printTasks( basePath, stageName, taskList, projectName, pipelineName ) {
    ElectricFlow ef = new ElectricFlow()
    try {
    for (tasks in taskList) {
        thisTask = basePath + "/tasks/" + tasks.taskName

		def taskResult = ef.getTask(
                projectName: projectName,
                pipelineName: pipelineName,
                taskName: tasks.taskName,
                stageName: stageName)
        printObj( thisTask, taskResult.task )
		if (tasks.taskType == "GROUP") {
            groupTaskList = tasks.task
            printTasks( thisTask, stageName, groupTaskList, projectName, pipelineName )
        } else {
          printProperties( thisTask )
          try {
	          thisJob = thisTask + "/job"
    	      printProperties( thisJob )
          } catch( Exception ex ) {
          	//println "No Job Data"
          }
        }
    }

  } catch(Exception ex) {
      //println "Problem getting task data " + ex
  }
}
/***************************************************************************************/
def printStages( basePath, stageList, projectName, pipelineName ) {
  ElectricFlow ef = new ElectricFlow()
  for (stages in stageList) {
       try {
          thisStage = basePath + "/stages/" + stages.stageName
          stage = ef.getProperty( propertyName: basePath + "/stages/" + stages.stageName ).property.value
          def stageResult = ef.getStage(
                          stageName:  stages.stageName,
                          projectName: projectName,
                          pipelineName: pipelineName)

		  //println JsonOutput.prettyPrint(JsonOutput.toJson(stageResult))
		  printObj( thisStage, stageResult.stage )
		  def taskResult = ef.getTasks(
                  stageName:  stages.stageName,
                  projectName: projectName,
                  pipelineName: pipelineName)

          taskList = taskResult.task
          taskPath = basePath + "/stages/" + stages.stageName 
          printTasks( taskPath, stages.stageName, taskList, projectName, pipelineName )
      } catch(Exception ex) {
          //println "Problem getting stage data " + ex
      }
  }
}

/***************************************************************************************/
def printPipeline( projectName, pipelineName, basePath ) {
  ElectricFlow ef = new ElectricFlow()
  def pipelinResult = ef.getPipeline(
                  pipelineName: pipelineName,
                  projectName: projectName)

  //println JsonOutput.prettyPrint(JsonOutput.toJson(pipelinResult))
  printObj( basePath, pipelinResult.pipeline )
  def stagesResult = ef.getStages(
                  projectName: projectName,
                  pipelineName: pipelineName)
  printProperties( basePath )

  def stageList = stagesResult.stage
  printStages( basePath, stageList, projectName, pipelineName )
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
pipelineName = \'$[pipelineName]\'
projectName = \'$[myProjectName]\'
println "Project Name: " + projectName
println "Pipeline Name: " + pipelineName
/*****************************************************************
 *    Get Pipeline Object
 */
println "*****************************************************************"
println "*****************************************************************"
basePath="/myPipelineRuntime"
printPipeline( projectName, pipelineName, basePath )
basePath="/myPipeline"
printPipeline( projectName, pipelineName, basePath )
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

      // Custom properties

      property 'basePath', {

        // Custom properties

        property 'options', {

          // Custom properties

          property 'option1', {

            // Custom properties

            property 'text', value: 'myPipeline'

            property 'value', value: 'myPipeline'
          }

          property 'option2', {

            // Custom properties

            property 'text', value: 'myRelease'

            property 'value', value: 'myRelease'
          }
          optionCount = '2'

          property 'type', value: 'list'
        }
        formType = 'standard'
      }
    }
  }
}