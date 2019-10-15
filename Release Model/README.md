# Application release sample

## Platform Requirements
ElectricFlow 6.1

## Standard Instructions

* Retrieve these file to a location on the EF server
* Edit the "dslDir" in assemble.groovy to point to where this file is located on the EF server
* Run the following from the command line on the EF server

```
ectool login <user> <password>
ectool evalDsl --dslFile "assemble.groovy"
ectool runProcedure "On line bank Release" --procedureName "Assemble"
```

* Once this procedure has finished executing, you should see the new Release "Quarterly Online Banking Release"
* Also created is a procedure to remove all generated objects for this release model, use this to clean up
* Run the release model through production
* Create a new artifact version for one of the applications:
```
ectool runProcedure "On line bank Release" --procedureName "Create Application" --actualParameter \
appName="OB - Fund Transfer" \
artifactGroup="com.mybank.apps" \
artifactKey="fund" \
envs="Banking-DEV" \
projName="On line bank Release" \
snapEnv="Banking-DEV" \
version=2.0 \
runAppCreation=0
```
* Update the application section of the release to use this new snapshot version, 2.0
* Run through one stage of the release pipeline and note the Path to Production view

## Instructions When using flow-demo
If you have flow-demo installed, run procedure "On line bank Release"::"Assemble" from the UI
