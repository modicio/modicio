package modic.io

import modic.io.logic.EvolutionService
import modic.io.model.Attribute
import modic.io.model.Node
import modic.io.repository.FragmentRepository
import modic.io.useCaseHelpers.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.sql.Timestamp

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EvolutionIntegrationTests {

    @Autowired
    lateinit var evolutionService: EvolutionService

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    @Test
    fun useCase1Forward() {
        val fragment = UseCase1Helper.getTestDataForward()
        fragmentRepository.save(fragment)
        val evolutionRequest = evolutionService.translateEvolutionRequest("The class Alpha must exist.\n" +
                "Class Alpha should have an attribute Beta. It is a phrase." +
                "It has a value Hello Friend.\n" +
                "Class Alpha has an existing attribute Beta. It must have a value Hello World instead of Hello Friend.")
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode: Node
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
    fun useCase1Backward() {
        val fragment = UseCase1Helper.getTestDataBackward()
        fragmentRepository.save(fragment)
        val preResult = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, preResult.first().model!!.getNodes().size)

        val evolutionRequest = evolutionService.translateEvolutionRequest("The class Alpha must exist.\n" +
                "Class Alpha should have an attribute Beta. It is a phrase." +
                "It has a value Hello Friend.\n" +
                "Class Alpha has an existing attribute Beta. It must have a value Hello World instead of Hello Friend.")
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest, backwards = true)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        Assertions.assertTrue(evolvedFragment.model!!.getNodes().isEmpty())
    }

    @Test
    fun useCase2Forward() {
        val fragment = UseCase2Helper.getTestDataForward()
        fragmentRepository.save(fragment)
        val evolutionRequest = evolutionService.translateEvolutionRequest("Class Gamma must compose class Delta called GammaDeltaComposition.\n" +
                "It should be public.\n" +
                "Class Gamma must have association to Epsilon called GammaEpsilonAssociation.\n" +
                "Gamma must have association GammaEpsilonAssociation up to date 2023-12-01.\n" +
                "Gamma must have association GammaEpsilonAssociation with all versions of variant 2023-10-01.")
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "Gamma") {
                retrievedNode = node
                break
            }
        }

        val retrievedComposition = retrievedNode.getCompositions().first()
        val retrievedAssociation = retrievedNode.getAssociationRelations().first()

        Assertions.assertEquals("GammaDeltaComposition", retrievedComposition.role)
        Assertions.assertEquals("modicio:Delta", retrievedComposition.target)
        Assertions.assertTrue(retrievedComposition.isPublic)

        Assertions.assertEquals("GammaEpsilonAssociation", retrievedAssociation.name)
        Assertions.assertEquals("modicio:Epsilon", retrievedAssociation.target)

        val leftOpen = retrievedAssociation.cInterface?.getLeftOpenDelimiters()?.first()
        val point = retrievedAssociation.cInterface?.getPointDelimiters()?.first()

        Assertions.assertEquals(Timestamp.valueOf("2023-12-01 12:00:00"), leftOpen?.borderVersionTime)
        Assertions.assertEquals(Timestamp.valueOf("2023-10-01 12:00:00"), point?.variantTime)
    }

    @Test
    fun useCase2Backward() {
        val fragment = UseCase2Helper.getTestDataBackward()
        fragmentRepository.save(fragment)
        val evolutionRequest = evolutionService.translateEvolutionRequest("Class Gamma must compose class Delta called GammaDeltaComposition.\n" +
                "It should be public.\n" +
                "Class Gamma must have association to Epsilon called GammaEpsilonAssociation.\n" +
                "Gamma must have association GammaEpsilonAssociation up to date 2023-12-01.\n" +
                "Gamma must have association GammaEpsilonAssociation with all versions of variant 2023-10-01.")
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest, backwards = true)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "Gamma") {
                retrievedNode = node
                break
            }
        }

        Assertions.assertTrue(retrievedNode.getCompositions().isEmpty())
        Assertions.assertTrue(retrievedNode.getAssociationRelations().isEmpty())
    }

    @Test
    fun useCase3() {
        val fragment = UseCase3Helper.getTestDataForward()
        fragmentRepository.save(fragment)
        val evolutionRequest = evolutionService.translateEvolutionRequest("Delete class Tau.\n" +
                "Class Tau should have an attribute Sigma. It is a number.")
        val exceptionMessage = assertThrows<Exception> {
            evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        }
        Assertions.assertEquals("Class does not exist!", exceptionMessage.message)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        Assertions.assertEquals(1, result.first().model!!.getNodes().size)
    }

    @Test
    fun useCase5Forward() {
        val fragment = UseCase5Helper.getTestDataForward()
        fragmentRepository.save(fragment)
        val evolutionRequest = evolutionService.translateEvolutionRequest("Create abstract class Zeta.\n" +
                "Set in class Zeta an attribute Theta. It has a value 42.\n" +
                "Set in class Zeta inheritance from class Alpha.")
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "Zeta") {
                retrievedNode = node
                break
            }
        }
        Assertions.assertEquals(1, retrievedNode.getAttributes().size)

        lateinit var retrievedAttribute: Attribute
        for (attribute in retrievedNode.getAttributes()) {
            if (attribute.name == "Theta") {
                retrievedAttribute = attribute
                break
            }
        }

        val concretization = retrievedNode.getConcretizations().first()
        Assertions.assertEquals("Default", retrievedAttribute.dType)
        Assertions.assertEquals("42", concretization.attributeInstance!!.anyValue)
        Assertions.assertEquals(retrievedAttribute.uri, concretization.attributeInstance!!.attributeUri)

        Assertions.assertEquals("modicio:Alpha", retrievedNode.getParentRelations().first().uri)
    }

    @Test
    fun useCase5Backward() {
        val fragment = UseCase5Helper.getTestDataBackward()
        fragmentRepository.save(fragment)
        val evolutionRequest = evolutionService.translateEvolutionRequest("Create abstract class Zeta.\n" +
                "Set in class Zeta an attribute Theta. It has a value 42.\n" +
                "Set in class Zeta inheritance from class Alpha.")
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest, backwards = true)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        Assertions.assertEquals(1, evolvedFragment.model!!.getNodes().size)
        Assertions.assertEquals("Alpha", evolvedFragment.model!!.getNodes().first().name)
    }

    @Test
    fun useCase6Forward() {
        val fragment = UseCase6Helper.getTestDataForward()
        fragmentRepository.save(fragment)
        val evolutionRequest = evolutionService.translateEvolutionRequest("Create new class Chi.\n" +
                "Class Chi should have an attribute Psi. It is a phrase.\n" +
                "It has a value Lorem ipsum igitum.\n" +
                "Set in class Chi association to class Omega called ChiOmegaAssociation.\n" +
                "Set in class Chi association ChiOmegaAssociation starting from date 2021-09-08.\n" +
                "Chi must have association ChiOmegaAssociation with version range from 2022-10-01 to 2022-11-10.\n" +
                "Change in Chi an existing attribute Psi. It must have a name Kappa instead of name Psi.")
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "Chi") {
                retrievedNode = node
                break
            }
        }

        Assertions.assertEquals(1, retrievedNode.getAttributes().size)

        lateinit var retrievedAttribute: Attribute
        for (attribute in retrievedNode.getAttributes()) {
            if (attribute.name == "Kappa") {
                retrievedAttribute = attribute
                break
            }
        }

        val concretization = retrievedNode.getConcretizations().first()
        Assertions.assertEquals("String", retrievedAttribute.dType)
        Assertions.assertEquals("Lorem ipsum igitum", concretization.attributeInstance!!.anyValue)
        Assertions.assertEquals(retrievedAttribute.uri, concretization.attributeInstance!!.attributeUri)



        val retrievedAssociation = retrievedNode.getAssociationRelations().first()

        Assertions.assertEquals("ChiOmegaAssociation", retrievedAssociation.name)
        Assertions.assertEquals("modicio:Omega", retrievedAssociation.target)

        val rightOpen = retrievedAssociation.cInterface?.getRightOpenDelimiters()?.first()
        val region = retrievedAssociation.cInterface?.getIntervalDelimiters()?.first()

        Assertions.assertEquals(Timestamp.valueOf("2021-09-08 12:00:00"), rightOpen?.borderVersionTime)
        Assertions.assertEquals(Timestamp.valueOf("2022-10-01 12:00:00"), region?.leftBorderVersionTime)
        Assertions.assertEquals(Timestamp.valueOf("2022-11-10 12:00:00"), region?.rightBorderVersionTime)
    }

    @Test
    fun useCase6Backward() {
        val fragment = UseCase6Helper.getTestDataBackward()
        fragmentRepository.save(fragment)

        val preResult = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(2, preResult.first().model!!.getNodes().size)

        val evolutionRequest = evolutionService.translateEvolutionRequest("Create new class Chi.\n" +
                "Class Chi should have an attribute Psi. It is a phrase.\n" +
                "It has a value Lorem ipsum igitum.\n" +
                "Set in class Chi association to class Omega called ChiOmegaAssociation.\n" +
                "Set in class Chi association ChiOmegaAssociation starting from date 2021-09-08.\n" +
                "Chi must have association ChiOmegaAssociation with version range from 2022-10-01 to 2022-11-10.\n" +
                "Change in Chi an existing attribute Psi. It must have a name Kappa instead of name Psi.")
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest, backwards = true)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        Assertions.assertEquals(1, result.size)

        Assertions.assertEquals(1, result.first().model!!.getNodes().size)

        val retrievedNode = result.first().model!!.getNodes().first()
        Assertions.assertEquals("Omega", retrievedNode.name)

    }


}