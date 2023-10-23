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

pipeline 'Endevor Test', {
  projectName = CurrentProject

  formalParameter 'myPackage', defaultValue: 'PKG1', {
    orderIndex = '1'
    required = '1'
    type = 'entry'
  }

  formalParameter 'ec_stagesToRun', {
    expansionDeferred = '1'
  }

  stage 'DEV', {
    colorCode = '#289ce1'
    pipelineName = 'Endevor Test'
    task 'Create Package', {
      actualParameter = [
        'append': 'no',
        'backout': 'yes',
        'baseURL': 'http://endevor.192.168.17.11.nip.io/',
        'Credentials': '/projects/endevor/credentials/EndevorCredentials',
        'description': 'This is my package',
        'doNotValidate': 'false',
        'esfromtime': '',
        'ewfromdate': '',
        'ewfromtime': '',
        'ewtodate': '',
        'ewtotime': '',
        'fromDSN': '',
        'fromMember': '',
        'fromPackage': '',
        'instance': 'DEV',
        'package': '$[myPackage]',
        'packageType': '',
        'promotion': 'no',
        'sharable': 'no',
      ]
      subprocedure = 'endevor_CreatePackage'
      subproject = 'endevor'
      taskType = 'PROCEDURE'

      attachCredential {
        credentialName = '/projects/endevor/credentials/EndevorCredentials'
      }
    }

    task 'Approve Package', {
      actualParameter = [
        'baseURL': 'http://endevor.192.168.17.11.nip.io',
        'Credentials': '/projects/endevor/credentials/EndevorCredentials',
        'instance': 'DEV',
        'package': '$[myPackage]',
      ]
      subprocedure = 'endevor_ApprovePackage'
      subproject = 'endevor'
      taskType = 'PROCEDURE'

      attachCredential {
        credentialName = '/projects/endevor/credentials/EndevorCredentials'
      }
    }

    task 'Execute Package', {
      actualParameter = [
        'baseURL': 'http://endevor.192.168.17.11.nip.io',
        'Credentials': '/projects/endevor/credentials/EndevorCredentials',
        'esfromtime': '',
        'ewfromdate': '',
        'ewfromtime': '',
        'ewtodate': '',
        'ewtotime': '',
        'instance': 'DEV',
        'package': '$[myPackage]',
        'status': '',
      ]
      subprocedure = 'endevor_ExecutePackage'
      subproject = 'endevor'
      taskType = 'PROCEDURE'

      attachCredential {
        credentialName = '/projects/endevor/credentials/EndevorCredentials'
      }
    }

    task 'List Packages Default', {
      actualParameter = [
        'baseURL': 'http://endevor.192.168.17.11.nip.io/',
        'Credentials': '/projects/endevor/credentials/EndevorCredentials',
        'enterprise': '',
        'instance': ' DEV',
        'packageType': '',
        'promotion': '',
        'status': 'READY',
      ]
      subprocedure = 'endevor_ListPackages'
      subproject = 'endevor'
      taskType = 'PROCEDURE'

      attachCredential {
        credentialName = '/projects/endevor/credentials/EndevorCredentials'
      }
    }
  }

  // Custom properties

  property 'ec_counters', {

    // Custom properties
    pipelineCounter = '9'
  }
}