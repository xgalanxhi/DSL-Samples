/*

Example of using DSL to invoke findObjects API

Here's an example of deleteObjects with a filter from the command line
ectool deleteObjects application --filters "{ propertyName=>'applicationId', operator=>'equals', operand1=>'059999bf-a611-11ec-ae3b-0a0027000005' }"

Find all the non-deploy jobs from the project "Default"
ectool countObjects job --maxIds 10000 --filters "{ propertyName=>'applicationId', operator=>'isNull' },{propertyName=>'projectName',operator=>'equals',operand1=>'Default'}"

Find all triggers
ectool findObjects trigger

Loop through projects to find triggers in each
ectool --format json getProjects | jq -r .project[].projectName | while read -r i
do
    echo Project: $i
    ectool findObjects trigger --filters "{propertyName=>'projectName',operator=>'equals',operand1=>\'$i\'}"
done

*/

// Imports needed for invoking findObjects in the DSL script
import com.electriccloud.query.Filter
import com.electriccloud.query.CompositeFilter
import com.electriccloud.query.PropertyFilter
import com.electriccloud.query.Operator
import com.electriccloud.query.SelectSpec
import com.electriccloud.util.SortOrder
import com.electriccloud.util.SortSpec

import com.fasterxml.jackson.databind.ObjectMapper
import org.dom4j.DocumentHelper

import groovy.json.JsonSlurper

// Make sure that the pipelines that 
// findObjects is looking for do exist

project 'US Test', {
  pipeline 'pipeline-0', {
    testprop1 = 'val1'
  }
  pipeline 'pipeline-1', {
    testprop2 = 'val2'
  }
  pipeline 'pipeline-3', {
    testprop2 = 'val3'
  }
}


/**
 * Sample filter definition for:
 * projectName equals "US Test"
 * AND
 *  (
 *     pipelineName equals "pipeline-0"
 *     or
 *     pipelineName equals "pipeline-1"
 *  )
 */
def filters = [[
                       propertyName: "projectName",
                       operator: "equals",
                       operand1: "US Test"
               ],
               [ filters: [[
                                   propertyName: "pipelineName",
                                   operator: "equals",
                                   operand1: "pipeline-0"
                           ],[
                                   propertyName: "pipelineName",
                                   operator: "equals",
                                   operand1: "pipeline-1"
                           ]],
                 operator: "or"
               ]]

/**
 * Simple selects can be specified simply as strings
 * Use the object structure if recurse option needs to be controlled.
 * The above 2 forms can be completed in the same list of selects.
 */
def selects = ["testprop1", [propertyName: "testprop2", recurse: true]]

/**
 * Sort columns
 */
def sorts = [[propertyName: "pipelineName", order: "descending"], [propertyName: "projectName"]]


// make the call
def result = findObjectsSimplified([objectType: 'pipeline', filters: filters, selects: selects, sorts: sorts])
def response = processFindObjectsResponse(result)

// traverse the response using XPath like syntax
assert response.objectId.size() == 2
assert response.object.size() == 2
assert response.object[0].pipeline.pipelineName == 'pipeline-1'
 
response.object.each { 
  assert it.pipeline.pipelineName in ['pipeline-0','pipeline-1'] 
}
 

// optionally return the infoset-aware findObjects response as the DSL outcome
result


/**
 * Helper function that takes care of converting findObject input arguments to the
 * structure required by the API. 
 */
def findObjectsSimplified(Map args) {
    findObjectsSimplified(args.objectType, args.filters, args.selects, args.sorts)
}

/**
 * Helper function that takes care of converting findObject input arguments to the
 * structure required by the API. 
 * @param objectType
 * @param filters
 * @param selects
 * @param sorts
 */
def findObjectsSimplified(String objectType, def filters = null, def selects = null, def sorts = null) {
    def result = findObjects(objectType: objectType, filter: constructFilters(filters), select: constructSelects(selects), sort: constructSorts(sorts))
}

/**
 * Helper function to convert the list of filters to a filter structure
 * recognized by findObjects for DSL evaluation.
 */
def constructFilters(def filters) {

    filters?.collect { f ->
        def op = Operator.valueOf(f.operator)
        if (op.isBoolean()) {
            new CompositeFilter(op, constructFilters(f.filters) as Filter[])
        } else {
            new PropertyFilter(f.propertyName, op, f.operand1, f.operand2)
        }
    }
}


/**
 * Helper function to convert the list of select strings to a list of SelectSpec
 * recognized by findObjects for DSL evaluation.
 */
def constructSelects(def selects) {
    selects?.collect { s ->
        s instanceof String ? new SelectSpec(s, false) : new SelectSpec(s.propertyName, s.recurse)
    }
}

/**
 * Helper function to convert the list of sort instances to a list of SortSpec
 * recognized by findObjects for DSL evaluation.
 */
def constructSorts(def sorts) {
    sorts?.collect { s ->
        new SortSpec(s.propertyName, SortOrder.valueOf(s.order?:"ascending"))
    }
}

/**
 * Helper function that conversts the findObject response into a 
 * document tree that can be traversed similar to XPath expressions.
 */
def processFindObjectsResponse(def infoset) {
    if (infoset.class.simpleName == 'JsonInfosetResultImpl') { // when called REST
	  def root = infoset.asNode()
	  
	  def mapper = new ObjectMapper();
      String jsonData = mapper.writeValueAsString(root)
      def jsonSlurper = new JsonSlurper()
      jsonSlurper.parseText(jsonData)
	  
	} else { // when called via ectool 
	    def document = DocumentHelper.createDocument();
		def root = document.addElement('result');
		
		infoset.elements?.each { element ->
		  def elementDoc = DocumentHelper.parseText(element.asXML())
		  root.add(elementDoc.getRootElement())
		}
		new XmlSlurper().parseText(root.asXML())
	}
}

