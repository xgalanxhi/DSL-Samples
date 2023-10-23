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

procedure 'endevor_CreatePackage', {
  projectName = CurrentProject
  timeLimit = '0'

  formalOutputParameter 'endevorResult'

  formalParameter 'Credentials', {
    orderIndex = '1'
    required = '1'
    type = 'credential'
  }

  formalParameter 'baseURL', defaultValue: 'http://endevor.192.168.17.11.nip.io', {
    orderIndex = '2'
    required = '1'
    type = 'entry'
  }

  formalParameter 'instance', {
    orderIndex = '3'
    required = '1'
    type = 'entry'
  }

  formalParameter 'package', {
    orderIndex = '4'
    required = '1'
    type = 'entry'
  }

  formalParameter 'description', {
    orderIndex = '5'
    required = '1'
    type = 'entry'
  }

  formalParameter 'ewfromdate', {
    orderIndex = '6'
    type = 'entry'
  }

  formalParameter 'esfromtime', {
    orderIndex = '7'
    type = 'entry'
  }

  formalParameter 'ewtodate', {
    orderIndex = '8'
    type = 'entry'
  }

  formalParameter 'ewtotime', {
    orderIndex = '9'
    type = 'entry'
  }

  formalParameter 'packageType', {
    orderIndex = '10'
    type = 'entry'
  }

  formalParameter 'sharable', defaultValue: 'no', {
    checkedValue = 'yes'
    orderIndex = '11'
    type = 'checkbox'
    uncheckedValue = 'no'
  }

  formalParameter 'backout', defaultValue: 'yes', {
    checkedValue = 'yes'
    orderIndex = '12'
    required = '1'
    type = 'checkbox'
    uncheckedValue = 'no'
  }

  formalParameter 'append', defaultValue: 'no', {
    checkedValue = 'yes'
    orderIndex = '13'
    type = 'checkbox'
    uncheckedValue = 'no'
  }

  formalParameter 'promotion', defaultValue: 'no', {
    checkedValue = 'yes'
    orderIndex = '14'
    type = 'checkbox'
    uncheckedValue = 'no'
  }

  formalParameter 'ewfromtime', {
    orderIndex = '15'
    type = 'entry'
  }

  formalParameter 'fromPackage', {
    orderIndex = '16'
    type = 'entry'
  }

  formalParameter 'doNotValidate', defaultValue: 'false', {
    checkedValue = 'true'
    orderIndex = '17'
    required = '1'
    type = 'checkbox'
    uncheckedValue = 'false'
  }

  formalParameter 'fromMember', {
    orderIndex = '18'
    type = 'entry'
  }

  formalParameter 'fromDSN', {
    orderIndex = '19'
    type = 'entry'
  }

  step 'CreatePackage', {
    command = '''import com.electriccloud.client.groovy.ElectricFlow
import groovy.json.*

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

/********************************************************************
 *  Json pretty
 */
def pretty = { obj ->
	return JsonOutput.prettyPrint(JsonOutput.toJson(obj))
}

/********************************************************************
 */
def instance =  java.net.URLEncoder.encode("$[instance]", "UTF-8")
def packageName =  java.net.URLEncoder.encode("$[package]", "UTF-8")
def endevorUrl = "$[baseURL]/EndevorService/rest/" + instance + "/packages/" + packageName 

postData = [:]
def description =  java.net.URLEncoder.encode("$[description]", "UTF-8")
if (description != "") {
    postData[\'description\'] = description
}

def ewfromdate =  java.net.URLEncoder.encode("$[ewfromdate]", "UTF-8")
if ( ewfromdate != "") {
    postData[\'ewfromdate\'] = ewfromdate
}

def ewfromtime =  java.net.URLEncoder.encode("$[ewfromtime]", "UTF-8")
if (ewfromtime != "") {
    postData[\'ewfromtime\'] = ewfromtime
}

def ewtodate =  java.net.URLEncoder.encode("$[ewtodate]", "UTF-8")
if (ewtodate != "") {
    postData[\'ewtodate\'] = ewtodate
}

def ewtotime =  java.net.URLEncoder.encode("$[ewtotime]", "UTF-8")
if (ewtotime != "") {
    postData[\'ewtotime\'] = ewtotime
}

def packageType =  java.net.URLEncoder.encode("$[packageType]", "UTF-8")
if (packageType != "") {
    postData[\'packageType\'] = packageType
}

def fromPackage =  java.net.URLEncoder.encode("$[fromPackage]", "UTF-8")
if (fromPackage != "") {
    postData[\'fromPackage\'] = fromPackage
}

def sharable =  java.net.URLEncoder.encode("$[sharable]", "UTF-8")
if (sharable != "") {
    postData[\'sharable\'] = sharable
}

def backout =  java.net.URLEncoder.encode("$[backout]", "UTF-8")
if (backout != "") {
    postData[\'backout\'] = backout
}

def append =  java.net.URLEncoder.encode("$[append]", "UTF-8")
if (append != "") {
    postData[\'append\'] = append
}

def promotion =  java.net.URLEncoder.encode("$[promotion]", "UTF-8")
if (promotion != "") {
    postData[\'promotion\'] = promotion
}

def fromDSN =  java.net.URLEncoder.encode("$[fromDSN]", "UTF-8")
if (fromDSN != "") {
    postData[\'fromDSN\'] = fromDSN
}

def fromMember =  java.net.URLEncoder.encode("$[fromMember]", "UTF-8")
if (fromMember != "") {
    postData[\'fromMember\'] = fromMember
}

def doNotValidate =  java.net.URLEncoder.encode("$[doNotValidate]", "UTF-8")
if (doNotValidate != "") {
    postData[\'doNotValidate\'] = doNotValidate
}


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
    connection.requestMethod = \'POST\'
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")
    def body = pretty(postData)
    connection.getOutputStream().write(body.getBytes());
    println body
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
      formalParameterName = '/projects/endevor/procedures/endevor_CreatePackage/formalParameters/Credentials'
    }
  }

  // Custom properties

  property 'ec_customEditorData', {

    // Custom properties

    property 'parameters', {

      // Custom properties

      property 'append', {

        // Custom properties

        property 'checkedValue', value: 'yes'
        formType = 'standard'

        property 'uncheckedValue', value: 'no'
      }

      property 'backout', {

        // Custom properties

        property 'checkedValue', value: 'yes'
        formType = 'standard'

        property 'uncheckedValue', value: 'no'
      }

      property 'doNotValidate', {

        // Custom properties

        property 'checkedValue', value: 'true'
        formType = 'standard'

        property 'uncheckedValue', value: 'false'
      }

      property 'promotion', {

        // Custom properties

        property 'checkedValue', value: 'yes'
        formType = 'standard'

        property 'uncheckedValue', value: 'no'
      }

      property 'sharable', {

        // Custom properties

        property 'checkedValue', value: 'yes'
        formType = 'standard'

        property 'uncheckedValue', value: 'no'
      }
    }
  }
}