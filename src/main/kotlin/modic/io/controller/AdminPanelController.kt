package modic.io.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AdminPanelController {


    @GetMapping("/admin")
    fun getLandingPage(model : Model): String {
        return "referenceFragment"
    }


}