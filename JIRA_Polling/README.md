# pollingJiraStatus

## Description

The `pollingJiraStatus.groovy` script will add a procedure that will poll JIRA for a status change to a ticket.

### Installing

* Edit `pollingJiraStatus.groovy` and change `CurrentProject` to the project you want to install to

```
def CurrentProject = 'dslsamples'

project CurrentProject, {
  tracked = '1'

  procedure 'Poll Jira for Target Status', {
...
...
...
```

* Run the DSL `pollingJiraStatus.groovy` to install

## Example

<<Example of running the sample>>