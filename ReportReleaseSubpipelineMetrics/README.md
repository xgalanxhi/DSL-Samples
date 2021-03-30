# Release metrics from subpipelines

This example illustrates how to use Release Pipeline output parameters to capture metrics from
subpipelines kicked off by the Release. This is done by having the subpipelines set these Release
Pipeline output parameters. Pipeline output parameters are automatically pushed to the reporting
database for are pipeline runtime events once the parameters have been set.

## Instructions
1. Apply the DSL file CreateRunRelease.groovy
	`ectool evalDsl --dslFile CreateRunRelease.groovy`
	or load this file into the DSLIDE and run it
1. Apply the DSL file CreateReport.groovy
	`ectool evalDsl --dslFile CreateReport.groovy`
	or load this file into the DSLIDE and run it
1. Examine the Dashboard DSL-Samples :: Release SubPipeline Metrics

What the DSL does
1. Creates a Release with output parameters (Sub1 and Sub2) and kicks off two subpipelines (Sub1 and Sub2)
1. Creates the subpipelines (Sub1 and Sub2) which set the parent Release output paramters, Sub1 and Sub2
	respectively
1. Runs the releases
1. Creates a Report that filters for pipeline runtime events where these parameters are set and aggregations
	the Releases and the parameters, only returning one entry per parameter
1. Creates a dashboard with a widget that displays each Release with its respective output parameter values
