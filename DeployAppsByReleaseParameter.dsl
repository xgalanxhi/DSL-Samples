/*

CloudBees CD DSL: Deploy parameter-selected applications attached to a Release

CloudBees CD release models support deploying applications in bulk. However, it may be that for a particular release
run, you only want to run a subset of the applications that are configured for the release definition. This sample
project illustrates how to deploy based on check box parameters.

Instructions
0. Run this DSL (ectool evalDsl --dslFile DeployAppsByReleaseParameter.dsl, or import and run from the DSLIDE)
1. Start the release and select the applications to be deployed. Note that that an additional (unused) parameter is
	revealed when a particular application parameter is toggled on. This illustrates the use of parameter render
	conditions.

The "Deploy selected applications" task should have deployed the application associated with the pipeline you attached

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
                            var props = api.getActualParameters(
                                {flowRuntimeId: myPipelineRuntime.flowRuntimeId}
                                ).actualParameter;
                            for(i=0; i<props.length; i++){
                                if(props[i].value=='true') {
                                    var appName = props[i].actualParameterName;
                                    for(j=0;j<itemList.length;j++){
                                        var item= itemList[j];
                                        if (appName == item) items.push(item)
                                    }
                                }
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
