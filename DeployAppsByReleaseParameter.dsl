/*

CloudBees CD DSL: Deploy parameter-selected applications attached to a Release

CloudBees CD release models support deploying applications in bulk. However, it may be that for a particular release
run, you only want to run a subset of the applications that are configured for the release definition. This sample
project illustrates how to deploy based on check box parameters.

Instructions
------------
0. Run this DSL (ectool evalDsl --dslFile DeployAppsByReleaseParameter.dsl, or import and run from the DSLIDE)
1. Start the release and select the applications to be deployed. Note that that an additional (unused) parameter is
	revealed when a particular application parameter is toggled on. This illustrates the use of parameter render
	conditions.

The "Deploy selected applications" task should have deployed the application associated with the pipeline you attached

Deployer Condition development tips
-----------------------------------
It is possible to debug the Deployer Condition code by running and rerunning the Deployer task in a pipeline. For complex
you code, might consider an IDE or an on-line tool like https://www.webtoolkitonline.com/javascript-tester.html to do your
development. If you go this route, you'll need to provide the runtime values that you plan to use:

applicationList – An array of applications added to the release
serviceList – An array of services added to the release
itemList – An array of applications and services added to the release
deployerTask - Current task
myPipelineRuntime.actualParameters - Pipeline parameters

And, you'll want to use JSON.stringify instead of "return" to view the results. Here is a sample testbed for the
Deployer Condition used in this example:

// Sample runtime values
var itemList=["App1","App2","App3"]
var myPipelineRuntime={
    "actualParameters":{
        "App1":"true",
        "App1_arg":"ABC",
        "App2":"false",
        "App2_arg":"",
        "App3":"true",
        "App3_arg":"123",
    }
}
// The code under development
var items = [];
for(Apps=0;Apps<itemList.length;Apps++){
	var item= itemList[Apps];
	if (myPipelineRuntime.actualParameters[item] == 'true') items.push(item)
}
//return items; // Remember to uncomment this

// View the object
JSON.stringify(items)


*/
def Apps = ["App1","App2","App3"]

project "Deploy Selected", {
    environment "QA", {
        environmentTier "App", {
            resource "${projectName}_${environmentName}_${environmentTierName}",
                    hostName: getResource(resourceName: "local").hostName
        }
    }
    Apps.each { App ->
        application App, {
            applicationTier "App"
            tierMap 'QA', {
                applicationName = applicationName
                environmentName = 'QA'
                environmentProjectName = projectName
                tierMapping 'App-App', {
                    applicationTierName = 'App'
                    environmentTierName = 'App'
                }
            }
            process "Deploy",{
                processStep 'NOP', {
                    actualParameter = [
                        commandToRun: 'echo',
                    ]
                    applicationTierName = 'App'
                    processStepType = 'command'
                    subprocedure = 'RunCommand'
                    subproject = '/plugins/EC-Core/project'
                }
            }
        } // application
        release "Sample Release",{
            pipeline "Release Pipeline",{
                formalParameter App,{
                    defaultValue = 'false'
                    checkedValue = 'true'
                    type = 'checkbox'
                    uncheckedValue = 'false'
                }
				formalParameter "${App}_arg", defaultValue: '', {
					dependsOn = App
					renderCondition = "\${${App}}=='true'"
				}				
                stage "QA",{
                    task 'Deploy selected applications', {
                        taskType = 'DEPLOYER'
                        deployerExpression = '''\
							var items = [];
							for(Apps=0;Apps<itemList.length;Apps++){
								var item= itemList[Apps];
								if (myPipelineRuntime.actualParameters[item] == 'true') items.push(item)
							}
							return items;
                        '''.stripIndent()
                    }
                }
                stage "Staging"
                stage "Production"
            }
            deployerApplication App, {
                processName = 'Deploy'
                deployerConfiguration 'QA', {
                    deployerTaskName = 'Deploy selected applications'
                    environmentName = 'QA'
                    processName = 'Deploy'
                    stageName = "QA"
                }
            }
        } // release
    } // Apps.each
} // project
