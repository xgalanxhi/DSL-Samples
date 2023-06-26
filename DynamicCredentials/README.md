# Property Browsers

## Description

Dynamic credentials are those that are entered as parameters at runtime and only last the duration of a job or pipeline run. These can be used as an alternative to static credentials which are saved to a project. The ideas is that a user
 can be prompted for credentials when they are needed for the execution to pipeline tasks.

### Installing

* Edit `Dynamic_Credentials.groovy` and change the default properties to meet your needs as follows:

    * `CurrentProject` - The project 
    * `creds` - The actual credentials
    * `ProcedureName`  - The name of this procedure

```
def CurrentProject = 'dslsamples'
def Credentials = "creds"
def ProcedureName = 'Dynamic Credentials'

procedure 'projectTrace', {
  projectName = CurrentProject
  timeLimit = '0'
...
...
...
```

* Run the DSL `Dynamic_Credentials.groovy` to install

## Example

 
- ![Pipeline Definition](img/Pipeline%20definition.png)
- ![Pipeline run dialog](img/Pipeline%20run%20dialog.png)
- ![Pipeline runtime](img/Pipeline%20runtime.png)
- ![Job details](img/Job%20details.png)
- ![Job step logfile](img/Job%20step%20logfile.png)

