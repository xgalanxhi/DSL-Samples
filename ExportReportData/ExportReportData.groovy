/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------

CloudBees CD DSL: Generate reporting data from a widget definition

Create a self-service catalog item that can be used to generate a tab-delimited file from a DevOps Insight
widget (report).

TODO: Widget drop down only works for "Electric Cloud" & "Application Deployments"
TODO: Documentation
TODO: Add filtering operations

 */

def CurrentProject = 'dslsamples'

project CurrentProject,{
    procedure "Export Report Data",{
        formalParameter "ProjName", required: true
        formalParameter "DashName", required: true
        formalParameter "WidName", required: true

        step "Run", shell: "ec-groovy", command: '''\
            import groovy.json.JsonOutput
            import com.electriccloud.client.groovy.ElectricFlow
            ElectricFlow ef = new ElectricFlow()
            println "Project Name:   $[ProjName]"
            println "Dashboard Name: $[DashName]"
            println "Widget Name:    $[WidName]"
            def widget = [
              projectName: '$[ProjName]',
              dashboardName:'$[DashName]',
              widgetName:'$[WidName]'
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

    catalog 'Reports', {
        catalogItem 'Export Report Data', {
            description = '''\
                <xml>
                  <title>
                    
                  </title>
                
                  <htmlData>
                    <![CDATA[
                      
                    ]]>
                  </htmlData>
                </xml>
            '''.stripIndent()
            buttonLabel = 'Generate'
            dslParamForm = ''
            dslString = '''\
                def RunId = runProcedure(procedureName: "Export Report Data", projectName: "''' + CurrentProject + '''",
                    actualParameter: [
                        ProjName : args.ProjName,
                        DashName : args.DashName,
                        WidName : args.WidName
                    ]
                )
                return RunId
            '''.stripIndent()
            endTargetJson = '''\
                {
                  "object": "job"
                }
            '''.stripIndent()
            iconUrl = 'icon-catalog-item.svg'
            useFormalParameter = '1'

            formalParameter 'ProjName', {
                expansionDeferred = '0'
                label = 'Project Name'
                orderIndex = '1'
                required = '1'
                type = 'project'
            }

            formalParameter 'DashName', {
                dependsOn = 'ProjName'
                expansionDeferred = '0'
                label = 'Dashboard Name'
                optionsDsl = '''\
                    import com.electriccloud.domain.FormalParameterOptionsResult
                    
                    def options = new FormalParameterOptionsResult()
                    
                    getDashboards(projectName: args.parameters['ProjName']).each { Dash ->
                      options.add(Dash.dashboardName, Dash.dashboardName)
                    }
                    
                    return options
                '''.stripIndent()
                orderIndex = '2'
                required = '1'
                type = 'select'
            }

            formalParameter 'WidName', {
                dependsOn = 'DashName'
                expansionDeferred = '0'
                label = 'Widget Name'
                optionsDsl = '''\
                    import com.electriccloud.domain.FormalParameterOptionsResult

                    def options = new FormalParameterOptionsResult()
                    def DashName = args.parameters['DashName']?:"Application Deployments"
                    def ProjName = args.parameters['ProjName']?:"Electric Cloud"

                    getWidgets(projectName: ProjName, dashboardName: DashName).each { Wid ->
                        options.add(Wid.widgetName, Wid.widgetName)
                    }

                    return options
                '''.stripIndent()
                orderIndex = '3'
                required = '1'
                type = 'select'
            }
        }
    }
}