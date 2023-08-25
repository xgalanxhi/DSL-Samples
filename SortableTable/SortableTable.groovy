/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------

Change the `CurrentProject` to the target project where  you want this procedure installed.

*/
def CurrentProject = 'dslsamples'

procedure 'TimelineSortableTable', {
  projectName = CurrentProject

  formalParameter 'jsonData', defaultValue: '', {
    label = 'JSON Table Data:'
    orderIndex = '1'
    required = '1'
    type = 'textarea'
  }

  formalParameter 'reportName', defaultValue: '', {
    label = 'Timeline Name'
    orderIndex = '2'
    type = 'entry'
  }

  formalParameter 'reportLogo', defaultValue: '', {
    label = 'Report Logo Link'
    orderIndex = '3'
    type = 'entry'
  }

  step 'Generate report', {
    command = '''import groovy.xml.MarkupBuilder
import groovy.json.*
import com.electriccloud.client.groovy.ElectricFlow

ElectricFlow ef = new ElectricFlow()

def ReportTitle = \'$[reportName]\'?\'$[reportName]\':\'report\'
def ReportLogo = \'$[reportLogo]\'?\'$[reportLogo]\':\'/commander/images/logo.gif\'

def TableData = new JsonSlurper().parseText \'\'\'$[jsonData]\'\'\'

def ColumnOrnamentation = new JsonSlurper().parseText \'\'\' \'\'\'.contains(\'[\')?\'\'\' \'\'\':\'[]\'

//assert TableData.getClass() == "java.util.ArrayList"

// Identify all the Column names in the JSON object
ColumnHeaders = []
TableData.each { row ->
	//assert row.getClass() == "groovy.json.internal.LazyMap"
	row.each { key, value ->
		if (!(key in ColumnHeaders)) ColumnHeaders.push(key)
	}
}
ColumnHeaders.sort()
ColumnHeaders

def sb = new StringWriter()
def html = new MarkupBuilder(sb)

html.doubleQuotes = true
html.expandEmptyElements = true
html.omitEmptyAttributes = false
html.omitNullAttributes = false
html.html {
    head {
        title (ReportTitle)
        script (src: \'https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\', type: \'text/javascript\', integrity:\'sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\', crossorigin:\'anonymous\')
        link (rel:\'stylesheet\', href:\'https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\', integrity: \'sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\', crossorigin:\'anonymous\')
        link (rel:\'stylesheet\', href:\'https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css\', integrity: \'sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp\', crossorigin:\'anonymous\')
        style(type:"text/css", \'\'\' body{margin:0;font-family:Arial, Helvetica, sans-serif;}.topnav{overflow:hidden;background-color:#333;}.topnav a{float:left;color:#f2f2f2;text-align:center;padding:10px 16px;text-decoration:none;font-size:17px;} \'\'\')

        style(type:"text/css", \'\'\'
                .myInput { 
                  background-image: url(\'https://www.w3schools.com//css/searchicon.png\');
                  background-position: 10px 10px;
                  background-repeat: no-repeat;
                  width: 500px;
                  font-size: 16px;
                  padding: 12px 20px 12px 40px;
                  border: 1px solid #ddd;
                  margin-bottom: 12px;
                  float: right;
                }\'\'\');

        script {
            mkp.yieldUnescaped \'\'\'
            (function(document) {
                \'use strict\';
                var LightTableFilter = (function(Arr) {
                    var _input;
                    function _onInputEvent(e) {
                        _input = e.target;
                        var tables = document.getElementsByClassName(\'reportTable\');
                        Arr.forEach.call(tables, function(table) {
                            Arr.forEach.call(table.tBodies, function(tbody) {
                                Arr.forEach.call(tbody.rows, _filter);
                            });
                        });
                    }
                    function _filter(row) {
                        var text = row.textContent.toLowerCase(), val = _input.value.toLowerCase();
                        row.style.display = text.indexOf(val) === -1 ? \'none\' : \'table-row\';
                    }
                    return {
                        init: function() {
                            var inputs = document.getElementsByClassName(\'light-table-filter\');
                            Arr.forEach.call(inputs, function(input) {
                                input.oninput = _onInputEvent;
                            });
                        }
                    };
                })(Array.prototype);
                document.addEventListener(\'readystatechange\', function() {
                    if (document.readyState === \'complete\') {
                        LightTableFilter.init();
                    }
                });
            })(document);\'\'\'
        }
        
        script {
            mkp.yieldUnescaped \'\'\'
            function sortTable(col) {
              var table, rows, switching, i, x, y, shouldSwitch;
              table = document.getElementById("myTable");
              switching = true;
              while (switching) {
                switching = false;
                rows = table.rows;
                for (i = 1; i < (rows.length - 1); i++) {
                  shouldSwitch = false;
                  x = rows[i].getElementsByTagName("TD")[col];
                  y = rows[i + 1].getElementsByTagName("TD")[col];
                  if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
                    shouldSwitch = true;
                    break;
                  }
                }
                if (shouldSwitch) {
                  rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
                  switching = true;
                }
              }
            }
            \'\'\'
        }
        
        
    }
    body {
    
      div (class:\'topnav\') {
      
          a (href:"/flow") {
            img (src: ReportLogo, width: "100px")
          }
        }
        
        
        
        mkp.yieldUnescaped(\'<!--\')
        mkp.yield(\'<test>\')
        mkp.yieldUnescaped(\'-->\')

        
        
        div (class:\'container-fluid\'){
            div (class:\'row\'){
                div (class:\'col-md-12\'){
                    H2(ReportTitle)
                    input(type: "search", class: "myInput light-table-filter", placeholder: "Filter")
                    table (class:\'reportTable table-hover table-striped table\', id: \'myTable\') {
                        thead () {
                            tr () {
                                def colNum = 0
                                ColumnHeaders.each { ColumnHeader -> 
                                    th()  { button(onclick: "sortTable($colNum);", ColumnHeader) }
                                    colNum = colNum + 1
                                }
                            }
                        }
                        tbody () {
                            TableData.each { row ->
                                tr (class: \'active\', style:\'box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)\') {    
                                    ColumnHeaders.each { column ->
                                        // TODO: add column ornamentation
                                        def contents = row[column]?row[column]:"" // accommodate null
                                        td(){mkp.yieldUnescaped contents}
                                    }
                                }
                         }
                        }
                    }
                }
            }    
        }
    }
}

ef.setProperty(propertyName: (String) "/myJob/reportHtml", value: sb.toString() )

/* Create Directory 
    path = "artifacts" 
*/
File fullPath = new File(\'artifacts\')
if (!fullPath.exists())
    fullPath.mkdirs()

/* Save Report to File
    Path = "artifacts/" + ReportTitle + ".html"
    Append = 1
    AddNewLine = 1
    Content = [/myJob/reportHtml]
*/
def myFile = new File("artifacts/" + ReportTitle + ".html")
myFile.append( sb.toString() + "\\n" )

ef.setProperty(propertyName: (String) "/myJob/report-urls/" + ReportTitle + "", value: "/commander/jobSteps/$[/myJobStep/jobStepId]/" + ReportTitle + ".html" )
println sb.toString()'''
    shell = 'ec-groovy'
    timeLimit = '0'
  }

  // Custom properties

  property 'ec_customEditorData', {

    // Custom properties

    property 'parameters', {

      // Custom properties

      property 'jsonData', {

        // Custom properties
        formType = 'standard'
      }

      property 'reportName', {

        // Custom properties
        formType = 'standard'
      }
    }
  }
}