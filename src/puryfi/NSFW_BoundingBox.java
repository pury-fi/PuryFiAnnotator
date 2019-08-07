/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package puryfi;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import static puryfi.NSFW_BoundingBox.Type.*;


/**
 *
 * @author 0131
 */
public class NSFW_BoundingBox {

  
    
    
    public static enum Type {
        BELLY_EXPOSED, BELLY_COVERED, BUTTOCKS_EXPOSED, BUTTOCKS_COVERED,
        FEMALE_BREAST_EXPOSED, FEMALE_BREAST_COVERED, FEMALE_GENITALIA_EXPOSED,
        FEMALE_GENITALIA_COVERED, MALE_GENITALIA_COVERED, MALE_GENITALIA_EXPOSED,
        MALE_BREAST_EXPOSED, MALE_BREAST_COVERED, FACE_FEMALE, FACE_MALE, FEET_COVERED, FEET_EXPOSED,
        ARMPITS_COVERED, ARMPITS_EXPOSED, ANUS_COVERED, ANUS_EXPOSED,     
    }

    String name;
    Type type;
    Rectangle bounding_box = new Rectangle();
    Double confidence;
    
    BufferedImage sticker = null;
    
    boolean censor = true;
    
    public NSFW_BoundingBox(String name, Double confidence, int[] box) {
        this.name = normalizeName(name);
        this.type = toType(name);
        this.confidence = confidence;
        bounding_box.x = box[0];
        bounding_box.y = box[1];
        bounding_box.width = box[2]-box[0];
        bounding_box.height = box[3]-box[1];
    }
    
    
    private String normalizeName(String name){
        switch (name) {
            case "BELLY_EXPOSED":
                return "Stomach / Belly - Exposed";                
            case "BELLY_COVERED":
                return "Stomach / Belly - Covered";
            case "BUTTOCKS_EXPOSED":
                return "Buttocks - Exposed";
            case "BUTTOCKS_COVERED":
                return "Buttocks - Covered";
            case "MALE_BREAST_EXPOSED":
                return "Male Breast - Exposed";
            case "MALE_BREAST_COVERED":
                return "Male Breast - Covered";
            case "MALE_GENITALIA_COVERED":
                return "Male Genitalia - Covered";
            case "MALE_GENITALIA_EXPOSED":
                return "Male Genitalia - Exposed";
            case "FEMALE_BREAST_EXPOSED":
                return "Female Breast - Exposed";
            case "FEMALE_BREAST_COVERED":
                return "Female Breast - Covered";
            case "FEMALE_GENITALIA_COVERED":
                return "Female Genitalia - Covered";
            case "FEMALE_GENITALIA_EXPOSED":
                return "Female Genitalia - Exposed";
            case "ANUS_EXPOSED":
                return "Anus - Exposed";
            case "ANUS_COVERED":
                return  "Anus - Covered";
            case "FACE_FEMALE":
                return  "Face - Female";
            case "FACE_MALE":
                return  "Face - Male";
            case "FEET_COVERED":
                return "Feet - Covered";
            case "FEET_EXPOSED":
                return "Feet - Exposed";
            case "ARMPITS_COVERED":
                return "Armpits - Covered";
            case "ARMPITS_EXPOSED":
                return "Armpits - Exposed";
            default:
                return name;
        }
    }
    
