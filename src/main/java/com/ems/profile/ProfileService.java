package com.ems.profile;

public interface ProfileService {
    ProfileResponse getMyProfile();
    ProfileResponse updateProfile(UpdateProfileRequest request);
    void changePassword(ChangePasswordRequest request);
}
