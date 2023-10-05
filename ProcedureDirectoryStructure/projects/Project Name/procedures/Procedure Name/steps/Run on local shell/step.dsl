import java.io.File


step 'Run on local shell', {
  description = ''
  alwaysRun = '0'
  broadcast = '0'
  command = new File(projectDir, "./procedures/Procedure Name/steps/Run on local shell.cmd").text
  condition = ''
  errorHandling = 'failProcedure'
  exclusiveMode = 'none'
  parallel = '0'
  precondition = ''
  procedureName = 'Procedure Name'
  projectName = 'Project Name'
  releaseMode = 'none'
  subprocedure = ''
  subproject = ''
  timeLimit = '0'
  timeLimitUnits = 'minutes'
  workspaceName = ''
}
