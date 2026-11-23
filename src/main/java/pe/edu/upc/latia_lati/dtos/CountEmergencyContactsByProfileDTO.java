package pe.edu.upc.latia_lati.dtos;

public class CountEmergencyContactsByProfileDTO {
    private String profileName;
    private int primaryContacts;
    private int otherContacts;

    public String getProfileName() {
        return profileName;
    }

    public int getPrimaryContacts() {
        return primaryContacts;
    }

    public int getOtherContacts() {
        return otherContacts;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setPrimaryContacts(int primaryContacts) {
        this.primaryContacts = primaryContacts;
    }

    public void setOtherContacts(int otherContacts) {
        this.otherContacts = otherContacts;
    }
}
