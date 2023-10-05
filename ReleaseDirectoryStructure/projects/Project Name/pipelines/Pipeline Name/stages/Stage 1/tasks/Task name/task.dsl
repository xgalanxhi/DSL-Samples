import java.io.File


task 'Task name', {
  advancedMode = '0'
  allowOutOfOrderRun = '0'
  alwaysRun = '0'
  command = new File(projectDir, "./pipelines/Pipeline Name/stages/Stage 1/tasks/Task name.cmd").text
  enabled = '1'
  errorHandling = 'stopOnError'
  insertRollingDeployManualStep = '0'
  projectName = 'Project Name'
  skippable = '0'
  taskType = 'COMMAND'
  useApproverAcl = '0'
  waitForPlannedStartDate = '0'
}
