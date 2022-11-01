# DSL-Samples

This is a collection of DSL samples that illustrate how to implement a variety of usecases wtih CloudBees CD.

In general, these samples can be installed in a CloudBees CD instance using either the DSL
IDE or `ectool evalDsl --dslFile <filename>` from the command line. See the comments in the
DSL files for details on their use.

## [Dynamic Credentials](Dynamic%20Credentials.groovy)
Dynamic credentials are those that are entered as parameters at runtime and only last the duration of a job or pipeline
 run. These can be used as an alternative to static credentials which are saved to a project. The ideas is that a user
 can be prompted for credentials when they are needed for the execution to pipeline tasks.

- ![Pipeline Definition](/images/Dynamic%20Credentials/Pipeline%20definition.png)
- ![Pipeline run dialog](/images/Dynamic%20Credentials/Pipeline%20run%20dialog.png)
- ![Pipeline runtime](/images/Dynamic%20Credentials/Pipeline%20runtime.png)
- ![Job details](/images/Dynamic%20Credentials/Job%20details.png)
- ![Job step logfile](/images/Dynamic%20Credentials/Job%20step%20logfile.png)

## [Export Report Data](ExportReportData.groovy)
Create a self-service catalog item that can be used to generate a tab-delimited file from a DevOps Insight
widget (report).
- ![Application Dashboard Widget](/images/Report%20Data/ApplicationDeploymentWidget.png)
- ![Self-service catalog entry](/images/Report%20Data/SelfserviceCatalogEntry.png)
- ![Job Details with link](/images/Report%20Data/JobDetails.png)
- ![Raw TAB data](/images/Report%20Data/RawTabData.png)
- ![In MS Excel](/images/Report%20Data/Excel.png)
