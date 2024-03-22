package modic.io.controller

import modic.io.logic.ModelService
import modic.io.model.Fragment
import modic.io.repository.TestDataHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AdminPanelController {

    @Autowired
    lateinit var modelService : ModelService

    @GetMapping("/admin")
    fun getLandingPage(model : Model): String {
        val referenceFragment = modelService.getReferenceFragment() ?: TestDataHelper.getSimpleFragmentOnlyModel()
        Fragment.renderFragmentToPlantUML(referenceFragment)
        model.addAttribute("diagram", "diagram.jpeg")
        model.addAttribute("variantName", referenceFragment.variantName)
        model.addAttribute("variantID", referenceFragment.variantID)
        model.addAttribute("runningTime", referenceFragment.runningTime)
        model.addAttribute("runningID", referenceFragment.runningID)
        return "referenceFragment"
    }

    @GetMapping("/admin/variants")
    fun displayVariantsAndVersions(model: Model): String {
        return "variants"
    }


}