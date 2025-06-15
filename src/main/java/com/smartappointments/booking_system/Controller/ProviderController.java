package com.smartappointments.booking_system.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/provider")
public class ProviderController {
    @GetMapping
    public String index(){
      return "provider/provider-panel";


    }



}