    private Type toType(String name){
        Type type = null;
        switch (name) {
            case "BELLY_EXPOSED":
                type = BELLY_EXPOSED;
                break;
            case "BELLY_COVERED":
                type = BELLY_COVERED;
                break;
            case "BUTTOCKS_EXPOSED":
                type = BUTTOCKS_EXPOSED;
                break;
            case "BUTTOCKS_COVERED":
                type = BUTTOCKS_COVERED;
                break;
            case "MALE_BREAST_EXPOSED":
                type = MALE_BREAST_EXPOSED;
                break;
            case "MALE_BREAST_COVERED":
                type = MALE_BREAST_COVERED;
                break;
            case "MALE_GENITALIA_COVERED":
                type = MALE_GENITALIA_COVERED;
                break;
            case "MALE_GENITALIA_EXPOSED":
                type = MALE_GENITALIA_EXPOSED;
                break;
            case "FEMALE_BREAST_EXPOSED":
                type = FEMALE_BREAST_EXPOSED;
                break;
            case "FEMALE_BREAST_COVERED":
                type = FEMALE_BREAST_COVERED;
                break;
            case "FEMALE_GENITALIA_COVERED":
                type = FEMALE_GENITALIA_COVERED;
                break;
            case "FEMALE_GENITALIA_EXPOSED":
                type = FEMALE_GENITALIA_EXPOSED;
                break;
            case "ANUS_EXPOSED":
                type = ANUS_EXPOSED;
                break;    
            case "ANUS_COVERED":
                type = ANUS_COVERED;
                break;    
            case "FACE_FEMALE":
                type = FACE_FEMALE;
                break;
            case "FACE_MALE":
                type = FACE_MALE;
                break;
            case "FEET_COVERED":
                type = FEET_COVERED;
                break;
            case "FEET_EXPOSED":
                type = FEET_EXPOSED;
                break;
            case "ARMPITS_COVERED":
                type = ARMPITS_COVERED;
                break;
            case "ARMPITS_EXPOSED":
                type = ARMPITS_EXPOSED;
                break;
            case "Stomach / Belly - Exposed":
                type = BELLY_EXPOSED;
                break;
            case "Stomach / Belly - Covered":
                type = BELLY_COVERED;
                break;
            case "Buttocks - Exposed":
                type = BUTTOCKS_EXPOSED;
                break;
            case "Buttocks - Covered":
                type = BUTTOCKS_COVERED;
                break;
            case "Male Breast - Exposed":
                type = MALE_BREAST_EXPOSED;
                break;
            case "Male Breast - Covered":
                type = MALE_BREAST_COVERED;
                break;
            case "Male Genitalia - Covered":
                type = MALE_GENITALIA_COVERED;
                break;
            case "Male Genitalia - Exposed":
                type = MALE_GENITALIA_EXPOSED;
                break;
            case "Female Breast - Exposed":
                type = FEMALE_BREAST_EXPOSED;
                break;
            case "Female Breast - Covered":
                type = FEMALE_BREAST_COVERED;
                break;
            case "Female Genitalia - Covered":
                type = FEMALE_GENITALIA_COVERED;
                break;
            case "Female Genitalia - Exposed":
                type = FEMALE_GENITALIA_EXPOSED;
                break;
            case "Anus - Exposed":
                type = ANUS_EXPOSED;
                break;
            case "Anus - Covered":
                type = ANUS_COVERED;
                break;
            case "Face - Female":
                type = FACE_FEMALE;
                break;
            case "Face - Male":
                type = FACE_MALE;
                break;
            case "Feet - Covered":
                type = FEET_COVERED;
                break;
            case "Feet - Exposed":
                type = FEET_EXPOSED;
                break;
            case "Armpits - Covered":
                type = ARMPITS_COVERED;
                break;
            case "Armpits - Exposed":
                type = ARMPITS_EXPOSED;
                break;
            default:
                System.err.println("NOT FOUND:" + name);
                break;
        }
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.type = toType(name);
        this.confidence = 1.0;
    }

