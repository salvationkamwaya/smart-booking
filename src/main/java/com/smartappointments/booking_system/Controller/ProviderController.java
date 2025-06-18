package com.smartappointments.booking_system.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/provider")
public class ProviderController {

    @GetMapping("/provider-panel")
    public String index(){
      return "provider/provider-panel";
    }

    @GetMapping("/add-slot")
    public String addslot(){
        return "provider/add-slot";
    }

    @GetMapping("/my-appointments")
    public String appointments(){
        return "provider/my-appointments";
    }
@GetMapping("/settings")
    public String settings(){
        return "provider/settings";
}

@GetMapping("/profile-settings")
    public String profileset(){
        return "provider/profile-settings";
}
@GetMapping("/view-calendar")
    public String viewcalendar(){
        return "provider/view-calendar";

}

}
