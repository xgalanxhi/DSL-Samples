# This should be moved into the automatic set up process
ectool deleteArtifact com.mybank.apps:fund
ectool deleteProject "On line bank Release"
cd "/vagrant/DSL-Samples/Release Model"
ectool evalDsl --dslFile "assemble.groovy"
ectool runProcedure "On line bank Release" --procedureName "Assemble"
sleep 100
ectool runProcedure "On line bank Release" --procedureName "Create Application" --actualParameter appName="OB - Fund Transfer" artifactGroup="com.mybank.apps" artifactKey="fund" envs="Banking-DEV" projName="On line bank Release" snapEnv="Banking-DEV" version=2.0 runAppCreation=0
