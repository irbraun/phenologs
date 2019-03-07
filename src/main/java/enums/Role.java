
package enums;

public enum Role {
    QUALITY_ID,
    QUALIFIER_ID,
    PRIMARY_ENTITY1_ID,
    PRIMARY_ENTITY2_ID,
    SECONDARY_ENTITY1_ID,
    SECONDARY_ENTITY2_ID,
    DEVELOPMENTAL_STAGE_ID,
    UNKNOWN;
    
    public static String getAbbrev(Role role){
        switch(role){
            case QUALITY_ID:
                return "quality";
            case QUALIFIER_ID:
                return "qualifier";
            case PRIMARY_ENTITY1_ID:
                return "primary_entity_1";
            case PRIMARY_ENTITY2_ID:
                return "primary_entity_2";
            case SECONDARY_ENTITY1_ID:
                return "secondary_entity_1";
            case SECONDARY_ENTITY2_ID:
                return "secondary_entity_2";
            case DEVELOPMENTAL_STAGE_ID:
                return "dev_stage";
            case UNKNOWN:
                return "unknown";
            default:
                return "unknown";
        }
    }
    
    
    
    
}
