package com.example.imm.anko;

public class DigitClassification {

    private String label;
    private float confidence;

    //public DigitClassification(String label,float confidence) {update(label,confidence);}

    public DigitClassification() {
        this.label = null;
        this.confidence = (float)-1.5;
    }

    public void update(String label,float confidence) {
        this.label = label;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }

}
