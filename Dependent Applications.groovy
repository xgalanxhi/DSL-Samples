/*

CloudBees Flow DSL: Illustrate application dependencies

This demonstration uses EC-NoArtifact, as a result, it is necessary to turn off Artifact Staging when running the Deploy process.

Set up
------
1. Verify that there is no inventory in "Dependent Applications" QA Environment:Â https://flow.acme-global.com/flow/#environments/a87bf55f-2b2d-11ea-b52c-06fe7d1e6f5a/inventory
2. If there is, run the Undeploy application process for that application

Demo
----
1. Navigate to Applications,Â https://flow.acme-global.com/flow/#applications
2. Filter down to the "Dependent Applications" project3. Note the two applications
3. Show that Slave App depends on the Master,Â https://flow.acme-global.com/flow/#applications/b6747794-2b2d-11ea-b20d-06fe7d1e6f5a5. Run the Deploy process for this Slave App. It will fail. Note the error message about being dependent on "Master App"
4. Now run the Master App Deploy process. Note the inventory.
5. Rerun the Slave App. It should work.

*/

project 'Dependent Applications', {
  environment 'PROD', {
    environmentEnabled = '1'
    projectName = 'Dependent Applications'
    environmentTier 'App Tier', {
      resource 'Dependent Applications_PROD_App Tier', hostName: "localhost"
    }
  }

  environment 'QA', {
    environmentEnabled = '1'
    projectName = 'Dependent Applications'
    reservationRequired = null
    rollingDeployEnabled = null
    rollingDeployType = null

    environmentTier 'App Tier', {
      batchSize = null
      batchSizeType = null
      resource 'Dependent Applications_QA_App Tier', hostName: "localhost"
    }
  }

  application 'Slave App', {

    applicationDependency '0f423aa2-2b2e-11ea-b7e5-06fe7d1e6f5a', {
      dependentApplicationName = 'Master App'
      dependentProjectName = null
      dependentServiceName = null
      dependentSnapshotName = null
      effectiveDate = null
      snapshotName = null
    }

    applicationTier 'App Tier', {
      applicationName = 'Slave App'
      projectName = 'Dependent Applications'

      component 'SA', pluginName: null, {
        applicationName = 'Slave App'
        pluginKey = 'EC-Artifact'
        reference = '0'
        sourceComponentName = null
        sourceProjectName = null

        process 'Install', {
          applicationName = null
          exclusiveEnvironment = null
          processType = 'DEPLOY'
          serviceName = null
          smartUndeployEnabled = null
          timeLimitUnits = null
          workingDirectory = null
          workspaceName = null

          processStep 'Create Artifact Placeholder', {
            actionLabelText = null
            actualParameter = [
              'commandToRun': '''artifact artifactKey: "$[/myComponent/ec_content_details/artifactName]", groupId: "group"
''',
              'shellToUse': 'ectool evalDsl --dslFile',
            ]
            afterLastRetry = null
            allowSkip = null
            alwaysRun = '0'
            applicationTierName = null
            componentRollback = null
            dependencyJoinType = null
            disableFailure = null
            emailConfigName = null
            errorHandling = 'abortJob'
            instruction = null
            notificationEnabled = null
            notificationTemplate = null
            processStepType = 'command'
            retryCount = null
            retryInterval = null
            retryType = null
            rollbackSnapshot = null
            rollbackType = null
            rollbackUndeployProcess = null
            skipRollbackIfUndeployFails = null
            smartRollback = null
            subcomponent = null
            subcomponentApplicationName = null
            subcomponentProcess = null
            subprocedure = 'RunCommand'
            subproject = '/plugins/EC-Core/project'
            subservice = null
            subserviceProcess = null
            timeLimitUnits = null
            useUtilityResource = '0'
            utilityResourceName = null
            workingDirectory = null
            workspaceName = null
          }
        }

        process 'Uninstall', {
          applicationName = null
          exclusiveEnvironment = null
          processType = 'UNDEPLOY'
          serviceName = null
          smartUndeployEnabled = '1'
          timeLimitUnits = null
          workingDirectory = null
          workspaceName = null

          processStep 'Uninstall', {
            actionLabelText = null
            actualParameter = [
              'commandToRun': 'echo',
            ]
            afterLastRetry = null
            allowSkip = null
            alwaysRun = '0'
            applicationTierName = null
            componentRollback = null
            dependencyJoinType = 'and'
            disableFailure = null
            emailConfigName = null
            errorHandling = 'abortJob'
            instruction = null
            notificationEnabled = null
            notificationTemplate = null
            processStepType = 'command'
            retryCount = null
            retryInterval = null
            retryType = null
            rollbackSnapshot = null
            rollbackType = null
            rollbackUndeployProcess = null
            skipRollbackIfUndeployFails = null
            smartRollback = null
            subcomponent = null
            subcomponentApplicationName = null
            subcomponentProcess = null
            subprocedure = 'RunCommand'
            subproject = '/plugins/EC-Core/project'
            subservice = null
            subserviceProcess = null
            timeLimitUnits = null
            useUtilityResource = '0'
            utilityResourceName = null
            workingDirectory = null
            workspaceName = null
          }
        }

        // Custom properties

        property 'ec_content_details', {

          // Custom properties

          property 'artifactName', value: 'SA', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
          filterList = ''
          overwrite = 'update'
          pluginProcedure = 'Retrieve'

          property 'pluginProjectName', value: 'EC-Artifact', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          retrieveToDirectory = ''

          property 'versionRange', value: '$[SA_version]', {
            expandable = '1'
            suppressValueTracking = '0'
          }
        }
      }
    }

    process 'Deploy', {
      applicationName = 'Slave App'
      exclusiveEnvironment = null
      processType = 'OTHER'
      serviceName = null
      smartUndeployEnabled = null
      timeLimitUnits = null
      workingDirectory = null
      workspaceName = null

      formalParameter 'SA_version', defaultValue: '1.0', {
        expansionDeferred = '0'
        label = null
        orderIndex = null
        required = '0'
        type = null
      }

      formalParameter 'ec_SA-run', defaultValue: '1', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_SA-version', defaultValue: '$[/projects/Dependent Applications/applications/Slave App/components/SA/ec_content_details/versionRange]', {
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'entry'
      }

      formalParameter 'ec_enforceDependencies', defaultValue: '0', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_smartDeployOption', defaultValue: '1', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_stageArtifacts', defaultValue: '0', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      processStep 'SA', {
        actionLabelText = null
        afterLastRetry = null
        allowSkip = null
        alwaysRun = '0'
        applicationTierName = 'App Tier'
        componentRollback = null
        dependencyJoinType = null
        disableFailure = null
        emailConfigName = null
        errorHandling = 'abortJob'
        instruction = null
        notificationEnabled = null
        notificationTemplate = null
        processStepType = 'process'
        retryCount = null
        retryInterval = null
        retryType = null
        rollbackSnapshot = null
        rollbackType = null
        rollbackUndeployProcess = null
        skipRollbackIfUndeployFails = null
        smartRollback = null
        subcomponent = 'SA'
        subcomponentApplicationName = 'Slave App'
        subcomponentProcess = 'Install'
        subprocedure = null
        subproject = null
        subservice = null
        subserviceProcess = null
        timeLimitUnits = null
        useUtilityResource = '0'
        utilityResourceName = null
        workingDirectory = null
        workspaceName = null

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
    }

    process 'Undeploy', {
      applicationName = 'Slave App'
      exclusiveEnvironment = '0'
      processType = 'OTHER'
      serviceName = null
      smartUndeployEnabled = null
      timeLimitUnits = null
      workingDirectory = null
      workspaceName = null

      formalParameter 'ec_SA-run', defaultValue: '1', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_SA-version', defaultValue: '$[/projects/Dependent Applications/applications/Slave App/components/SA/ec_content_details/versionRange]', {
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'entry'
      }

      formalParameter 'ec_enforceDependencies', defaultValue: '0', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_smartDeployOption', defaultValue: '1', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_stageArtifacts', defaultValue: '0', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      processStep 'Uninstall', {
        actionLabelText = null
        afterLastRetry = null
        allowSkip = null
        alwaysRun = '0'
        applicationTierName = 'App Tier'
        componentRollback = null
        dependencyJoinType = 'and'
        disableFailure = null
        emailConfigName = null
        errorHandling = 'abortJob'
        instruction = null
        notificationEnabled = null
        notificationTemplate = null
        processStepType = 'process'
        retryCount = null
        retryInterval = null
        retryType = null
        rollbackSnapshot = null
        rollbackType = null
        rollbackUndeployProcess = null
        skipRollbackIfUndeployFails = null
        smartRollback = null
        subcomponent = 'SA'
        subcomponentApplicationName = 'Slave App'
        subcomponentProcess = 'Uninstall'
        subprocedure = null
        subproject = null
        subservice = null
        subserviceProcess = null
        timeLimitUnits = null
        useUtilityResource = '0'
        utilityResourceName = null
        workingDirectory = null
        workspaceName = null

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
    }

    tierMap 'PROD', {
      applicationName = 'Slave App'
      environmentName = 'PROD'
      environmentProjectName = 'Dependent Applications'
      projectName = 'Dependent Applications'

      tierMapping 'App Tier_PROD', {
        applicationTierName = 'App Tier'
        environmentTierName = 'App Tier'
        resourceExpression = null
        tierMapName = 'PROD'
      }
    }

    tierMap 'QA', {
      applicationName = 'Slave App'
      environmentName = 'QA'
      environmentProjectName = 'Dependent Applications'
      projectName = 'Dependent Applications'

      tierMapping 'App Tier_QA', {
        applicationTierName = 'App Tier'
        environmentTierName = 'App Tier'
        resourceExpression = null
        tierMapName = 'QA'
      }
    }

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }

    property 'jobCounter', value: '28', {
      expandable = '1'
      suppressValueTracking = '1'
    }
  }

  application 'Master App', {

    applicationTier 'App Tier', {
      applicationName = 'Master App'
      projectName = 'Dependent Applications'

      component 'MA', pluginName: null, {
        applicationName = 'Master App'
        pluginKey = 'EC-Artifact'
        reference = '0'
        sourceComponentName = null
        sourceProjectName = null

        process 'Install', {
          applicationName = null
          exclusiveEnvironment = null
          processType = 'DEPLOY'
          serviceName = null
          smartUndeployEnabled = null
          timeLimitUnits = null
          workingDirectory = null
          workspaceName = null

          processStep 'Create Artifact Placeholder', {
            actionLabelText = null
            actualParameter = [
              'commandToRun': '''artifact artifactKey: "$[/myComponent/ec_content_details/artifactName]", groupId: "group"
''',
              'shellToUse': 'ectool evalDsl --dslFile',
            ]
            afterLastRetry = null
            allowSkip = null
            alwaysRun = '0'
            applicationTierName = null
            componentRollback = null
            dependencyJoinType = null
            disableFailure = null
            emailConfigName = null
            errorHandling = 'abortJob'
            instruction = null
            notificationEnabled = null
            notificationTemplate = null
            processStepType = 'command'
            retryCount = null
            retryInterval = null
            retryType = null
            rollbackSnapshot = null
            rollbackType = null
            rollbackUndeployProcess = null
            skipRollbackIfUndeployFails = null
            smartRollback = null
            subcomponent = null
            subcomponentApplicationName = null
            subcomponentProcess = null
            subprocedure = 'RunCommand'
            subproject = '/plugins/EC-Core/project'
            subservice = null
            subserviceProcess = null
            timeLimitUnits = null
            useUtilityResource = '0'
            utilityResourceName = null
            workingDirectory = null
            workspaceName = null
          }
        }

        process 'Uninstall', {
          applicationName = null
          exclusiveEnvironment = null
          processType = 'UNDEPLOY'
          serviceName = null
          smartUndeployEnabled = '1'
          timeLimitUnits = null
          workingDirectory = null
          workspaceName = null

          processStep 'Uninstall', {
            actionLabelText = null
            actualParameter = [
              'commandToRun': 'echo',
            ]
            afterLastRetry = null
            allowSkip = null
            alwaysRun = '0'
            applicationTierName = null
            componentRollback = null
            dependencyJoinType = 'and'
            disableFailure = null
            emailConfigName = null
            errorHandling = 'abortJob'
            instruction = null
            notificationEnabled = null
            notificationTemplate = null
            processStepType = 'command'
            retryCount = null
            retryInterval = null
            retryType = null
            rollbackSnapshot = null
            rollbackType = null
            rollbackUndeployProcess = null
            skipRollbackIfUndeployFails = null
            smartRollback = null
            subcomponent = null
            subcomponentApplicationName = null
            subcomponentProcess = null
            subprocedure = 'RunCommand'
            subproject = '/plugins/EC-Core/project'
            subservice = null
            subserviceProcess = null
            timeLimitUnits = null
            useUtilityResource = '0'
            utilityResourceName = null
            workingDirectory = null
            workspaceName = null
          }
        }

        // Custom properties

        property 'ec_content_details', {

          // Custom properties

          property 'artifactName', value: 'MA', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
          filterList = ''
          overwrite = 'update'
          pluginProcedure = 'Retrieve'

          property 'pluginProjectName', value: 'EC-Artifact', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          retrieveToDirectory = ''

          property 'versionRange', value: '$[MA_version]', {
            expandable = '1'
            suppressValueTracking = '0'
          }
        }
      }
    }

    process 'Deploy', {
      applicationName = 'Master App'
      exclusiveEnvironment = null
      processType = 'OTHER'
      serviceName = null
      smartUndeployEnabled = null
      timeLimitUnits = null
      workingDirectory = null
      workspaceName = null

      formalParameter 'MA_version', defaultValue: '1.0', {
        expansionDeferred = '0'
        label = null
        orderIndex = null
        required = '0'
        type = null
      }

      formalParameter 'ec_MA-run', defaultValue: '1', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_MA-version', defaultValue: '$[/projects/Dependent Applications/applications/Master App/components/MA/ec_content_details/versionRange]', {
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'entry'
      }

      formalParameter 'ec_enforceDependencies', defaultValue: '0', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_smartDeployOption', defaultValue: '1', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_stageArtifacts', defaultValue: '0', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      processStep 'MA', {
        actionLabelText = null
        afterLastRetry = null
        allowSkip = null
        alwaysRun = '0'
        applicationTierName = 'App Tier'
        componentRollback = null
        dependencyJoinType = null
        disableFailure = null
        emailConfigName = null
        errorHandling = 'abortJob'
        instruction = null
        notificationEnabled = null
        notificationTemplate = null
        processStepType = 'process'
        retryCount = null
        retryInterval = null
        retryType = null
        rollbackSnapshot = null
        rollbackType = null
        rollbackUndeployProcess = null
        skipRollbackIfUndeployFails = null
        smartRollback = null
        subcomponent = 'MA'
        subcomponentApplicationName = 'Master App'
        subcomponentProcess = 'Install'
        subprocedure = null
        subproject = null
        subservice = null
        subserviceProcess = null
        timeLimitUnits = null
        useUtilityResource = '0'
        utilityResourceName = null
        workingDirectory = null
        workspaceName = null

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
    }

    process 'Undeploy', {
      applicationName = 'Master App'
      exclusiveEnvironment = '0'
      processType = 'OTHER'
      serviceName = null
      smartUndeployEnabled = null
      timeLimitUnits = null
      workingDirectory = null
      workspaceName = null

      formalParameter 'ec_MA-run', defaultValue: '1', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_MA-version', defaultValue: '$[/projects/Dependent Applications/applications/Master App/components/MA/ec_content_details/versionRange]', {
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'entry'
      }

      formalParameter 'ec_enforceDependencies', defaultValue: '0', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_smartDeployOption', defaultValue: '1', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      formalParameter 'ec_stageArtifacts', defaultValue: '0', {
        checkedValue = null
        expansionDeferred = '1'
        label = null
        orderIndex = null
        required = '0'
        type = 'checkbox'
        uncheckedValue = null
      }

      processStep 'Uninstall', {
        actionLabelText = null
        afterLastRetry = null
        allowSkip = null
        alwaysRun = '0'
        applicationTierName = 'App Tier'
        componentRollback = null
        dependencyJoinType = 'and'
        disableFailure = null
        emailConfigName = null
        errorHandling = 'abortJob'
        instruction = null
        notificationEnabled = null
        notificationTemplate = null
        processStepType = 'process'
        retryCount = null
        retryInterval = null
        retryType = null
        rollbackSnapshot = null
        rollbackType = null
        rollbackUndeployProcess = null
        skipRollbackIfUndeployFails = null
        smartRollback = null
        subcomponent = 'MA'
        subcomponentApplicationName = 'Master App'
        subcomponentProcess = 'Uninstall'
        subprocedure = null
        subproject = null
        subservice = null
        subserviceProcess = null
        timeLimitUnits = null
        useUtilityResource = '0'
        utilityResourceName = null
        workingDirectory = null
        workspaceName = null

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
    }

    tierMap 'PROD', {
      applicationName = 'Master App'
      environmentName = 'PROD'
      environmentProjectName = 'Dependent Applications'
      projectName = 'Dependent Applications'

      tierMapping 'App Tier_PROD', {
        applicationTierName = 'App Tier'
        environmentTierName = 'App Tier'
        resourceExpression = null
        tierMapName = 'PROD'
      }
    }

    tierMap 'QA', {
      applicationName = 'Master App'
      environmentName = 'QA'
      environmentProjectName = 'Dependent Applications'
      projectName = 'Dependent Applications'

      tierMapping 'App Tier_QA', {
        applicationTierName = 'App Tier'
        environmentTierName = 'App Tier'
        resourceExpression = null
        tierMapName = 'QA'
      }
    }

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }

    property 'jobCounter', value: '19', {
      expandable = '1'
      suppressValueTracking = '1'
    }
  }

  pipeline 'Dependent Application Pipeline', {
    description = ''
    disableMultipleActiveRuns = '0'
    disableRestart = '0'
    enabled = '1'
    overrideWorkspace = '0'
    pipelineRunNameTemplate = null
    releaseName = null
    skipStageMode = 'ENABLED'
    templatePipelineName = null
    templatePipelineProjectName = null
    type = null
    workspaceName = null

    formalParameter 'ec_stagesToRun', defaultValue: null, {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = null
    }

    stage 'QA', {
      description = ''
      colorCode = '#00adee'
      completionType = 'auto'
      condition = null
      duration = null
      parallelToPrevious = null
      pipelineName = 'Dependent Application Pipeline'
      plannedEndDate = null
      plannedStartDate = null
      precondition = null
      resourceName = null
      waitForPlannedStartDate = '0'

      gate 'PRE', {
        condition = null
        precondition = null
      }

      gate 'POST', {
        condition = null
        precondition = null
      }

      task 'Deploy Master App', {
        description = ''
        actionLabelText = null
        actualParameter = [
          'ec_enforceDependencies': '1',
          'ec_smartDeployOption': '1',
          'ec_stageArtifacts': '0',
          'MA_version': '1.7',
        ]
        advancedMode = '0'
        afterLastRetry = null
        allowOutOfOrderRun = '0'
        allowSkip = null
        alwaysRun = '0'
        condition = null
        deployerExpression = null
        deployerRunType = null
        disableFailure = null
        duration = null
        emailConfigName = null
        enabled = '1'
        environmentName = 'QA'
        environmentProjectName = 'Dependent Applications'
        environmentTemplateName = null
        environmentTemplateProjectName = null
        errorHandling = 'stopOnError'
        gateCondition = null
        gateType = null
        groupName = null
        groupRunType = null
        insertRollingDeployManualStep = '0'
        instruction = null
        notificationEnabled = null
        notificationTemplate = null
        parallelToPrevious = null
        plannedEndDate = null
        plannedStartDate = null
        precondition = null
        requiredApprovalsCount = null
        resourceName = ''
        retryCount = null
        retryInterval = null
        retryType = null
        rollingDeployEnabled = '0'
        rollingDeployManualStepCondition = null
        skippable = '0'
        snapshotName = null
        stageSummaryParameters = null
        startingStage = null
        subErrorHandling = null
        subapplication = 'Master App'
        subpipeline = null
        subpluginKey = null
        subprocedure = null
        subprocess = 'Deploy'
        subproject = 'Dependent Applications'
        subrelease = null
        subreleasePipeline = null
        subreleasePipelineProject = null
        subreleaseSuffix = null
        subservice = null
        subworkflowDefinition = null
        subworkflowStartingState = null
        taskProcessType = 'APPLICATION'
        taskType = 'PROCESS'
        triggerType = null
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }

      task 'Deploy Slave App', {
        description = ''
        actionLabelText = null
        actualParameter = [
          'ec_enforceDependencies': '1',
          'ec_smartDeployOption': '1',
          'ec_stageArtifacts': '0',
          'SA_version': '2.1',
        ]
        advancedMode = '0'
        afterLastRetry = null
        allowOutOfOrderRun = '0'
        allowSkip = null
        alwaysRun = '0'
        condition = null
        deployerExpression = null
        deployerRunType = null
        disableFailure = null
        duration = null
        emailConfigName = null
        enabled = '1'
        environmentName = 'QA'
        environmentProjectName = 'Dependent Applications'
        environmentTemplateName = null
        environmentTemplateProjectName = null
        errorHandling = 'stopOnError'
        gateCondition = null
        gateType = null
        groupName = null
        groupRunType = null
        insertRollingDeployManualStep = '0'
        instruction = null
        notificationEnabled = null
        notificationTemplate = null
        parallelToPrevious = null
        plannedEndDate = null
        plannedStartDate = null
        precondition = null
        requiredApprovalsCount = null
        resourceName = ''
        retryCount = null
        retryInterval = null
        retryType = null
        rollingDeployEnabled = '0'
        rollingDeployManualStepCondition = null
        skippable = '0'
        snapshotName = null
        stageSummaryParameters = null
        startingStage = null
        subErrorHandling = null
        subapplication = 'Slave App'
        subpipeline = null
        subpluginKey = null
        subprocedure = null
        subprocess = 'Deploy'
        subproject = 'Dependent Applications'
        subrelease = null
        subreleasePipeline = null
        subreleasePipelineProject = null
        subreleaseSuffix = null
        subservice = null
        subworkflowDefinition = null
        subworkflowStartingState = null
        taskProcessType = 'APPLICATION'
        taskType = 'PROCESS'
        triggerType = null
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }
    }

    stage 'Prod', {
      description = ''
      colorCode = '#008080'
      completionType = 'auto'
      condition = null
      duration = null
      parallelToPrevious = null
      pipelineName = 'Dependent Application Pipeline'
      plannedEndDate = null
      plannedStartDate = null
      precondition = null
      resourceName = null
      waitForPlannedStartDate = '0'

      gate 'PRE', {
        condition = null
        precondition = null

        task 'Promotion', {
          description = ''
          actionLabelText = null
          advancedMode = '0'
          afterLastRetry = null
          allowOutOfOrderRun = '0'
          allowSkip = '0'
          alwaysRun = '0'
          condition = null
          deployerExpression = null
          deployerRunType = null
          disableFailure = null
          duration = null
          emailConfigName = null
          enabled = '1'
          environmentName = null
          environmentProjectName = null
          environmentTemplateName = null
          environmentTemplateProjectName = null
          errorHandling = 'stopOnError'
          gateCondition = null
          gateType = 'PRE'
          groupName = null
          groupRunType = null
          insertRollingDeployManualStep = '0'
          instruction = null
          notificationEnabled = '1'
          notificationTemplate = 'ec_default_gate_task_notification_template'
          parallelToPrevious = null
          plannedEndDate = null
          plannedStartDate = null
          precondition = null
          requiredApprovalsCount = null
          resourceName = ''
          retryCount = null
          retryInterval = null
          retryType = null
          rollingDeployEnabled = null
          rollingDeployManualStepCondition = null
          skippable = '0'
          snapshotName = null
          stageSummaryParameters = null
          startingStage = null
          subErrorHandling = null
          subapplication = null
          subpipeline = null
          subpluginKey = null
          subprocedure = null
          subprocess = null
          subproject = 'Dependent Applications'
          subrelease = null
          subreleasePipeline = null
          subreleasePipelineProject = null
          subreleaseSuffix = null
          subservice = null
          subworkflowDefinition = null
          subworkflowStartingState = null
          taskProcessType = null
          taskType = 'APPROVAL'
          triggerType = null
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
          approver = [
            'admin',
          ]
        }
      }

      gate 'POST', {
        condition = null
        precondition = null
      }

      task 'Deploy Master App', {
        description = ''
        actionLabelText = null
        actualParameter = [
          'ec_enforceDependencies': '1',
          'ec_smartDeployOption': '1',
          'ec_stageArtifacts': '0',
          'MA_version': '1.7',
        ]
        advancedMode = '0'
        afterLastRetry = null
        allowOutOfOrderRun = '0'
        allowSkip = null
        alwaysRun = '0'
        condition = null
        deployerExpression = null
        deployerRunType = null
        disableFailure = null
        duration = null
        emailConfigName = null
        enabled = '1'
        environmentName = 'PROD'
        environmentProjectName = 'Dependent Applications'
        environmentTemplateName = null
        environmentTemplateProjectName = null
        errorHandling = 'stopOnError'
        gateCondition = null
        gateType = null
        groupName = null
        groupRunType = null
        insertRollingDeployManualStep = '0'
        instruction = null
        notificationEnabled = null
        notificationTemplate = null
        parallelToPrevious = null
        plannedEndDate = null
        plannedStartDate = null
        precondition = null
        requiredApprovalsCount = null
        resourceName = ''
        retryCount = null
        retryInterval = null
        retryType = null
        rollingDeployEnabled = '0'
        rollingDeployManualStepCondition = null
        skippable = '0'
        snapshotName = null
        stageSummaryParameters = null
        startingStage = null
        subErrorHandling = null
        subapplication = 'Master App'
        subpipeline = null
        subpluginKey = null
        subprocedure = null
        subprocess = 'Deploy'
        subproject = 'Dependent Applications'
        subrelease = null
        subreleasePipeline = null
        subreleasePipelineProject = null
        subreleaseSuffix = null
        subservice = null
        subworkflowDefinition = null
        subworkflowStartingState = null
        taskProcessType = 'APPLICATION'
        taskType = 'PROCESS'
        triggerType = null
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }

      task 'Deploy Slave App', {
        description = ''
        actionLabelText = null
        actualParameter = [
          'ec_enforceDependencies': '1',
          'ec_smartDeployOption': '1',
          'ec_stageArtifacts': '0',
          'SA_version': '2.1',
        ]
        advancedMode = '0'
        afterLastRetry = null
        allowOutOfOrderRun = '0'
        allowSkip = null
        alwaysRun = '0'
        condition = null
        deployerExpression = null
        deployerRunType = null
        disableFailure = null
        duration = null
        emailConfigName = null
        enabled = '1'
        environmentName = 'PROD'
        environmentProjectName = 'Dependent Applications'
        environmentTemplateName = null
        environmentTemplateProjectName = null
        errorHandling = 'stopOnError'
        gateCondition = null
        gateType = null
        groupName = null
        groupRunType = null
        insertRollingDeployManualStep = '0'
        instruction = null
        notificationEnabled = null
        notificationTemplate = null
        parallelToPrevious = null
        plannedEndDate = null
        plannedStartDate = null
        precondition = null
        requiredApprovalsCount = null
        resourceName = ''
        retryCount = null
        retryInterval = null
        retryType = null
        rollingDeployEnabled = '0'
        rollingDeployManualStepCondition = null
        skippable = '0'
        snapshotName = null
        stageSummaryParameters = null
        startingStage = null
        subErrorHandling = null
        subapplication = 'Slave App'
        subpipeline = null
        subpluginKey = null
        subprocedure = null
        subprocess = 'Deploy'
        subproject = 'Dependent Applications'
        subrelease = null
        subreleasePipeline = null
        subreleasePipelineProject = null
        subreleaseSuffix = null
        subservice = null
        subworkflowDefinition = null
        subworkflowStartingState = null
        taskProcessType = 'APPLICATION'
        taskType = 'PROCESS'
        triggerType = null
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }
    }

    // Custom properties

    property 'ec_counters', {

      // Custom properties
      pipelineCounter = '1'
    }
  }
}
