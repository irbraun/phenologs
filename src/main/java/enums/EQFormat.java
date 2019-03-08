package enums;

import java.util.EnumSet;

public enum EQFormat {
    EQ,
    EQq,
    EEQ,
    EEQq,
    EQE,
    EQqE,
    EEQE,
    EEQqE,
    EQEE,
    EQqEE,
    EEQEE,
    EEQqEE;
    
    public static boolean hasComplexPrimaryEntity(EQFormat format){
        return EnumSet.of(EEQ,EEQq,EEQE,EEQEE,EEQqE,EEQqEE).contains(format);
    }
    
    public static boolean hasComplexSecondaryEntity(EQFormat format){
        return EnumSet.of(EQEE,EQqEE,EEQEE,EEQqEE).contains(format);
    }
    
    public static boolean hasOptionalQualifier(EQFormat format){
        return EnumSet.of(EQq,EEQq,EQqE,EEQqE,EQqEE,EEQqEE).contains(format);
    }
    
}
