# DSL-Samples

This is a collection of DSL samples that illustrate how to implement a variety of usecases wtih CloudBees CD.

In general, these samples can be installed in a CloudBees CD instance using either the DSL
IDE or `ectool evalDsl --dslFile <filename>` from the command line. See the comments in the
DSL files for details on their use.

## [Export Report Data](ExportReportData.groovy)
Create a self-service catalog item that can be used to generate a tab-delimited file from a DevOps Insight
widget (report).
- ![Application Dashboard Widget](/images/Report%20Data/ApplicationDeploymentWidget.png)
- ![Self-service catalog entry](/images/Report%20Data/SelfserviceCatalogEntry.png)
- ![Job Details with link](/images/Report%20Data/JobDetails.png)
- ![Raw TAB data](/images/Report%20Data/RawTabData.png)
- ![In MS Excel](/images/Report%20Data/Excel.png)
