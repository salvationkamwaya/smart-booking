package com.smartappointments.booking_system.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/client")
public class ClientController {

    @GetMapping
    public String client(){
        return "client/client-panel";
    }





}

