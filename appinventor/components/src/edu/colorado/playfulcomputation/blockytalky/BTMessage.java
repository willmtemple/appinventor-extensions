package edu.colorado.playfulcomputation.blockytalky;

import java.util.List;

public  class BTMessage{
    private static final int INT_TYPE = 1;
    private static final int FLOAT_TYPE = 2;
    private static final int STRING_TYPE = 3;

    int type;
    String key;
    String stringValue;
    int intValue;
    double floatValue;

    public BTMessage(String key, String value){
        this.type = STRING_TYPE;
        this.key = key;
        this.stringValue = value;
    }

    public BTMessage(String key, int value){
        this.type = INT_TYPE;
        this.key = key;
        this.intValue = value;
    }

    public BTMessage(String key, double value){
        this.type = FLOAT_TYPE;
        this.key = key;
        this.floatValue = value;
    }

    public boolean isIntType(){
        return this.type == INT_TYPE;
    }

    public boolean isFloatType(){
        return this.type == FLOAT_TYPE;
    }

    public boolean isStringType(){
        return this.type == STRING_TYPE;
    }

    public List<Integer> encodeAsBytesForAppInventorBLE(){
        return BTBLEEncoder.EncodeMessage(this);
    }

}