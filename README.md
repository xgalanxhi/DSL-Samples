# DSL-Samples

This is a collection of DSL samples that illustrate how to implement a variety of usecases wtih CloudBees CD.

## [Dynamic Credentials](Dynamic%20Credentials.groovy)
Dynamic credentials are those that are entered as parameters at runtime and only last the duration of a job or pipeline
 run. These can be used as an alternative to static credentials which are saved to a project. The ideas is that a user
 can be prompted for credentials when they are needed for the execution to pipeline tasks.

![Pipeline Definition](/images/Dynamic Credentials/Pipeline definition.png)
![Pipeline run dialog](/images/Dynamic Credentials/Pipeline run dialog.png)
![Pipeline runtime](/images/Dynamic Credentials/Pipeline runtime.png)
![Job details](/images/Dynamic Credentials/Job details.png)
