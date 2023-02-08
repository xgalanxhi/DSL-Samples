/*
    This is a sample to create a Configuration for a plugin lioke EC-ServiceNow
*/

def conf="/projects/Holy/pluginConfigurations/githubWebhookTriggerConf"
def proj="/plugins/EC-Github/project"
def uName=''
def pwd='abcdef'        // or you would better grab it from an extertnal source

// Create a Transient credential
def Cred = new RuntimeCredentialImpl()		
Cred.name = 'webhookSecret_credential'	        
Cred.userName = uName		
Cred.password = pwd
def Creds=[Cred]

runProcedure(
  projectName : proj,
  procedureName : "SetupWebhook",
  actualParameter : [
    configuration: conf,               // required
    credentialsType: "configuration",  // required
    ec_action: "create",               // required
    ec_trigger: "45c769c9-7137-11ed-bc03-26079f782ee8", //required
    githubApi_credential: null,
    insecureSsl: 'true',           // Credential has the same name than the config
    webhookSecret_credential: "webhookSecret_credential"
  ],
  credential: Creds
)

