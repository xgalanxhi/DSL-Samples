/*

CloudBees CDRO DSL: Find plugins used in pipelines

Instructions
1. Apply this DSL: ectool evalDsl --dslFile PluginUsage.groovy
2. Run the procedure DSL-Samples::Plugin usage
3. Examine procedure step log

*/


project "DSL-Samples",{
	procedure "Plugin usage",{
		step "Find usage", shell: "ec-groovy", command: '''\

import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.Filter
import com.electriccloud.client.groovy.models.Select
import com.electriccloud.client.groovy.models.Sort

ElectricFlow ef = new ElectricFlow()

Filter filter = new Filter('taskType', 'equals', 'PLUGIN')
//Select select = new Select('taskType')
//Sort sort = new Sort('taskType', 'ascending')

result = ef.findObjects(
        objectType: 'task',
        filters: [filter],
        //selects: [select],
        //sorts: [sort]
)

/* Sample "result"

[objectId:[task-e9d2d047-2489-11ed-bce3-0a0027000004, task-aed7ccd5-f7f1-11ec-9ff4-0a0027000003, task-8245a413-3daa-11ed-9d88-0a0027000004, task-82649dfb-3daa-11ed-9d88-0a0027000004], object:[[objectId:task-e9d2d047-2489-11ed-bce3-0a0027000004, task:[taskId:e9d2d047-2489-11ed-bce3-0a0027000004, taskName:aab, advancedMode:0, allowCurrentUserToApprove:0, allowOutOfOrderRun:0, alwaysRun:0, autoRetryInProgress:0, createTime:2022-08-25T15:23:47.003Z, deployerTask:0, errorHandling:stopOnError, flowStateName:e9d2d048-2489-11ed-bce3-0a0027000004_task_flow_state, gateName:af1f0f36-f7f1-11ec-9ff4-0a0027000003, hasWaitDependencies:0, index:1, lastModifiedBy:admin, modifyTime:2022-08-25T15:24:07.643Z, owner:admin, parentFlowName:af1f0f36-f7f1-11ec-9ff4-0a0027000003_pregateflow, propertySheetId:e9d3baae-2489-11ed-bce3-0a0027000004, requiresDefinition:0, resourceName:, subpluginKey:EC-Jenkins, subprocedure:CollectReportingData, taskCount:0, taskEnabled:1, taskSkippable:0, taskType:PLUGIN, tracked:1, useApproverAcl:0, waitForPlannedStartDate:0, waitingForDependency:0, waitingForManualRetry:0, waitingForPlannedStartDate:0, waitingForPrecondition:0, waitingOnManual:0]], [objectId:task-aed7ccd5-f7f1-11ec-9ff4-0a0027000003, task:[taskId:aed7ccd5-f7f1-11ec-9ff4-0a0027000003, taskName:Build, advancedMode:0, allowCurrentUserToApprove:0, allowOutOfOrderRun:0, alwaysRun:0, autoRetryInProgress:0, createTime:2022-06-29T21:23:12.038Z, deployerTask:0, description:, errorHandling:stopOnError, flowStateName:aed7ccd6-f7f1-11ec-9ff4-0a0027000003_task_flow_state, hasWaitDependencies:0, index:1, lastModifiedBy:admin, modifyTime:2022-08-25T15:57:45.350Z, owner:admin, parentFlowName:ae7f4d5d-f7f1-11ec-9ff4-0a0027000003_stageflow, propertySheetId:aed7ccdc-f7f1-11ec-9ff4-0a0027000003, requiresDefinition:0, resourceName:, stageId:ae7f4d5d-f7f1-11ec-9ff4-0a0027000003, stageName:Dev, subpluginKey:EC-Git, subprocedure:Commit, taskCount:0, taskEnabled:1, taskSkippable:0, taskType:PLUGIN, tracked:1, useApproverAcl:0, waitForPlannedStartDate:0, waitingForDependency:0, waitingForManualRetry:0, waitingForPlannedStartDate:0, waitingForPrecondition:0, waitingOnManual:0]], [objectId:task-8245a413-3daa-11ed-9d88-0a0027000004, task:[taskId:8245a413-3daa-11ed-9d88-0a0027000004, taskName:Build, advancedMode:0, allowCurrentUserToApprove:0, allowOutOfOrderRun:0, alwaysRun:0, autoRetryInProgress:0, createTime:2022-09-26T14:50:03.989Z, deployerTask:0, description:, errorHandling:stopOnError, flowStateName:aed7ccd6-f7f1-11ec-9ff4-0a0027000003_task_flow_state, hasWaitDependencies:0, index:0, lastModifiedBy:admin, modifyTime:2022-09-26T14:50:03.989Z, owner:admin, parentFlowName:8225bfd7-3daa-11ed-9d88-0a0027000004_stageflow, propertySheetId:82479fe5-3daa-11ed-9d88-0a0027000004, releaseName:Based on pipeline template, requiresDefinition:0, resourceName:, stageId:8225bfd7-3daa-11ed-9d88-0a0027000004, stageName:Dev, subpluginKey:EC-Git, subprocedure:Commit, taskCount:0, taskEnabled:1, taskSkippable:0, taskType:PLUGIN, tracked:1, useApproverAcl:0, waitForPlannedStartDate:0, waitingForDependency:0, waitingForManualRetry:0, waitingForPlannedStartDate:0, waitingForPrecondition:0, waitingOnManual:0]], [objectId:task-82649dfb-3daa-11ed-9d88-0a0027000004, task:[taskId:82649dfb-3daa-11ed-9d88-0a0027000004, taskName:aab, advancedMode:0, allowCurrentUserToApprove:0, allowOutOfOrderRun:0, alwaysRun:0, autoRetryInProgress:0, createTime:2022-09-26T14:50:03.989Z, deployerTask:0, errorHandling:stopOnError, flowStateName:e9d2d048-2489-11ed-bce3-0a0027000004_task_flow_state, gateName:825b00f7-3daa-11ed-9d88-0a0027000004, hasWaitDependencies:0, index:1, lastModifiedBy:admin, modifyTime:2022-09-26T14:50:03.989Z, owner:admin, parentFlowName:825b00f7-3daa-11ed-9d88-0a0027000004_pregateflow, propertySheetId:8264ec1d-3daa-11ed-9d88-0a0027000004, releaseName:Based on pipeline template, requiresDefinition:0, resourceName:, subpluginKey:EC-Jenkins, subprocedure:CollectReportingData, taskCount:0, taskEnabled:1, taskSkippable:0, taskType:PLUGIN, tracked:1, useApproverAcl:0, waitForPlannedStartDate:0, waitingForDependency:0, waitingForManualRetry:0, waitingForPlannedStartDate:0, waitingForPrecondition:0, waitingOnManual:0]]]]


*/

result.object.task.each {
	println it.subpluginKey
}

'''.stripIndent()

	} //procedure
} //project
