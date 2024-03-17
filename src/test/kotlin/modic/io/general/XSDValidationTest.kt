package modic.io.general

import modic.io.TestDataHelper
import modic.io.model.Fragment
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class XSDValidationTest {

    @Test
    fun validateToXSDTest(){

        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()

        val xml = Fragment.marshallFragment(fragment)

        println(Fragment.transformXMLtoPlantUML(xml))
    }
}