
pipeline 'Pipeline Name', {
  disableMultipleActiveRuns = '0'
  disableRestart = '0'
  enabled = '1'
  overrideWorkspace = '0'
  projectName = 'Project Name'
  skipStageMode = 'ENABLED'

  formalOutputParameter 'output'

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
    required = '0'
  }

  formalParameter 'input', defaultValue: 'input default', {
    expansionDeferred = '0'
    required = '0'
  }
}
