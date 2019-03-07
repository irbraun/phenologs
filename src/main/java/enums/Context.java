package enums;

import java.util.EnumSet;

public enum Context {
    ROOTPATH,
    SIBLING,
    NONE;
    
    public static EnumSet<Context> getContextSubtypes(){
        return EnumSet.of(ROOTPATH, SIBLING);
    }
    
    public static EnumSet<Context>getContextFreeSubtypes(){
        return EnumSet.of(NONE);
    }
    
    
}


