import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.*

ElectricFlow ef = new ElectricFlow()

def inputValue = ef.getProperty( propertyName: 'input' ).property.value
println ef.setOutputParameter(outputParameterName: 'outputGroovy', value: inputValue)