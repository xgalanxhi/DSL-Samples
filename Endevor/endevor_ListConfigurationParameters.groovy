/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------

CloudBees CD DSL: Interface to Endevor Mainframe service

TODO: Documentation

 */

def CurrentProject = 'dslsamples'

procedure 'endevor_ListConfigurationParameters', {
  projectName = CurrentProject
  timeLimit = '0'

  step 'ListConfigurationParameters', {
    command = '''def getURL( String url, String contentType) {
  def site = new URL(url)
  def connection = site.openConnection()
  connection.requestMethod = \\\'GET\\\'
  if (connection.responseCode == 200) {
    println "Response Code " + connection.responseCode
    println "Content \\\\n" + connection.content.text
    println "Content type " + connection.contentType
    println "Last modified " + connection.lastModified
    // connection.headerFields.each { println "> ${it}"}
  } else {
    println connection.responseCode
  }
}

def url = "http://endevor.192.168.17.11.nip.io/"
def contentType = "text/html"

getURL( url, contentType )\'\'\'
    shell = \'ec-groovy\'
    timeLimit = \'0\'
    timeLimitUnits = \'seconds\'
'''
    }
}