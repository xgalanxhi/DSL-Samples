procedure 'Get Releases from JIRA', {
    description = ''
    jobNameTemplate = ''
    resourceName = ''
    timeLimit = ''
    timeLimitUnits = 'minutes'
    workspaceName = ''

    formalParameter 'ProjectToCreateRelease', defaultValue: '', {
      description = 'What project do you want to create the releases in?'
      expansionDeferred = '0'
      label = null
      orderIndex = null
      required = '1'
      type = 'project'
    }

    step 'Get Issues from JIRA', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = null
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = null
      parallel = '0'
      postProcessor = null
      precondition = ''
      releaseMode = 'none'
      resourceName = 'local'
      shell = null
      subprocedure = 'GetIssues'
      subproject = '/plugins/EC-JIRA/project'
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = null
      workspaceName = ''
      actualParameter 'config', 'JIRA Configuration'
      actualParameter 'createLink', '0'
      actualParameter 'filter', ''
      actualParameter 'jql', 'issuetype = Release AND project = RO3'
      actualParameter 'maxResults', ''
      actualParameter 'resultFormat', 'propertySheet'
      actualParameter 'resultProperty', '/myJob/getIssuesResult'
    }

    step 'Get Release Details', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = '''ectool setProperty /myJob/releaseName $[/myJob/getIssuesResult/issues/RO3-1/summary] 
ectool setProperty /myJob/plannedStartDate "2019-05-15"
ectool setProperty /myJob/plannedEndDate "2019-06-15"
'''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = ''
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = ''
      subprocedure = null
      subproject = null
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }

    step 'Create Release', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = 'ectool createRelease "$[ProjectToCreateRelease]" "$[/myJob/releaseName]" --description "Release Description" --pipelineName "Release Pipeline Template" --pipelineProjectName "Default" --plannedEndDate $[/myJob/plannedEndDate] --plannedStartDate $[/myJob/plannedStartDate]'
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = ''
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = ''
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

        property 'ProjectToCreateRelease', {

          // Custom properties
          formType = 'standard'
        }

        property 'projectName', {

          // Custom properties
          formType = 'standard'
        }
      }
    }
  }
