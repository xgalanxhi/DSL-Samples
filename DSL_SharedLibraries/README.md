# DSL Shared Libraries
DSL is evaluated on the CloudBees CD/RO server. If the DSL run by the evalDsl command contains file
references, these files need to be on the server file system. The clientFiles argument can be used
to push files from the client where the evalDsl API is run to the server.
This example illustrates how to use the clientFiles argument to create reusable CloudBees CD/RO DSL code.
The file sharedFiles/com/electriccloud/commander/dsl/sample/DslBaseScript.groovy contains the top level resusable code which references other files in this directory tree.

## Dependencies
- Requires CloudBees CD/RO version 10.3 or later
- Uses Stomp protocol. Currently cannot be proxied, so client or client have to have direct access to server.

## Instructions
- Add your own resusable code to DslBaseScript.groovy and update referenceScript.groovy to use it
- Apply the referenceScript.groovy
    `ectool evalDsl --dslFile referenceScript.groovy --clientFiles ./sharedFiles`
