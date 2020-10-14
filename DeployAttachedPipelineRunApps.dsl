/*

CloudBees CD DSL: Deploy only the applications with associated attached pipelines

CloudBees CD release models support deploying applications in bulk. However, it may be that for a particular release
run, you only want to run a subset of the applications that are configured for the release definition. This sample
project illustrates how to deploy just the applications for which there is an associated attached pipeline run. In this
example, the application name and pipeline definition names are the same. This example also shows how data from the
attached pipeline runs can be pulled into the release pipeline.

Instructions
0. Run this DSL (ectool evalDsl --dslFile DeployAttachedPipelineRunApps.dsl, or import and run from the DSLIDE)
1. Start the application pipelines
2. Start the release
3. Attach two of the pipeline runs to the release
4. Approve the release manual task
    - The "Deploy selected applications" task should have deployed the application associated with the pipeline you
        attached
    - Note that links to the attached pipeline runs are provided in first stage summary as is the feature list gathered
        from the attached pipelines.
5. You can attach or detach pipeline runs then restart the "Gather evidence task" to see the effect

*/
def Apps = ["App1","App2","App3"]

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
                stage "Build",{
                    task "Get feature list",{
                        taskType = 'COMMAND'
                        subpluginKey = 'EC-Core'
                        subprocedure = 'RunCommand'
                        actualParameter = [
                            commandToRun: '''\
                                ectool setProperty "/myPipelineRuntime/features" --value $[/javascript
                                        function onlyUnique(value, index, self) {
                                          return self.indexOf(value) === index;
                                        }
                                        var c = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ\'
                                        var key = c.charAt(Math.random() * c.length) + c.charAt(Math.random() * c.length) + c.charAt(Math.random() * c.length)
                                        var number = Math.trunc(Math.random() * 10000)
                                        var features = []
                                        for (i=0;i<Math.random() * 20;i++) \tfeatures.push(key + "-" + Math.trunc(number + Math.random() * 20))
                                        features.filter(onlyUnique).join(",")
                                    ]
                            '''.stripIndent()
                        ]
                    }
                }
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
                                commandToRun: '''\
                                    /*
                                    
                                    - Create stage summary links for the attached pipeline runs
                                    - Create a comma separted list of attached runtime pipeline IDs as a release pipeline runtime property
                                    
                                    */
                                    import com.electriccloud.client.groovy.ElectricFlow
                                    ElectricFlow ef = new ElectricFlow()
                                    def attachedPipelines = ef.getAttachedPipelineRuns(
                                            projectName: "$[/myPipelineRuntime/projectName]",
                                            releaseName: "$[/myRelease]"
                                        ).attachedPipelineRunDetail
                                    def flowRuntimeIds = []
                                    def features = []
                                    def attachedLink = "\\n\\n<html><ul>"
                                    attachedPipelines.each { attachedPipeline ->
                                      def pipelineId = ef.getProperty(
                                            flowRuntimeId: attachedPipeline.flowRuntimeId,
                                            propertyName: "/myPipeline/pipelineId"
                                        ).property.value
                                      features.addAll(ef.getProperty(
                                                    flowRuntimeId: attachedPipeline.flowRuntimeId,
                                                    propertyName: "/myPipelineRuntime/features"
                                                ).property.value.split(","))
                                      attachedLink += """<li><a href="#pipeline-run/${pipelineId}/${attachedPipeline.flowRuntimeId}">${attachedPipeline.flowRuntimeName}</a></li>"""
                                      flowRuntimeIds.push(attachedPipeline.flowRuntimeId)
                                    }
                                    attachedLink += "</ul></html>"
                                    
                                    ef.setProperty(
                                            propertyName: "/myPipelineRuntime/attachedPipelines",
                                            value: flowRuntimeIds.join(",")
                                        )
                                    ef.setProperty(
                                            propertyName: "/myStageRuntime/ec_summary/Attached Pipelines",
                                            value: attachedLink
                                        )
                                    ef.setProperty(
                                            propertyName: "/myStageRuntime/ec_summary/Features",
                                            value: features.join(",")
                                        )
                                '''.stripIndent(),
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
