package modic.io.service

import modic.io.TestDataHelper
import modic.io.logic.EvolutionService
import modic.io.model.Attribute
import modic.io.model.Node
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.sql.Timestamp

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EvolutionServiceTests {


    @Autowired
    lateinit var evolutionService: EvolutionService

    @Autowired
    lateinit var fragmentRepository: FragmentRepository


    @Test
    fun createClassWithAttribute() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(fragment)
        val evolutionRequest = "CREATE CLASS TestClass/DELETE CLASS TestClass,OPEN CLASS TestClass/CLOSE CLASS TestClass,&" +
                "ADD ATTRIBUTE testAttribute/DELETE ATTRIBUTE testAttribute,&" +
                "OPEN ATTRIBUTE testAttribute,SET TYPE number,CLOSE ATTRIBUTE testAttribute,CLOSE CLASS TestClass/OPEN CLASS TestClass,"
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "TestClass") {
                retrievedNode = node
                break
            }
        }
        Assertions.assertEquals("TestClass", retrievedNode.name)
        Assertions.assertEquals("modicio:TestClass", retrievedNode.uri)
        Assertions.assertEquals(1, retrievedNode.getAttributes().size)

        val retrievedAttribute = retrievedNode.getAttributes().first()
        Assertions.assertEquals("testAttribute", retrievedAttribute.name)
        Assertions.assertEquals("modicio:testAttribute", retrievedAttribute.uri)
        Assertions.assertEquals("Integer", retrievedAttribute.dType)
    }

    @Test
    fun createAbstractClass() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(fragment)
        val evolutionRequest1 = "CREATE ABSTRACT CLASS TestClass/DELETE CLASS TestClass,"
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest1)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "TestClass") {
                retrievedNode = node
                break
            }
        }
        //3 classes after creating the new one
        Assertions.assertEquals(3, nodes.size)
        Assertions.assertEquals("TestClass", retrievedNode.name)
        Assertions.assertTrue(retrievedNode.getIsAbstract())
    }

    @Test
    fun deleteClass() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(fragment)
        val evolutionRequest2 = "DELETE CLASS Todo/CREATE CLASS Todo,"
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest2)
        val result2 = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        val evolvedFragment2 = result2.first()
        val nodes2 = evolvedFragment2.model!!.getNodes()

        //only 1 class left after deleting another one
        Assertions.assertEquals(1, nodes2.size)
    }

    @Test
    fun createAttributeWithTypeAndValue() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(fragment)
        val evolutionRequest = "OPEN CLASS Project/CLOSE CLASS Project," +
        "ADD ATTRIBUTE Beta/DELETE ATTRIBUTE Beta," +
        "OPEN ATTRIBUTE Beta," +
        "SET TYPE number," +
        "SET ATTRIBUTE VALUE TO 13," +
        "CLOSE ATTRIBUTE Beta," +
        "CLOSE CLASS Project/OPEN CLASS Project,"
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "Project") {
                retrievedNode = node
                break
            }
        }
        Assertions.assertEquals(2, retrievedNode.getAttributes().size)

        lateinit var retrievedAttribute: Attribute
        for (attribute in retrievedNode.getAttributes()) {
            if (attribute.name == "Beta") {
                retrievedAttribute = attribute
                break
            }
        }

        val concretization = retrievedNode.getConcretizations().first()
        Assertions.assertEquals("Beta", retrievedAttribute.name)
        Assertions.assertEquals("Integer", retrievedAttribute.dType)
        Assertions.assertEquals("13", concretization.attributeInstance!!.anyValue)
        Assertions.assertEquals(retrievedAttribute.uri, concretization.attributeInstance!!.attributeUri)
    }

    @Test
    fun createClassWithAttributeAndTypeAndValue() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(fragment)
        val evolutionRequest = "CREATE CLASS Alpha/DELETE CLASS Alpha,&" +
        "START OPTIONAL," +
        "OPEN CLASS Alpha/CLOSE CLASS Alpha," +
        "ADD ATTRIBUTE Beta/DELETE ATTRIBUTE Beta," +
        "OPEN ATTRIBUTE Beta," +
        "SET TYPE phrase," +
        "SET ATTRIBUTE VALUE TO Hello Friend," +
        "CLOSE ATTRIBUTE Beta," +
        "CLOSE CLASS Alpha/OPEN CLASS Alpha," +
        "END OPTIONAL,&" +
        "OPEN CLASS Alpha/CLOSE CLASS Alpha," +
        "OPEN ATTRIBUTE Beta/CLOSE ATTRIBUTE Beta," +
        "SET ATTRIBUTE VALUE TO Hello World/SET ATTRIBUTE VALUE TO Hello Friend," +
        "CLOSE ATTRIBUTE Beta/OPEN ATTRIBUTE Beta," +
        "CLOSE CLASS Alpha/OPEN CLASS Alpha,"
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "Alpha") {
                retrievedNode = node
                break
            }
        }
        Assertions.assertEquals(1, retrievedNode.getAttributes().size)

        lateinit var retrievedAttribute: Attribute
        for (attribute in retrievedNode.getAttributes()) {
            if (attribute.name == "Beta") {
                retrievedAttribute = attribute
                break
            }
        }

        val concretization = retrievedNode.getConcretizations().first()
        Assertions.assertEquals("String", retrievedAttribute.dType)
        Assertions.assertEquals("Hello World", concretization.attributeInstance!!.anyValue)
        Assertions.assertEquals(retrievedAttribute.uri, concretization.attributeInstance!!.attributeUri)
    }

    @Test
    fun createCompositionAndAssociationAndEditVersionVariant() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(fragment)
        val evolutionRequest = "OPEN CLASS Todo/CLOSE CLASS Todo," +
        "ADD COMPOSITION TodoProjectComposition TARGET demo.project/DELETE COMPOSITION TodoProjectComposition," +
        "START OPTIONAL," +
        "MAKE COMPOSITION PUBLIC/MAKE COMPOSITION PRIVATE," +
        "END OPTIONAL," +
        "CLOSE CLASS Todo/OPEN CLASS Todo,&" +
        "OPEN CLASS Todo/CLOSE CLASS Todo," +
        "ADD ASSOCIATION TodoProjectAssociation TARGET demo.project/DELETE ASSOCIATION TodoProjectAssociation," +
        "CLOSE CLASS Todo/OPEN CLASS Todo,&" +
        "OPEN CLASS Todo/CLOSE CLASS Todo," +
        "OPEN ASSOCIATION TodoProjectAssociation," +
        "SET VERSION STARTING FROM DATE 2023-12-01," +
        "CLOSE ASSOCIATION TodoProjectAssociation," +
        "CLOSE CLASS Todo/OPEN CLASS Todo,&" +
        "OPEN CLASS Todo/CLOSE CLASS Todo," +
        "OPEN ASSOCIATION TodoProjectAssociation," +
        "SET COMPATIBLE WITH ALL VERSIONS OF VARIANT 2023-10-01," +
        "CLOSE ASSOCIATION TodoProjectAssociation," +
        "CLOSE CLASS Todo/OPEN CLASS Todo,"
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "Todo") {
                retrievedNode = node
                break
            }
        }

        val retrievedComposition = retrievedNode.getCompositions().first()
        val retrievedAssociation = retrievedNode.getAssociationRelations().first()

        Assertions.assertEquals("TodoProjectComposition", retrievedComposition.role)
        Assertions.assertEquals("modicio:demo.project", retrievedComposition.target)
        Assertions.assertTrue(retrievedComposition.isPublic)

        Assertions.assertEquals("TodoProjectAssociation", retrievedAssociation.name)
        Assertions.assertEquals("modicio:demo.project", retrievedAssociation.target)

        val rightOpen = retrievedAssociation.cInterface?.getRightOpenDelimiters()?.first()
        val point = retrievedAssociation.cInterface?.getPointDelimiters()?.first()

        Assertions.assertEquals(Timestamp.valueOf("2023-12-01 12:00:00"), rightOpen?.borderVersionTime)
        Assertions.assertEquals(Timestamp.valueOf("2023-10-01 12:00:00"), point?.variantTime)
    }

}