    public static Type getType(int label){
        if(Type.values().length > label){
            return Type.values()[label];
        }
        return Type.values()[0];
    }
    
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        this.confidence = 1.0;
    }

    public Rectangle getBounding_box() {
        return bounding_box;
    }

    public void setBounding_box(Rectangle bounding_box) {
        this.bounding_box = bounding_box;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public boolean isCensored() {
       
        if (type == Type.FACE_FEMALE && NSWFAPI.ff_CheckBox.isSelected() || type == Type.FACE_MALE && NSWFAPI.fm_CheckBox.isSelected()){
            return censor;
        }
        if(type == Type.FACE_FEMALE || type == Type.FACE_MALE){
            return false;
        }       
        return censor && checkOptions();
    }

    public void setCensored(boolean print) {
        this.censor = print;
    }
    
   
    public boolean checkOptions(){
        if(this.type == null){
            return true;
        }
        if(this.type.equals(BELLY_EXPOSED) && !NSWFAPI.belly_e_button.isSelected()){
            return false;
        }
        if(this.type.equals(BELLY_COVERED) && !NSWFAPI.belly_c_button.isSelected()){
            return false;
        }
        if(this.type.equals(BUTTOCKS_EXPOSED) && !NSWFAPI.buttocks_e_button.isSelected()){
            return false;
        }
        if(this.type.equals(BUTTOCKS_COVERED) && !NSWFAPI.buttocks_c_button.isSelected()){
            return false;
        }
        if(this.type.equals(FEMALE_BREAST_EXPOSED) && !NSWFAPI.fbreats_e_button.isSelected()){
            return false;
        }
        if(this.type.equals(FEMALE_BREAST_COVERED) && !NSWFAPI.fbreats_c_button.isSelected()){
            return false;
        }
        if(this.type.equals(FEMALE_GENITALIA_EXPOSED) && !NSWFAPI.fgen_e_button.isSelected()){
            return false;
        }
        if(this.type.equals(FEMALE_GENITALIA_COVERED) && !NSWFAPI.fgen_c_button.isSelected()){
            return false;
        }
        if(this.type.equals(MALE_GENITALIA_COVERED) && !NSWFAPI.mgen_c_button.isSelected()){
            return false;
        }
        if(this.type.equals(MALE_GENITALIA_EXPOSED) && !NSWFAPI.mgen_e_button.isSelected()){
            return false;
        }
        if(this.type.equals(MALE_BREAST_EXPOSED) && !NSWFAPI.mbreast_e_button.isSelected()){
            return false;
        }
        if(this.type.equals(MALE_BREAST_COVERED) && !NSWFAPI.mbreast_c_button.isSelected()){
            return false;
        }
        if(this.type.equals(FEET_COVERED) && !NSWFAPI.feet_c_button.isSelected()){
            return false;
        }
        if(this.type.equals(FEET_EXPOSED) && !NSWFAPI.feet_e_button.isSelected()){
            return false;
        }
        if(this.type.equals(ARMPITS_COVERED) && !NSWFAPI.armpits_c_button.isSelected()){
            return false;
        }
        if(this.type.equals(ARMPITS_EXPOSED) && !NSWFAPI.armpits_e_button.isSelected()){
            return false;
        }
        if (this.type.equals(ANUS_COVERED) && !NSWFAPI.anus_c_button.isSelected()) {
            return false;
        }
        if (this.type.equals(ANUS_EXPOSED) && !NSWFAPI.anus_e_button.isSelected()) {
            return false;
        }
        return true;
    }
    
    DecimalFormat f = new DecimalFormat("#0.00");
    
    public String getHeadline() {
        if(NSWFAPI.labelmodeComboBox.getSelectedIndex() == 1){
            String[] splited = getName().split("\\s+");
            String r = "";
            for (int i = 0; i < splited.length; i++) {
                if(!splited[i].isEmpty()){
                    r += splited[i].charAt(0);
                    if(splited[i].charAt(0) == 'A'){
                       r += splited[i].charAt(1); 
                    }
                }
            }
            Double confidence1 = getConfidence();
            return r + f.format(confidence1);
        }
         if(NSWFAPI.labelmodeComboBox.getSelectedIndex() == 2){
            return "";
        }
        return getName() + " " + getConfidence();
    }

    public BufferedImage getSticker() {
        return sticker;
    }

    public void setSticker(BufferedImage sticker) {
        this.sticker = sticker;
    }
   
    
    
}
