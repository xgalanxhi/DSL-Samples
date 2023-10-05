import java.io.File


step 'Run ec-groovy', {
  description = ''
  alwaysRun = '0'
  broadcast = '0'
  command = new File(projectDir, "./procedures/Procedure Name/steps/Run ec-groovy.groovy").text
  condition = ''
  errorHandling = 'failProcedure'
  exclusiveMode = 'none'
  logFileName = ''
  parallel = '0'
  postProcessor = ''
  precondition = ''
  procedureName = 'Procedure Name'
  projectName = 'Project Name'
  releaseMode = 'none'
  resourceName = ''
  shell = 'ec-groovy'
  subprocedure = ''
  subproject = ''
  timeLimit = '0'
  timeLimitUnits = 'seconds'
  workingDirectory = ''
  workspaceName = ''
}
