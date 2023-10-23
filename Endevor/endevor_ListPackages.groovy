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

procedure 'endevor_ListPackages', {
  projectName = CurrentProject
  timeLimit = '0'

  formalOutputParameter 'endevorResult'

  formalParameter 'Credentials', {
    orderIndex = '1'
    required = '1'
    type = 'credential'
  }

  formalParameter 'baseURL', defaultValue: 'http://endevor.192.168.17.11.nip.io/', {
    orderIndex = '2'
    required = '1'
    type = 'entry'
  }

  formalParameter 'instance', {
    orderIndex = '3'
    required = '1'
    type = 'entry'
  }

  formalParameter 'status', {
    orderIndex = '4'
    type = 'entry'
  }

  formalParameter 'packageType', {
    orderIndex = '5'
    type = 'entry'
  }

  formalParameter 'enterprise', {
    orderIndex = '6'
    type = 'entry'
  }

  formalParameter 'promotion', {
    orderIndex = '7'
    type = 'entry'
  }

  step 'ListPackages', {
    command = '''import com.electriccloud.client.groovy.ElectricFlow
import groovy.json.JsonSlurper

/********************************************************************
 *  Json Scan to Properties
 */
void scan(org.apache.groovy.json.internal.LazyMap object, String path) {
    object.each { k, v ->
        scan(v, "${path}/${k}")
    }
}
void scan(java.util.ArrayList object, String path) {
    object.eachWithIndex { v, i ->
        scan(v, "${path}/${i}")
    }
}
void scan(object, String path) {
    println "${path} ${object}"
    ef.setProperty(propertyName: "${path}", value: "${object}")
}

def endevorUrl = "$[baseURL]/EndevorService/rest/$[instance]&status=$[status]&type=$[packageType]&enterprise=$[enterprise]&promotion=$[promotion]"
def contentType = "text/html"
println "URL = " + endevorUrl

ef = new ElectricFlow()
def result = ef.getFullCredential(
                jobStepId: \'/myJobStep\',
                credentialName: \'Credentials\')
           
def user = result.credential.userName
def password = result.credential.password

String auth = user + ":" + password;
byte[] encodedAuth = auth.bytes.encodeBase64().toString()
String authHeaderValue = "Basic " + encodedAuth

try {
    def site = new URL(endevorUrl)
    def connection = site.openConnection()
    connection.setRequestProperty("Authorization", authHeaderValue)
    connection.requestMethod = \'GET\'
    if (connection.responseCode == 200) {
        println "Response Code " + connection.responseCode
        def results = connection.content.text
        println "Content type " + connection.contentType
        println "Last modified " + connection.lastModified
        def Slurper = new JsonSlurper()
        def responseObj = Slurper.parseText(results)
        println "Return Code: " + responseObj.returnCode
        if ( responseObj.reasonCode != "0000" ) {
            throw new Exception("Return Code: " + responseObj.returnCode)
        }
        println "Reason Code: " + responseObj.reasonCode
        if ( responseObj.reasonCode != "0000" ) {
            throw new Exception("Return Code: " + responseObj.reasonCode)
        }
        scan(responseObj.data, "/myJob/packages")
    } else {
        println connection.responseCode
    }
} catch(e) {
    println e.toString()
    exit -1
}

'''
    shell = 'ec-groovy'
    timeLimit = '0'
    timeLimitUnits = 'seconds'

    attachParameter {
      formalParameterName = '/projects/endevor/procedures/endevor_ListPackages/formalParameters/Credentials'
    }
  }
}