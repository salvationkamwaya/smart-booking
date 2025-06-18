package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.ProviderProfile;
import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.ProviderProfileRepository;
import com.smartappointments.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class ProviderProfileService {    @Autowired
    private ProviderProfileRepository providerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    public ProviderProfile getProviderProfile(User user) {
        return providerProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    ProviderProfile newProfile = new ProviderProfile();
                    newProfile.setUser(user);
                    return providerProfileRepository.save(newProfile);
                });
    }

    @Transactional
    public ProviderProfile updateProviderProfile(ProviderProfile profile, User user) {
        ProviderProfile existingProfile = getProviderProfile(user);
        
        // Update all fields
        existingProfile.setFirstName(profile.getFirstName());
        existingProfile.setLastName(profile.getLastName());
        existingProfile.setPhoneNumber(profile.getPhoneNumber());
        existingProfile.setDateOfBirth(profile.getDateOfBirth());
        existingProfile.setGender(profile.getGender());
        existingProfile.setBio(profile.getBio());
        existingProfile.setProfession(profile.getProfession());
        existingProfile.setSpecialization(profile.getSpecialization());
        existingProfile.setWorkplace(profile.getWorkplace());
        existingProfile.setLicenseNumber(profile.getLicenseNumber());
        existingProfile.setYearsOfExperience(profile.getYearsOfExperience());
        existingProfile.setEducation(profile.getEducation());
        existingProfile.setCertifications(profile.getCertifications());
        existingProfile.setServices(profile.getServices());
        existingProfile.setFacebookUrl(profile.getFacebookUrl());
        existingProfile.setTwitterUrl(profile.getTwitterUrl());
        existingProfile.setLinkedinUrl(profile.getLinkedinUrl());
        existingProfile.setInstagramUrl(profile.getInstagramUrl());        return providerProfileRepository.save(existingProfile);
    }

    @Transactional
    public ProviderProfile updatePersonalInfo(Map<String, Object> personalData, User user) {
        ProviderProfile existingProfile = getProviderProfile(user);
        
        // Update User entity fields (firstName, lastName)
        if (personalData.containsKey("firstName")) {
            user.setFirstName((String) personalData.get("firstName"));
        }        if (personalData.containsKey("lastName")) {
            user.setLastName((String) personalData.get("lastName"));
        }
        
        // Save the updated user
        userRepository.save(user);
        
        // Update ProviderProfile personal information fields
        if (personalData.containsKey("phoneNumber")) {
            existingProfile.setPhoneNumber((String) personalData.get("phoneNumber"));
        }
        if (personalData.containsKey("dateOfBirth")) {
            String dateStr = (String) personalData.get("dateOfBirth");
            if (dateStr != null && !dateStr.isEmpty()) {
                existingProfile.setDateOfBirth(LocalDate.parse(dateStr));
            }
        }
        if (personalData.containsKey("gender")) {
            existingProfile.setGender((String) personalData.get("gender"));
        }
        if (personalData.containsKey("bio")) {
            existingProfile.setBio((String) personalData.get("bio"));
        }

        return providerProfileRepository.save(existingProfile);
    }

    @Transactional
    public ProviderProfile updateProfessionalInfo(Map<String, Object> professionalData, User user) {
        ProviderProfile existingProfile = getProviderProfile(user);
        
        // Update professional information fields
        if (professionalData.containsKey("profession")) {
            existingProfile.setProfession((String) professionalData.get("profession"));
        }
        if (professionalData.containsKey("specialization")) {
            existingProfile.setSpecialization((String) professionalData.get("specialization"));
        }
        if (professionalData.containsKey("workplace")) {
            existingProfile.setWorkplace((String) professionalData.get("workplace"));
        }
        // Handle both workplace and clinicName for backward compatibility
        if (professionalData.containsKey("clinicName")) {
            existingProfile.setWorkplace((String) professionalData.get("clinicName"));
        }
        if (professionalData.containsKey("licenseNumber")) {
            existingProfile.setLicenseNumber((String) professionalData.get("licenseNumber"));
        }        if (professionalData.containsKey("yearsOfExperience")) {
            Object yearsObj = professionalData.get("yearsOfExperience");
            if (yearsObj instanceof Number) {
                existingProfile.setYearsOfExperience(((Number) yearsObj).intValue());
            } else if (yearsObj instanceof String) {
                try {
                    existingProfile.setYearsOfExperience(Integer.parseInt((String) yearsObj));
                } catch (NumberFormatException e) {
                    // Ignore invalid number format
                }
            }
        }
        if (professionalData.containsKey("education")) {
            existingProfile.setEducation((String) professionalData.get("education"));
        }
        if (professionalData.containsKey("certifications")) {
            existingProfile.setCertifications((String) professionalData.get("certifications"));
        }
        
        // Social media URLs
        if (professionalData.containsKey("facebookUrl")) {
            existingProfile.setFacebookUrl((String) professionalData.get("facebookUrl"));
        }
        if (professionalData.containsKey("twitterUrl")) {
            existingProfile.setTwitterUrl((String) professionalData.get("twitterUrl"));
        }
        if (professionalData.containsKey("linkedinUrl")) {
            existingProfile.setLinkedinUrl((String) professionalData.get("linkedinUrl"));
        }
        if (professionalData.containsKey("instagramUrl")) {
            existingProfile.setInstagramUrl((String) professionalData.get("instagramUrl"));
        }

        return providerProfileRepository.save(existingProfile);
    }

    public String uploadProfilePicture(MultipartFile file, User user) throws IOException {
        ProviderProfile profile = getProviderProfile(user);
        
        // Create uploads directory if it doesn't exist
        String uploadsDir = "uploads/profile-pictures/";
        Path uploadPath = Paths.get(uploadsDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath);
        
        // Update profile picture path
        profile.setProfilePicture(uploadsDir + filename);
        providerProfileRepository.save(profile);
        
        return filename;
    }

    public void deleteProviderProfile(User user) {
        providerProfileRepository.findByUser(user).ifPresent(profile -> {
            // Delete profile picture if exists
            if (profile.getProfilePicture() != null) {
                try {
                    Files.deleteIfExists(Paths.get(profile.getProfilePicture()));
                } catch (IOException e) {
                    // Log error but continue with profile deletion
                    e.printStackTrace();
                }
            }
            providerProfileRepository.delete(profile);
        });
    }
}
