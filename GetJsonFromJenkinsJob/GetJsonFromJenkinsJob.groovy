/*
 Copyright 2024 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------


ElectricFlow DSL Example: load and parse a json file from Jenkins into CDRO

Instructions:
1. Run this DSL code from the command line, DSL Editor or DSLIDE
		Command line: ectool evalDsl --dslFile GetJsonFromJenkinsJob.groovy
2. Run the procedure 

*/

def CurrentProject = 'dslsamples'

procedure 'GetJsonFromJenkinsJob', {
  projectName =  CurrentProject
  timeLimit = '0'

  formalParameter 'JenkinsConfig', {
    orderIndex = '1'
    propertyReference = 'EC-Jenkins'
    required = '1'
    type = 'pluginConfiguration'
  }

  formalParameter 'JenkinsJob', defaultValue: 'TestJob1', {
    orderIndex = '2'
    required = '1'
    type = 'entry'
  }

  formalParameter 'JenkinsArtifact', defaultValue: 'test.json', {
    orderIndex = '3'
    required = '1'
    type = 'entry'
  }

  step 'Transform Jenkins Job Path', {
    description = 'Fix string to append /job/ delimiter between folder paths within Jenkins job path.'
    command = '''import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*


ElectricFlow ef = new ElectricFlow()

job_name= \'$[JenkinsJob]\'

def folders = job_name.split(\'/\')
  // Remove empty elements
    folders = folders.findAll { it }

    // Initialize the updated path
    def updatedPath = ""

    // Iterate through folders and build the updated path
    for (int i = 0; i < folders.size(); i++) {
        // Skip iteration if folder name is "job"
        if (folders[i] == "job") {
            continue
        }

        // Add folder to the updated path
        updatedPath += (i > 0 ? "/job/" : "") + folders[i]
  }

ef.setProperty(
                propertyName: \'/myJob/updated_path\',
                value: updatedPath )
'''
    procedureName = 'GetJsonFromJenkinsJob'
    shell = 'ec-groovy'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
    workspaceName = 'default'
  }

  step 'Get Artifact From Jenkins', {
    actualParameter = [
      'artifacts': '$[JenkinsArtifact]',
      'build_number': '',
      'config_name': '$[JenkinsConfig]',
      'job_name': '$[JenkinsJob]',
      'target_directory': '',
    ]
    procedureName = 'GetJsonFromJenkinsJob'
    subprocedure = 'DownloadArtifacts'
    subproject = '/plugins/EC-Jenkins/project'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }



  step 'Get JSON Data', {
    command = '''import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*


ElectricFlow ef = new ElectricFlow()

File file = new File("$[JenkinsArtifact]")
def String jsonArtifact = file.getText()
println( jsonArtifact )
ef.setProperty(
                propertyName: \'/myJob/jsonText\',
                value: jsonArtifact )'''
    procedureName = 'GetJsonFromJenkinsJob'
    shell = 'ec-groovy'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }

  step 'json2properties', {
    actualParameter = [
      'json': '$[/myJob/jsonText]',
      'startingpath': '/myJob/expandedJson',
    ]
    procedureName = 'GetJsonFromJenkinsJob'
    subprocedure = 'json2properties'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }
}