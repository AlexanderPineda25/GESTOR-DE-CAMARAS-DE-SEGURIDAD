package Camaras.VIDEOCAMARAS.infraestructure.controller.auth;

import Camaras.VIDEOCAMARAS.aplication.service.UserService;
import Camaras.VIDEOCAMARAS.shared.dto.Report.UserReportDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/users")
public class UserWebController {

    private final UserService userService;

    public UserWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/report/{userId}")
    public String getUserReportWeb(@PathVariable Long userId, Model model) {
        UserReportDTO report = userService.getUserReportById(userId);
        model.addAttribute("report", report);
        return "user-report";
    }

    @GetMapping("/list")
    public String getAllUsersWeb(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "users-list";
    }
}