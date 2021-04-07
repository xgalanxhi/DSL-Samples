/*

CloudBees SDA CD/RO DSL: list out all report types and fields

Instructions: run this DSL from the DSLIDE to view the report

_________Build Report (build)______________________
Base Drilldown Url (baseDrilldownUrl STRING)
Build in progress (building BOOLEAN)
...
Tags (tags STRING)
Timestamp (timestamp DATETIME)
_________Build Commit Report (build_commit)______________________
Build Number (buildNumber STRING)
Build System Type (buildSource STRING)
...
Tags (tags STRING)
Timestamp (timestamp DATETIME)

*/
def attributes=[]
getReportObjectTypes().each { ro ->
  attributes.push("_________${ro.displayName} (${ro.reportObjectTypeName})______________________")
  getReportObjectAttributes(reportObjectTypeName: ro.reportObjectTypeName).each { attr ->
    attributes.push("${attr.displayName} (${attr.reportObjectAttributeName} ${attr.type})")
  }
}
attributes.reverse().join('\n')

