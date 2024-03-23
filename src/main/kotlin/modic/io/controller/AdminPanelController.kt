package modic.io.controller

import modic.io.logic.ModelService
import modic.io.model.Fragment
import modic.io.repository.FragmentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AdminPanelController {

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    @Autowired
    lateinit var modelService : ModelService

    @GetMapping("/admin")
    fun getLandingPage(model : Model): String {
        val referenceFragment = modelService.getReferenceFragment()
        Fragment.renderFragmentToPlantUML(referenceFragment)
        addFragmentMetadataToModel(model, referenceFragment)
        return "referenceFragment"
    }

    @GetMapping("/admin/variants")
    fun displayVariantsAndVersions(model: Model): String {
        return "variants"
    }

    @GetMapping("/admin/fragmentDetails")
    fun displayFragmentDetails(model: Model, @RequestParam fragmentID : String): String{
        val fragment = fragmentRepository.getFragmentByDataID(fragmentID.toLong())
        model.addAttribute("header", fragment?.instance?.header?.getElements())
        model.addAttribute("deltas", fragment?.trace?.getDeltas())
        //Nodes --> Interfaces
        model.addAttribute("interfaces", fragment?.model?.getNodes())
        addFragmentMetadataToModel(model, fragment)
        return "fragmentDetails"
    }

    private fun addFragmentMetadataToModel(model: Model, referenceFragment: Fragment?) {
        model.addAttribute("diagram", "diagram.jpeg")
        model.addAttribute("variantName", referenceFragment?.variantName)
        model.addAttribute("variantID", referenceFragment?.variantID)
        model.addAttribute("runningTime", referenceFragment?.runningTime)
        model.addAttribute("runningID", referenceFragment?.runningID)
    }
}
