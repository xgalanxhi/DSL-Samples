/*

Procedure that calls workflow which polls ServiceNow record until it reaches 'approved' state

Add PollServiceNow project to ServiceNow plugin project ACL
Install and promote WorkflowWrapper: https://github.com/electric-cloud/ec-workflow-wrapper/blob/master/out/WorkflowWrapper-1.2.0.jar

*/

project 'PollServiceNow', {
  resourceName = null
  workspaceName = null

  procedure 'sleep', {
    description = ''
    jobNameTemplate = ''
    resourceName = ''
    timeLimit = ''
    timeLimitUnits = 'minutes'
    workspaceName = ''

    formalParameter 'SleepTime', defaultValue: '5', {
      description = 'In Second'
      expansionDeferred = '0'
      label = null
      orderIndex = null
      required = '1'
      type = 'entry'
    }

    step 'sleep', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = 'sleep $[SleepTime]'
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = ''
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = 'ec-perl'
      subprocedure = null
      subproject = null
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }

    // Custom properties

    property 'ec_customEditorData', {

      // Custom properties

      property 'parameters', {

        // Custom properties

        property 'SleepTime', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
  }

  workflowDefinition 'Wait for Approval', {
    description = ''
    workflowNameTemplate = ''

    stateDefinition 'Start', {
      description = ''
      startable = '1'
      subprocedure = 'GetRecord'
      subproject = '/plugins/EC-ServiceNow/project'
      substartingState = ''
      subworkflowDefinition = ''
      actualParameter 'config_name', '$[Config]'
      actualParameter 'filter', ''
      actualParameter 'property_sheet', '/myWorkflow'
      actualParameter 'record_id', '$[RecordID]'

      formalParameter 'Config', defaultValue: 'ServiceNow', {
        description = 'ServiceNow configuration'
        expansionDeferred = '0'
        label = null
        orderIndex = null
        required = '1'
        type = 'entry'
      }

      formalParameter 'RecordID', defaultValue: '', {
        description = ''
        expansionDeferred = '0'
        label = null
        orderIndex = null
        required = '1'
        type = 'entry'
      }

      // Custom properties

      property 'ec_customEditorData', {

        // Custom properties

        property 'parameters', {

          // Custom properties

          property 'Config', {

            // Custom properties
            formType = 'standard'
          }

          property 'RecordID', {

            // Custom properties
            formType = 'standard'
          }
        }
      }
    }

    stateDefinition 'Done', {
      description = ''
      startable = '0'
      subprocedure = null
      subproject = null
      substartingState = null
      subworkflowDefinition = null
    }

    stateDefinition 'Wait', {
      description = ''
      startable = '0'
      subprocedure = 'sleep'
      subproject = ''
      substartingState = ''
      subworkflowDefinition = ''
      actualParameter 'SleepTime', '5'
    }

    transitionDefinition 'Done', {
      description = ''
      condition = '$[/javascript JSON.parse(myWorkflow.ResponseContent)[0].approval == "approved" ]'
      stateDefinitionName = 'Start'
      targetState = 'Done'
      trigger = 'onCompletion'
    }

    transitionDefinition 'Wait', {
      description = ''
      condition = '$[/javascript JSON.parse(myWorkflow.ResponseContent)[0].approval != "approved" ]'
      stateDefinitionName = 'Start'
      targetState = 'Wait'
      trigger = 'onCompletion'
    }

    transitionDefinition 'repeat', {
      description = ''
      condition = ''
      stateDefinitionName = 'Wait'
      targetState = 'Start'
      trigger = 'onCompletion'
      actualParameter 'Config', '$[/myWorkflow/Config]'
      actualParameter 'RecordID', '$[/myWorkflow/RecordID]'
    }
  }
}
