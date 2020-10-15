/*

CloudBees CD DSL: Find all active schedules

Running this DSL will output a list of projectName:scheduleNames for all enabled schedules.
The commented out line can be used to disable all the schedules.

*/
def enabled = []
getProjects().each { proj ->
  getSchedules(projectName: proj.projectName).each { sched ->
    if (sched.scheduleDisabled==false) {
      enabled.push((sched.projectName):sched.scheduleName)
      //schedule sched.scheduleName, projectName: sched.projectName, scheduleDisabled: true
    }
  }
}
enabled
