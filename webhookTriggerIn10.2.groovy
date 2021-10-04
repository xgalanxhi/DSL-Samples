def myTrigger = trigger "myTestTrigger", {
    projectName = "HolyPlayground"
    releaseName = "TestRelease"
    actualParameter = [
      'ec_stagesToRun': '["Stage 1", "Stage 2"]',
    ]
    enabled = '1'
    insertRollingDeployManualStep = '0'
    pluginKey = 'EC-Github'
    pluginParameter = [
        'commitStatusEvent': 'false',
        'includeBranches': "master",
        'includeCommitStatuses': 'success',
        'includePrActions': 'closed_merged',
        'prEvent': 'false',
        'pushEvent': 'true',
        'repositories': "holywen/bashlib",
    ]
    quietTimeMinutes = '0'
    runDuplicates = '1'
    serviceAccountName = "holywen"
    triggerType = 'webhook'
    webhookName = 'default'
    def value = "TestRelease"
    def myGitHubWebhookSecret = value.md5()
    webhookSecret = myGitHubWebhookSecret
    // Custom properties
    property 'ec_trigger_state', {
        propertyType = 'sheet'
    }
  }

  myTrigger.asType(Trigger.class).id
