package com.smartappointments.booking_system.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/client")
public class ClientController {


  @GetMapping
    public String index(){
        return "client/client-panel";
    }

    @GetMapping("/client-panel")
    public String client(){
        return "client/client-panel";
    }

@GetMapping("/available-slots")
    public String availableslot(){
        return "client/available-slots";
}

    @GetMapping("/book-appointment")
    public String bookappointment(){
        return "client/book-appointment";
    }
    @GetMapping("/my-bookings")
    public String bookings() {
        return "client/my-bookings";
    }

    @GetMapping("/my-provider")
    public String myprovider() {
        return "client/my-provider";
    }

    @GetMapping("/appointment-history")
    public String appointmenthistory() {
        return "client/appointment-history";
    }

}

