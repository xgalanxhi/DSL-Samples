/*

CloudBees CDRO DSL: Extract credential value in a DSL step

*/

procedure 'Get full credentials with DSL', {
  projectName = 'Default'

  formalParameter 'cred', defaultValue: '', {
    required = '1'
    type = 'credential'
  }

  step 'Get password', {
    command = 'property "/myJob/cred", value: getFullCredential(credentialName:"cred", jobStepId: "$[/myJobStep/jobStepId]").password'
    shell = 'ectool evalDsl --dslFile'
    attachParameter {
      formalParameterName = '/projects/Default/procedures/Get full credentials with DSL/formalParameters/cred'
    }
  }
}