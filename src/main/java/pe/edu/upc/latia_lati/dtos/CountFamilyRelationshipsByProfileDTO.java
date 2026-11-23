package pe.edu.upc.latia_lati.dtos;

public class CountFamilyRelationshipsByProfileDTO {
    private String profileName;
    private int totalRelatives;

    public String getProfileName() {
        return profileName;
    }

    public int getTotalRelatives() {
        return totalRelatives;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setTotalRelatives(int totalRelatives) {
        this.totalRelatives = totalRelatives;
    }
}
