package com.smartappointments.booking_system.config;

import com.smartappointments.booking_system.service.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Skip if not authenticated or accessing public resources
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return true;
        }

        // Skip if accessing authentication endpoints
        if (requestURI.startsWith("/auth/") || requestURI.startsWith("/logout") || 
            requestURI.startsWith("/css/") || requestURI.startsWith("/js/") || 
            requestURI.startsWith("/images/") || requestURI.startsWith("/debug/")) {
            return true;
        }

        if (authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            String userRole = userPrincipal.getUser().getRole().name();
            
            // Check if user is trying to access URLs for different roles
            if (requestURI.startsWith("/admin/") && !userRole.equals("ADMIN")) {
                response.sendRedirect("/auth/login?access=denied&attempted=admin");
                return false;
            }
            
            if (requestURI.startsWith("/provider/") && !userRole.equals("PROVIDER")) {
                response.sendRedirect("/auth/login?access=denied&attempted=provider");
                return false;
            }
            
            if (requestURI.startsWith("/client/") && !userRole.equals("CLIENT")) {
                response.sendRedirect("/auth/login?access=denied&attempted=client");
                return false;
            }
        }

        return true;
    }
}
