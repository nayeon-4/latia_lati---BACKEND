package pe.edu.upc.latia_lati.dtos;

public class CountEmergencyContactsByRelationshipDTO {
    private String relationship;
    private int totalContacts;

    public String getRelationship() {
        return relationship;
    }

    public int getTotalContacts() {
        return totalContacts;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setTotalContacts(int totalContacts) {
        this.totalContacts = totalContacts;
    }
}
