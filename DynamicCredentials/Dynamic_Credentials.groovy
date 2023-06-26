/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------

Cloudbees Flow DSL: Use of dynamic credentials parameters

Dynamic credentials are those that are entered as parameters at runtime and only last the duration of a job or pipeline
 run. These can be used as an alternative to static credentials which are saved to a project. The ideas is that a user
 can be prompted for credentials when they are needed for the execution to pipeline tasks.

*/

def CurrentProject = 'dslsamples'
def Credentials = "creds"
def ProcedureName = 'Dynamic Credentials'

project CurrentProject,{
	procedure ProcedureName, {
		formalParameter Credentials, type: 'credential'
		step 'Use Credentials',{
			command = """\
				user=\$(ectool getFullCredential ${Credentials} --value userName)
				pass=\$(ectool getFullCredential ${Credentials} --value password)
				
				# Sample command below to show successful passing of the credentials. Make sure
				# not to echo out the hidden \${user} and \${pass} values in you work.
				echo "User: \${user}, Password: \${pass}"
			""".stripIndent()
			attachParameter formalParameterName : Credentials
		}
  	}
	pipeline 'Use Dynamic Credentials', {
		formalParameter Credentials, type: 'credential', required: true
		stage 'QA', {
			task 'Run credential procedure', {
				actualParameter = [
						creds: Credentials,
				]
				subprocedure = ProcedureName
				subproject = projectName
				taskType = 'PROCEDURE'
				attachParameter formalParameterName : Credentials
			}
		}
	}
}
