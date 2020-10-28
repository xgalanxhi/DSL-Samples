/*

CloudBees CD DSL: Generate reporting data from a widget definition

Currently implemented as a procedure requiring the user to manually enter the project name,
dashboard name, and the widget name. The data is written to a tab-delimited file and this
file is accessible from the job page as a link.


TODO: don't use projectName as a formal parameter to avoid conflicts with intrinsic
TODO: Wrap the procedure in an SSC with pull down menus for project, dashboard and widget

DSL version:
def widget = [
  projectName: "Electric Cloud",
  dashboardName:"Application Deployments",
  widgetName:"TopAppsByDeployments"
]
widget << [reportName:getWidget(widget).reportName]
def ReportData = runReport(widget).asNode()[0]
def headers = []
ReportData[0].fieldNames().each {headers.push it}
def tab = headers.join('\t') + '\n'
ReportData.each { row ->
 headers.each { field ->
    tab += row[field]
    tab += '\t'
  }
  tab += '\n'
}
tab


 */

project "Report Data",{
    procedure "Export Report Data",{
            formalParameter "projectName", required: true
        formalParameter "dashboardName", required: true
        formalParameter "widgetName", required: true

        step "Run", shell: "ec-groovy", command: '''\
            import groovy.json.JsonOutput
            import com.electriccloud.client.groovy.ElectricFlow
            ElectricFlow ef = new ElectricFlow()
            def widget = [
              projectName: '$[/myJob/actualParameters/projectName]',
              dashboardName:'$[dashboardName]',
              widgetName:'$[widgetName]'
            ]
            widget << [reportName:ef.getWidget(widget).widget.reportName]
            def ReportData = ef.runReport(widget).result
            def headers = []
            ReportData[0].each {key, val -> headers.push key}
            
            def fileName = "report.tab"
            File directory = new File("artifacts")
            File fh = new File("artifacts" + '/' + fileName)
            if (! directory.exists()) directory.mkdir()
            fh << headers.join('\\t') + '\\n'
            ReportData.each { row ->
             headers.each { field ->
                fh << row[field]
                fh << '\\t'
              }
              fh << '\\n'
            }
            ef.setProperty(
                propertyName: "/myJob/report-urls/Report Data",
                value: "/commander/jobSteps/$[/myJobStep/jobStepId]/${fileName}"
            )         
        '''.stripIndent()
    }
}