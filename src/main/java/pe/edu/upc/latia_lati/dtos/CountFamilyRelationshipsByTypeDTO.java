package pe.edu.upc.latia_lati.dtos;

public class CountFamilyRelationshipsByTypeDTO {
    private String relationshipType;
    private int totalRelationships;

    public String getRelationshipType() {
        return relationshipType;
    }

    public int getTotalRelationships() {
        return totalRelationships;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public void setTotalRelationships(int totalRelationships) {
        this.totalRelationships = totalRelationships;
    }
}
