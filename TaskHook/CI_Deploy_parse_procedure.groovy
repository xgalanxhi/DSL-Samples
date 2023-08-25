/*

CloudBees CD/RO ec-groovy command step code

This code can be added as a task hook to a CloudBees CD/RO pipeline task of
type "CI job" and is is set to Deploy. This code will parse the Jenkins log
and if it includes a Maven style artifact version (group:name:version),
this value will be pushed the the environment inventory.

See https://docs.beescloud.com/docs/cloudbees-cd/latest/deploy-automation/third-party-deployment for details on the deployment task.

*/

import groovy.json.JsonOutput

import com.electriccloud.client.groovy.ElectricFlow

ElectricFlow ef = new ElectricFlow()

def logFileName = '$[/myJob/steps/Get CI Build Details/logFileName]'

// See https://docs.beescloud.com/docs/cloudbees-cd/latest/deploy-automation/third-party-deployment#_use_the_third_party_deployment_response_for_inventory_and_analytics_updates for details on this data structure and :
def data=[
	"deploymentSystem": "TaskHookTest",
	"deploymentUrl": "https://github.com/electric-cloud-community/BringYourOwnDeployerPlugin"
]

File logFile = new File(logFileName)

if (!logFile.exists()) {
println "File does not exist"

}

def artifacts = []
logFile.eachLine { line ->

if (match = line =~ /Deploying ([\w-_.]+:)?([\w-_.]+):([\w-_.]+).*/) {

def artifactNamePart1 = match.group(1)
def artifactNamePart2 = match.group(2)
def artifactName = artifactNamePart1? artifactNamePart1 + artifactNamePart2 :artifactNamePart2
def artifactData = [
	"artifactName" : artifactName,
	"artifactVersion" : match.group(3)
]
artifacts.add(artifactData)

}
}
data << ["artifacts": artifacts]

ef.setProperty(propertyName: "/myJob/ec_deployment_artifacts", value: JsonOutput.prettyPrint(JsonOutput.toJson(data)))
