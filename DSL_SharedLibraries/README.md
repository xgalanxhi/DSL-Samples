# DSL Shared Libraries
This example illustrates how to create reusable CloudBees CD/RO DSL code.
The file sharedFiles/com/electriccloud/commander/dsl/sample/DslBaseScript.groovy contains the top level resusable code which references other files in this directory tree.

## Dependencies
- Requires CloudBees CD/RO version 10.3 or later

## Instructions
- Add your own resusable code to DslBaseScript.groovy and update referenceScript.groovy to use it
- Apply the referenceScript.groovy
    ectool evalDsl --dslFile referenceScript.groovy --clientFiles ./sharedFiles