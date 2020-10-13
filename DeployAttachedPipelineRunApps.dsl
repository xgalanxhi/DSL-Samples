/*

CloudBees CD DSL: Deploy only the applications with associated attached pipelines

CloudBees CD release models support deploying applications in bulk. However, it may be that for a particular release
run, you only want to run a subset of the applications that are configured for the release definition. This sample
project illustrates how to deploy just the applications for which there is an associated attached pipeline run. In this
example, the application name and pipeline definition names are the same.

Instructions
0. Run this DSL (ectool evalDsl --dslFile DeployAttachedPipelineRunApps.dsl, or import and run from the DSLIDE)
1. Start the application pipelines
2. Start the release
3. Attach one of the pipeline runs to the release
4. Approve the release manual task

The "Deploy selected applications" task should have deployed the application associated with the pipeline you attached

*/
def Apps = ["App1","App2"]

project "Deploy Attached", {
    environment "QA",{
        environmentTier "App",{
            resource "${projectName}_${environmentName}_${environmentTierName}",
                hostName: getResource(resourceName: "local").hostName
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
            }
            pipeline App,{
                stage "Build"
                stage "Testing"
                stage "Join Release",{
                    gate 'PRE', {
                        task 'Wait', {
                            gateCondition = 'true'
                            gateType = 'PRE'
                            precondition = 'false'
                            taskType = 'CONDITIONAL'
                        }
                    }
                }
            }
            release "Sample Release",{
                pipeline "Release Pipeline",{
                    stage "QA",{
                        task 'Attach Pipeline Runs', {
                            instruction = 'Attach desired pipeline runs'
                            taskType = 'MANUAL'
                            approver = [getProperty(propertyName: "/myUser/userName").value]
                        }
                        task 'Gather evidence',{
                            taskType = 'COMMAND'
                            subpluginKey = 'EC-Core'
                            subprocedure = 'RunCommand'
                            actualParameter = [
                                commandToRun: 'echo Gathering evidence from development pipeline',
                            ]
                        }
                        task 'Deploy selected applications', {
                            taskType = 'DEPLOYER'
                            deployerExpression = '''\
                                    var items=[];
                                    var attachedRuns = api.getAttachedPipelineRuns(
                                        {projectName: myPipelineRuntime.projectName,
                                        releaseName: myPipelineRuntime.releaseName}
                                        ).attachedPipelineRunDetail
                                    if (typeof(attachedRuns)!=='undefined') for (i=0; i < attachedRuns.length; i++) {
                                        var attached = api.getProperty(
                                                {flowRuntimeId: attachedRuns[i].flowRuntimeId,
                                                propertyName: "/myPipelineRuntime/pipelineName"}
                                            ).property.value
                                        for(var j=0;j<itemList.length;j++){
                                          var item = itemList[j];
                                          if (item == attached) items.push(item);
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
            }
        }
    }
}
