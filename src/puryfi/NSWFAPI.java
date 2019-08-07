/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package puryfi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.imgscalr.Scalr;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author 0131
 */
public class NSWFAPI extends javax.swing.JFrame {

    File[] input;
    public static File output_folder = new File("output");
    public static File temp = new File("output/temp");
    
    List<NSWF_Image> converter = new ArrayList<>();

    int displayed_index = 0;
    
    public boolean editmode = false;
    /**
     * Creates new form NewJFrame
     */
    public NSWFAPI() {

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                display(displayed_index);
            }
        };
        initComponents();
        
        
		

        editButton.setEnabled(output_folder.isDirectory() && output_folder.list().length>0);
        setAIstate(0);
        ImageIcon img = new ImageIcon(getClass().getResource("/puryfi/puryfi.png"));
        setIconImage(img.getImage());
        mgen_e_button.addActionListener(actionListener);
        mgen_c_button.addActionListener(actionListener);
        buttocks_e_button.addActionListener(actionListener);
        buttocks_c_button.addActionListener(actionListener);
        belly_e_button.addActionListener(actionListener);
        belly_c_button.addActionListener(actionListener);
        fgen_e_button.addActionListener(actionListener);
        fgen_c_button.addActionListener(actionListener);
        fbreats_e_button.addActionListener(actionListener);
        fbreats_c_button.addActionListener(actionListener);
        mbreast_e_button.addActionListener(actionListener);
        mbreast_c_button.addActionListener(actionListener);
        fm_CheckBox.addActionListener(actionListener);
        ff_CheckBox.addActionListener(actionListener);
        feet_c_button.addActionListener(actionListener);
        feet_e_button.addActionListener(actionListener);
        armpits_c_button.addActionListener(actionListener);
        armpits_e_button.addActionListener(actionListener);
        anus_c_button.addActionListener(actionListener);
        anus_e_button.addActionListener(actionListener);
        if(!output_folder.exists())output_folder.mkdir();
        if(!temp.exists())temp.mkdir();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                synchronized (NSWFAPI.class) {
                    switch (ke.getID()) {
                        case KeyEvent.KEY_PRESSED:
                            if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
                                if (del_buf != null) {
                                    NSWF_Image get = converter.get(displayed_index);
                                    boolean remove = get.getResults().remove(del_buf);
                                    display(displayed_index);
                                    del_buf = null;
                                     if(editmode){
                                         get.setEdited(true);
                                     }
                                }
                            }
                            break;

                    }
                    return false;
                }
            }
        });
        
    }
    
   


    public static void openfolder(String dir) {
        File folder = new File((dir));
        if (folder.exists() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(folder);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
   

    public NSWF_Image parser(File file, String json){
        JSONObject obj = new JSONObject(json);      
        obj.put("file", file.getAbsolutePath());
       
        Double score = obj.getJSONObject("output").getDouble("nsfw_score");
        JSONArray arr = obj.getJSONObject("output").getJSONArray("detections");
        NSWF_Image result = new NSWF_Image(file, score, obj);
        for (int i = 0; i < arr.length(); i++) {
            String name = arr.getJSONObject(i).getString("name");
            Double confidence = arr.getJSONObject(i).getDouble("confidence");
            JSONArray array = arr.getJSONObject(i).optJSONArray("bounding_box");
            int[] nums = new int[array.length()];
            for (int j = 0; j < array.length(); ++j) {
                nums[j] = array.optInt(j);
            }         
            result.getResults().add(new NSFW_BoundingBox(name, confidence, nums));
        }
        return result;
    }
    
    public void parseFace(NSWF_Image container, String json){
        try{
        JSONObject obj = new JSONObject(json);      
        container.getJson().put("face", json);
        JSONArray arr = obj.getJSONArray("faces");
        for (int i = 0; i < arr.length(); i++) {
            String gender = arr.getJSONObject(i).getJSONObject("attributes").getJSONObject("gender").getString("value");
            Integer age = arr.getJSONObject(i).getJSONObject("attributes").getJSONObject("age").getInt("value");
            String ethnicity = arr.getJSONObject(i).getJSONObject("attributes").getJSONObject("ethnicity").getString("value");
            Integer face_rect_x = arr.getJSONObject(i).getJSONObject("face_rectangle").getInt("left");
            Integer face_rect_y = arr.getJSONObject(i).getJSONObject("face_rectangle").getInt("top");
            Integer face_rect_w = arr.getJSONObject(i).getJSONObject("face_rectangle").getInt("width");
            Integer face_rect_h = arr.getJSONObject(i).getJSONObject("face_rectangle").getInt("height");
            int[] nums = new int[]{face_rect_x,face_rect_y,face_rect_w+face_rect_x,face_rect_h+face_rect_y};
            container.getResults().add(new NSFW_Face_BoundingBox("Face - "+gender, 1.0, gender, ethnicity, age, nums));
        }
        }catch(org.json.JSONException ex){
            ex.printStackTrace();
            System.err.println(json);
        }
    }
    
    
    public NSWF_Image convert(File file) {
        try {
            boolean tmp = false;
            File outputFile = file;
            if(outputFile.length() > 500*1000){    
            outputFile = new File("output/temp/upload.jpg");
            try (InputStream is = new FileInputStream(file)) {
                BufferedImage image = ImageIO.read(is);
                try (OutputStream os = new FileOutputStream(outputFile)) {
                    ImageIO.write(image, "jpg", os);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                tmp = true;
                if(outputFile.length() > 4000*1000){
                     return null; 
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
            }
            if(noaiRadioButton.isSelected()){
                return new NSWF_Image(file, 1.0, null);
            }
            String url = "http://localhost:8000/";
            if(puryRadioButton.isSelected()){
                 url = "http://pury.fi/detect";
            }
            
            MultipartEntity entity = new MultipartEntity();
            entity.addPart("file", new FileBody(outputFile));
            BufferedImage img = ImageIO.read(file);
            Instant start = Instant.now();
            HttpResponse returnResponse = null;
            try{
            returnResponse = Request.Post(url)
                    .body(entity).socketTimeout(30000)
                    .execute().returnResponse();
            }catch(java.net.SocketTimeoutException timeout){
                if(localRadioButton.isSelected()){
                  restartAI();  
                }
                return null;
            }
            Instant finish = Instant.now();
            double timeElapsed = Duration.between(start, finish).toMillis()/1000.0;  //in millis
           try{
           NSWF_Image parser = parser(file, EntityUtils.toString(returnResponse.getEntity()));
          

            if(tmp)outputFile.delete();
            return parser;
             }catch(org.json.JSONException e){
               System.out.println(EntityUtils.toString(returnResponse.getEntity()));
               e.printStackTrace();
               return null;
           }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    int mouse_m_x = 0;
    int mouse_m_y = 0;
    
    public void display(int i){
        if(!converter.isEmpty()){
        displayed_index = i;
        jLabel9.setText("Preview: " + (displayed_index+1) + " / " + converter.size());
        
        NSWF_Image get = converter.get(i);
        scoreLabel.setText("<html>NSFW Score: <b>"+get.getNsfw_score());
        BufferedImage cens = get.getCensoredImage();
        BufferedImage org_rsize = get.getResizedPaintedImage(originialimagelabel);
            if (new_bb != null) {
                int image_offset_x = 0;
                int image_offset_y = 0;
                Rectangle viewportsize = originialimagelabel.getBounds();
                BufferedImage org = get.getBufferedImage();
                double scalex = (double) originialimagelabel.getWidth() / org.getWidth();
                double scaley = (double) originialimagelabel.getHeight() / org.getHeight();
                double scale = Math.min(scalex, scaley);
                int r_h = (int) (org.getHeight() * scale);
                int r_w = (int) (org.getWidth() * scale);
                image_offset_y = (viewportsize.height - r_h) / 2;
                image_offset_x = (viewportsize.width - r_w) / 2;
                int deltaX = (mouse_m_x - bb_x);
                int deltaY = (mouse_m_y - bb_y);
                Graphics2D g = (Graphics2D) org_rsize.getGraphics();
                g.setColor(Color.pink);
                if(deltaX > 0 && deltaY > 0){
                    g.fillRect(bb_x - image_offset_x, bb_y - image_offset_y, deltaX, deltaY);
                }else if(deltaX < 0 && deltaY < 0){
                    g.fillRect(bb_x - image_offset_x+deltaX, bb_y - image_offset_y+deltaY, -deltaX, -deltaY);
                }else if(deltaX < 0 && deltaY > 0){
                    g.fillRect(bb_x - image_offset_x+deltaX, bb_y - image_offset_y, -deltaX, deltaY);
                }else if(deltaX > 0 && deltaY < 0){
                    g.fillRect(bb_x - image_offset_x, bb_y - image_offset_y+deltaY, deltaX, -deltaY);
                }
                g.dispose();
            }
            BufferedImage cens_rsize = rsize(cens, censoredlimagelabel);
            originialimagelabel.setIcon(new ImageIcon(org_rsize));
            censoredlimagelabel.setIcon(new ImageIcon(cens_rsize));
        ignoreCheckBox.setSelected(converter.get(displayed_index).isIgnore());
        }
    }
    
  


    public static BufferedImage rsize(BufferedImage image, JLabel label) {
        double scalex = (double) label.getWidth() / image.getWidth();
        double scaley = (double) label.getHeight() / image.getHeight();
        double scale = Math.min(scalex, scaley);  
        return Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC,(int)(Math.max(1,image.getWidth()*scale)), (int)(Math.max(1,image.getHeight()*scale)), Scalr.OP_ANTIALIAS); 
    }
    
    
    int aistate = 0;
    
    public int getAIState(){
        return aistate;
    }
    /**
     * 0 = starting
     * 1 = waiting
     * 3 = thinking
     * @param state 
     */
    public void setAIstate(int state){
        switch (state) {
            case 0:
                aistate = 0;
                statusLabel.setText("Turning on AI");
                statusLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/puryfi/load_red.gif")));
                break;
            case 1:  
                aistate = 1;
                statusLabel.setText("AI feels kinky now");
                statusLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/puryfi/alive.gif")));   
                startButton.setEnabled(true);
                break;
            case 2: 
                aistate = 2;
                statusLabel.setText("AI is looking at nudes");
                statusLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/puryfi/load_green.gif"))); 
                break;
             case 3: 
                 aistate = 3;
                statusLabel.setText("AI got tired, we are restarting");
                statusLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/puryfi/load_red.gif"))); 
                break;

        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        belly_c_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        belly_e_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        buttocks_c_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        buttocks_e_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        fbreast_c_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        fbreast_e_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        fgen_c_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        fgen_e_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        mbreast_c_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        mbreast_e_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        mgen_c_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        mgen_e_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        ff_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        fm_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        fc_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        fe_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        ac_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        ae_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        anc_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        ane_RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        stickerMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        deleteMenuItem = new javax.swing.JMenuItem();
        buttonGroup3 = new javax.swing.ButtonGroup();
        apiGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        originialimagelabel = new javax.swing.JLabel();
        censoredlimagelabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        buttocks_c_button = new javax.swing.JCheckBox();
        fbreats_c_button = new javax.swing.JCheckBox();
        buttocks_e_button = new javax.swing.JCheckBox();
        fbreats_e_button = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        fgen_e_button = new javax.swing.JCheckBox();
        fgen_c_button = new javax.swing.JCheckBox();
        mgen_c_button = new javax.swing.JCheckBox();
        mgen_e_button = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        barButton = new javax.swing.JRadioButton();
        pixelButton = new javax.swing.JRadioButton();
        blurButton = new javax.swing.JRadioButton();
        belly_c_button = new javax.swing.JCheckBox();
        belly_e_button = new javax.swing.JCheckBox();
        mbreast_c_button = new javax.swing.JCheckBox();
        mbreast_e_button = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jSpinner1 = new javax.swing.JSpinner();
        ignoreCheckBox = new javax.swing.JCheckBox();
        scoreLabel = new javax.swing.JLabel();
        ff_CheckBox = new javax.swing.JCheckBox();
        fm_CheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        startButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        faceplusplusCheckBox = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        localRadioButton = new javax.swing.JRadioButton();
        puryRadioButton = new javax.swing.JRadioButton();
        jSeparator3 = new javax.swing.JSeparator();
        editButton = new javax.swing.JButton();
        noaiRadioButton = new javax.swing.JRadioButton();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        feet_c_button = new javax.swing.JCheckBox();
        feet_e_button = new javax.swing.JCheckBox();
        armpits_c_button = new javax.swing.JCheckBox();
        armpits_e_button = new javax.swing.JCheckBox();
        anus_c_button = new javax.swing.JCheckBox();
        anus_e_button = new javax.swing.JCheckBox();
        statusLabel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        labelmodeComboBox = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();

        buttonGroup3.add(belly_c_RadioButtonMenuItem);
        belly_c_RadioButtonMenuItem.setText("Stomach / Belly - Covered");
        belly_c_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                belly_c_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(belly_c_RadioButtonMenuItem);

        buttonGroup3.add(belly_e_RadioButtonMenuItem);
        belly_e_RadioButtonMenuItem.setText("Stomach / Belly - Exposed");
        belly_e_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                belly_e_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(belly_e_RadioButtonMenuItem);

        buttonGroup3.add(buttocks_c_RadioButtonMenuItem);
        buttocks_c_RadioButtonMenuItem.setText("Buttocks - Covered");
        buttocks_c_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttocks_c_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(buttocks_c_RadioButtonMenuItem);

        buttonGroup3.add(buttocks_e_RadioButtonMenuItem);
        buttocks_e_RadioButtonMenuItem.setText("Buttocks - Exposed");
        buttocks_e_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttocks_e_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(buttocks_e_RadioButtonMenuItem);

        buttonGroup3.add(fbreast_c_RadioButtonMenuItem);
        fbreast_c_RadioButtonMenuItem.setText("Female Breast - Covered");
        fbreast_c_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fbreast_c_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(fbreast_c_RadioButtonMenuItem);

        buttonGroup3.add(fbreast_e_RadioButtonMenuItem);
        fbreast_e_RadioButtonMenuItem.setText("Female Breast - Exposed");
        fbreast_e_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fbreast_e_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(fbreast_e_RadioButtonMenuItem);

        buttonGroup3.add(fgen_c_RadioButtonMenuItem);
        fgen_c_RadioButtonMenuItem.setText("Female Genitalia - Covered");
        fgen_c_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fgen_c_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(fgen_c_RadioButtonMenuItem);

        buttonGroup3.add(fgen_e_RadioButtonMenuItem);
        fgen_e_RadioButtonMenuItem.setText("Female Genitalia - Exposed");
        fgen_e_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fgen_e_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(fgen_e_RadioButtonMenuItem);

        buttonGroup3.add(mbreast_c_RadioButtonMenuItem);
        mbreast_c_RadioButtonMenuItem.setText("Male Breast - Covered");
        mbreast_c_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mbreast_c_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(mbreast_c_RadioButtonMenuItem);

        buttonGroup3.add(mbreast_e_RadioButtonMenuItem);
        mbreast_e_RadioButtonMenuItem.setText("Male Breast - Exposed");
        mbreast_e_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mbreast_e_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(mbreast_e_RadioButtonMenuItem);

        buttonGroup3.add(mgen_c_RadioButtonMenuItem);
        mgen_c_RadioButtonMenuItem.setText("Male Genitalia - Covered");
        mgen_c_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mgen_c_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(mgen_c_RadioButtonMenuItem);

        buttonGroup3.add(mgen_e_RadioButtonMenuItem);
        mgen_e_RadioButtonMenuItem.setText("Male Genitialia - Exposed");
        mgen_e_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mgen_e_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(mgen_e_RadioButtonMenuItem);

        buttonGroup3.add(ff_RadioButtonMenuItem);
        ff_RadioButtonMenuItem.setText("Face - Female");
        ff_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ff_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ff_RadioButtonMenuItem);

        buttonGroup3.add(fm_RadioButtonMenuItem);
        fm_RadioButtonMenuItem.setText("Face - Male");
        fm_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fm_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(fm_RadioButtonMenuItem);

        buttonGroup3.add(fc_RadioButtonMenuItem);
        fc_RadioButtonMenuItem.setText("Feet - Covered");
        fc_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fc_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(fc_RadioButtonMenuItem);

        buttonGroup3.add(fe_RadioButtonMenuItem);
        fe_RadioButtonMenuItem.setText("Feet - Exposed");
        fe_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fe_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(fe_RadioButtonMenuItem);

        buttonGroup3.add(ac_RadioButtonMenuItem);
        ac_RadioButtonMenuItem.setText("Armpits - Covered");
        ac_RadioButtonMenuItem.setToolTipText("");
        ac_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ac_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ac_RadioButtonMenuItem);

        buttonGroup3.add(ae_RadioButtonMenuItem);
        ae_RadioButtonMenuItem.setText("Armpits - Exposed");
        ae_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ae_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ae_RadioButtonMenuItem);

        buttonGroup3.add(anc_RadioButtonMenuItem);
        anc_RadioButtonMenuItem.setText("Anus - Covered");
        anc_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anc_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(anc_RadioButtonMenuItem);

        buttonGroup3.add(ane_RadioButtonMenuItem);
        ane_RadioButtonMenuItem.setText("Anus - Exposed");
        ane_RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ane_RadioButtonMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ane_RadioButtonMenuItem);
        jPopupMenu1.add(jSeparator4);

        stickerMenuItem.setText("Add / Remove Sticker");
        stickerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stickerMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(stickerMenuItem);
        jPopupMenu1.add(jSeparator2);

        deleteMenuItem.setText("Delete");
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuItemActionPerformed(evt);
            }
        });
        jPopupMenu1.add(deleteMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Pury.fi");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("<html><a href=\"pury.fi\"/>www.pury.fi</a> <br>By allusion, 0131, Jae Jin and Skier23");
        jLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Pury.fi Censor Tool");

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        originialimagelabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        originialimagelabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                originialimagelabelMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                originialimagelabelMouseMoved(evt);
            }
        });
        originialimagelabel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                originialimagelabelMouseWheelMoved(evt);
            }
        });
        originialimagelabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                originialimagelabelMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                originialimagelabelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                originialimagelabelMouseReleased(evt);
            }
        });
        jPanel2.add(originialimagelabel);

        censoredlimagelabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(censoredlimagelabel);

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setText("Preview:");

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton4.setText("<html> <p>&rarr;</p> ");
        jButton4.setActionCommand("<html> <p>U+2B05</p> ");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton6.setText("<html> <p>&larr;</p> ");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        buttocks_c_button.setSelected(true);
        buttocks_c_button.setText("Buttocks - Covered");

        fbreats_c_button.setSelected(true);
        fbreats_c_button.setText("Female Breast - Covered");

        buttocks_e_button.setSelected(true);
        buttocks_e_button.setText("Buttocks - Exposed");

        fbreats_e_button.setSelected(true);
        fbreats_e_button.setText("Female Breast - Exposed");

        jCheckBox5.setSelected(true);
        jCheckBox5.setText("<html>Save <b>Json-Metadata");

        fgen_e_button.setSelected(true);
        fgen_e_button.setText("Female Genitalia - Exposed");

        fgen_c_button.setSelected(true);
        fgen_c_button.setText("Female Genitalia - Covered");

        mgen_c_button.setSelected(true);
        mgen_c_button.setText("Male Genitalia - Covered");

        mgen_e_button.setSelected(true);
        mgen_e_button.setText("Male Genitalia - Exposed");

        jLabel10.setText("Censor options:");

        saveButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        saveButton.setText("Save images");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(barButton);
        barButton.setSelected(true);
        barButton.setText("Black bar");
        barButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(pixelButton);
        pixelButton.setText("Pixelation");
        pixelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pixelButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(blurButton);
        blurButton.setText("Blur");
        blurButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blurButtonActionPerformed(evt);
            }
        });

        belly_c_button.setSelected(true);
        belly_c_button.setText("Stomach / Belly - Covered");

        belly_e_button.setSelected(true);
        belly_e_button.setText("Stomach / Belly - Exposed");

        mbreast_c_button.setSelected(true);
        mbreast_c_button.setText("Male Breast - Covered");

        mbreast_e_button.setSelected(true);
        mbreast_e_button.setText("Male Breast - Exposed");

        jLabel5.setText("Censor type:");

        jToggleButton1.setText("Toogle options");
        jToggleButton1.setFocusable(false);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(20, 1, null, 1));
        jSpinner1.setEnabled(false);
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        ignoreCheckBox.setText("Ignore image");
        ignoreCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreCheckBoxActionPerformed(evt);
            }
        });

        scoreLabel.setVisible(false);
        scoreLabel.setText("<html>NSFW Score:");

        ff_CheckBox.setText("Face - Female");

        fm_CheckBox.setText("Face - Male");

        jButton2.setText("Select");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel8.setText("Output folder: /output");

        jLabel7.setText("Input folder:");

        startButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("*max filesize 4MB");

        jButton1.setText("Select");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        faceplusplusCheckBox.setVisible(false);
        faceplusplusCheckBox.setText("face++");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel11.setText("API:");

        apiGroup.add(localRadioButton);
        localRadioButton.setText("localhost:8000");

        apiGroup.add(puryRadioButton);
        puryRadioButton.setSelected(true);
        puryRadioButton.setText("pury.fi/detect");

        editButton.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        editButton.setText("Edit stored data");
        editButton.setFocusPainted(false);
        editButton.setFocusable(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        apiGroup.add(noaiRadioButton);
        noaiRadioButton.setText("No AI");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startButton))
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator3)
                    .addComponent(editButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(faceplusplusCheckBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(localRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(puryRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noaiRadioButton)))
                        .addGap(0, 113, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startButton)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1)
                        .addComponent(jLabel7)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jLabel8)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(localRadioButton)
                        .addComponent(puryRadioButton)
                        .addComponent(noaiRadioButton))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(faceplusplusCheckBox)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.75f), Float.valueOf(0.05f), Float.valueOf(0.95f), Float.valueOf(0.05f)));
        jSpinner2.setEnabled(false);
        jSpinner2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner2StateChanged(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(" ");

        feet_c_button.setSelected(true);
        feet_c_button.setText("Feet - Covered");

        feet_e_button.setSelected(true);
        feet_e_button.setText("Feet - Exposed");

        armpits_c_button.setSelected(true);
        armpits_c_button.setText("Armpits - Covered");

        armpits_e_button.setSelected(true);
        armpits_e_button.setText("Armpits - Exposed");

        anus_c_button.setSelected(true);
        anus_c_button.setText("Anus - Covered");

        anus_e_button.setSelected(true);
        anus_e_button.setText("Anus - Exposed");

        statusLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusLabel.setText("Loading AI");
        statusLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/puryfi/rsz_become_a_patron_button.png"))); // NOI18N
        jLabel12.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel12MouseClicked(evt);
            }
        });

        labelmodeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Full names", "Short names", "Hidden" }));
        labelmodeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelmodeComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Label mode:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addGap(283, 283, 283))
                                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(ff_CheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(fm_CheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(armpits_c_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(feet_c_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(feet_e_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(armpits_e_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(11, 11, 11)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(anus_c_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(fgen_c_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(fbreats_c_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(buttocks_c_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(belly_c_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mgen_c_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mbreast_c_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(fbreats_e_button, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mbreast_e_button, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(fgen_e_button, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(buttocks_e_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(belly_e_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mgen_e_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(anus_e_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(pixelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(barButton)
                                            .addComponent(blurButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addGap(0, 7, Short.MAX_VALUE)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(jSpinner2, javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)))))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelmodeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addGap(0, 0, Short.MAX_VALUE))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ignoreCheckBox)
                                .addGap(8, 8, 8)
                                .addComponent(jCheckBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(scoreLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jToggleButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(jLabel10))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(belly_c_button)
                                    .addComponent(belly_e_button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(buttocks_e_button)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fbreats_e_button)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(fgen_e_button)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(46, 46, 46)
                                                .addComponent(mgen_e_button))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(buttocks_c_button)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fbreats_c_button)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fgen_c_button)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(mbreast_c_button)
                                            .addComponent(mbreast_e_button))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(mgen_c_button)
                                            .addComponent(feet_e_button)
                                            .addComponent(feet_c_button)
                                            .addComponent(fm_CheckBox)))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(23, 23, 23)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(armpits_e_button)
                                .addComponent(anus_c_button)
                                .addComponent(anus_e_button))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(ff_CheckBox)
                                .addComponent(armpits_c_button))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(barButton)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pixelButton)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(blurButton)
                            .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addGap(9, 9, 9)
                        .addComponent(labelmodeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(2, 2, 2)
                .addComponent(scoreLabel)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(saveButton)
                        .addComponent(ignoreCheckBox)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //final JFileChooser fc = new JFileChooser();
        Frame parent = new Frame();  
        FileDialog fc = new FileDialog(parent, "Select Images", FileDialog.LOAD);
        fc.setDirectory("");  
        fc.setMultipleMode(true);
        fc.setFile("*.jpg;*.jpeg;*.png");
        
        fc.setVisible(true);
        File[] files = fc.getFiles();
        if(files.length > 0){
        input = files;//fc.getSelectedFiles();

        if (input.length == 1) {
            if (input[0].isDirectory()) {
                File dir = input[0];
                input = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return dir.length() < 4*Math.pow(10, 6) && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
                    }
                });
            }
        }
        List<File> filter = new ArrayList<>();
        for (int i = 0; i < input.length; i++) {
            if(input[i].length() < 4*Math.pow(10, 6)){ 
                filter.add(input[i]);
            }
        }
        input = filter.toArray(new File[0]);
        jLabel7.setText("Input folder: " + input.length + " files selected.");
        }
        //}

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            output_folder = fc.getSelectedFile();
            if (returnVal == JFileChooser.APPROVE_OPTION) {
              
                    jLabel8.setText("Output folder: ..." + fc.getSelectedFile().getPath().substring( fc.getSelectedFile().getPath().length()-20,  fc.getSelectedFile().getPath().length()-1));
                
            }
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    int i_backup = -1;
    
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
       
        saveButton.setText("Save images");
        if(input != null){
        Thread t = new Thread(new Runnable() {
            
            long timstamp = 0;
            
            @Override
            public void run() {
               
                startButton.setEnabled(false);
                setAIstate(2);
                for (int i = 0; i < input.length; i++) {
                    File name = input[i];
                    timstamp = System.currentTimeMillis();
                    NSWF_Image convert = convert(name);
                    if(convert != null){
                        converter.add(convert);
                        if(converter.size() == 1){
                            display(converter.size()-1);
                            saveButton.setText("Save images");
                            saveButton.setEnabled(true);
                        }                        
                    }else{
                        boolean restarting = false;
                        while (getAIState() == 3) {
                            restarting = true;
                            try {                                
                                System.err.println("Waiting for Ai to turn on.");
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if(restarting && i < input.length){
                            if(i_backup != i){
                                    i--;
                            }
                            i_backup = i;
                            setAIstate(2);
                        }       
                        continue;
                    }
                    jLabel9.setText("Preview: " + (displayed_index+1) + " / " + converter.size());
                }
                if(!converter.isEmpty()){
                    saveButton.setEnabled(true);
                }
                startButton.setEnabled(true);
                setAIstate(1);
            }
        });
        t.start();
        }
    }//GEN-LAST:event_startButtonActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
      if(displayed_index > 0){
          display(displayed_index-1);
      }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
      if(displayed_index < converter.size()-1){
          display(displayed_index+1);
      }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        if (!converter.isEmpty()) {
            for (int i = 0; i < converter.size(); i++) {
                if(editmode && !converter.get(i).isEdited()){
                    converter.get(i).saveCensoredImage();
                    continue;
                }
                if (!converter.get(i).isIgnore()) {
                    converter.get(i).saveImage();
                    if(editmode){
                        converter.get(i).getEditedsourcefileimage().delete();
                        converter.get(i).getEditedsourcefiletxt().delete();
                    }
                }
                
            }
            editButton.setEnabled(output_folder.isDirectory() && output_folder.list().length>0);
            openfolder("output");
            
        }
    }//GEN-LAST:event_saveButtonActionPerformed

     public static String[] readFile(String filepath) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(filepath), "UTF8"));
            LineNumberReader lnr = new LineNumberReader(new FileReader(filepath));
            lnr.skip(Long.MAX_VALUE);
            lnr.close();
            String[] filecontent = new String[lnr.getLineNumber() + 1];
            for (int i = 0; i < filecontent.length; i++) {
                filecontent[i] = br.readLine();
            }
            return filecontent;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException | NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
   public static File[] getFiles(String foldername, final String[] filter) {
        return new File(foldername).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File folder, String name) {
                if (filter != null && filter.length > 0) {
                    for (int i = 0; i < filter.length; i++) {
                        if (name.endsWith(filter[i]) || name.endsWith(filter[i].toUpperCase())) { // only these picture types are added.                     
                            return true;
                        }
                    }
                } else {
                    return true;
                }
                return false;
            }
        });

    }
    
    private void originialimagelabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_originialimagelabelMouseClicked
        if(pressed == null){
        if (evt.isPopupTrigger()){         
         if(doPopup(originialimagelabel, evt)){
             return;
         }
        }
        if(!converter.isEmpty() && evt.getClickCount() == 2){
        NSWF_Image get = converter.get(displayed_index);
        List<NSFW_BoundingBox> results = get.getResults();
        int x = evt.getX();
        int y = evt.getY();
        int image_offset_x = 0;
        int image_offset_y = 0;
        Rectangle viewportsize = originialimagelabel.getBounds();
        BufferedImage org = get.getBufferedImage();
        double scalex = (double) originialimagelabel.getWidth() / org.getWidth();
        double scaley = (double) originialimagelabel.getHeight() / org.getHeight();
        double scale = Math.min(scalex, scaley);
        int r_h = (int) (org.getHeight()*scale);
        int r_w = (int) (org.getWidth()*scale);
        
        image_offset_y = (viewportsize.height-r_h)/2;
        image_offset_x = (viewportsize.width-r_w)/2;

        
        for (int i = 0; i < results.size(); i++) {
            NSFW_BoundingBox get1 = results.get(i);
            Rectangle bounding_box = get1.bounding_box;
            int bx = (int) (scale*bounding_box.x)+image_offset_x;
            int by = (int) (scale*bounding_box.y)+image_offset_y;
             int bw = (int) (scale*(bounding_box.width+bounding_box.x))+image_offset_x;
              int bh = (int) (scale*(bounding_box.height+bounding_box.y))+image_offset_y;
          
            if(bx <= x && bw >= x &&
               by <= y && bh >= y){
                get1.setCensored(!get1.isCensored());
                display(displayed_index);
            }
        }
        
        }
        
        new_bb = null;
        }
    }//GEN-LAST:event_originialimagelabelMouseClicked

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        mgen_e_button.setSelected(!jToggleButton1.isSelected());
        mgen_c_button.setSelected(!jToggleButton1.isSelected());
        buttocks_e_button.setSelected(!jToggleButton1.isSelected());
        buttocks_c_button.setSelected(!jToggleButton1.isSelected());
        belly_e_button.setSelected(!jToggleButton1.isSelected());
        belly_c_button.setSelected(!jToggleButton1.isSelected());
        fgen_e_button.setSelected(!jToggleButton1.isSelected());
        fgen_c_button.setSelected(!jToggleButton1.isSelected());
        fbreats_e_button.setSelected(!jToggleButton1.isSelected());
        fbreats_c_button.setSelected(!jToggleButton1.isSelected());
        mbreast_e_button.setSelected(!jToggleButton1.isSelected());
        mbreast_c_button.setSelected(!jToggleButton1.isSelected());
        feet_c_button.setSelected(!jToggleButton1.isSelected());
        feet_e_button.setSelected(!jToggleButton1.isSelected());
        armpits_c_button.setSelected(!jToggleButton1.isSelected());
        armpits_e_button.setSelected(!jToggleButton1.isSelected());
        anus_e_button.setSelected(!jToggleButton1.isSelected());
        anus_c_button.setSelected(!jToggleButton1.isSelected());
                
         display(displayed_index);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void pixelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pixelButtonActionPerformed
        display(displayed_index);
         jLabel4.setText("Pixelsize");
        jSpinner1.setEnabled(true);
        jSpinner2.setEnabled(false);
    }//GEN-LAST:event_pixelButtonActionPerformed

    private void barButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barButtonActionPerformed
       display(displayed_index);
       jLabel4.setText("");
       jSpinner1.setEnabled(false);
       jSpinner2.setEnabled(false);
    }//GEN-LAST:event_barButtonActionPerformed

    private void blurButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blurButtonActionPerformed
       display(displayed_index);
       jLabel4.setText("Blur | Edges");
       jSpinner1.setEnabled(true);
       jSpinner2.setEnabled(true);
    }//GEN-LAST:event_blurButtonActionPerformed

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
       display(displayed_index);
    }//GEN-LAST:event_jSpinner1StateChanged

    private void ignoreCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreCheckBoxActionPerformed
     if(!converter.isEmpty()){
         NSWF_Image get = converter.get(displayed_index);
         get.setIgnore(ignoreCheckBox.isSelected());
     }
    }//GEN-LAST:event_ignoreCheckBoxActionPerformed

    private void originialimagelabelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_originialimagelabelMouseMoved
    if(!converter.isEmpty()){
        NSWF_Image get = converter.get(displayed_index);
        List<NSFW_BoundingBox> results = get.getResults();
        int x = evt.getX();
        int y = evt.getY();
        int image_offset_x = 0;
        int image_offset_y = 0;
        Rectangle viewportsize = originialimagelabel.getBounds();
        BufferedImage org = get.getBufferedImage();
        double scalex = (double) originialimagelabel.getWidth() / org.getWidth();
        double scaley = (double) originialimagelabel.getHeight() / org.getHeight();
        double scale = Math.min(scalex, scaley);
        int r_h = (int) (org.getHeight()*scale);
        int r_w = (int) (org.getWidth()*scale); 
        image_offset_y = (viewportsize.height-r_h)/2;
        image_offset_x = (viewportsize.width-r_w)/2; 
        boolean hit = false;
        for (int i = 0; i < results.size(); i++) {
            NSFW_BoundingBox get1 = results.get(i);
            Rectangle bounding_box = get1.bounding_box;
            int bx = (int) (scale*bounding_box.x)+image_offset_x;
            int by = (int) (scale*bounding_box.y)+image_offset_y;
            int bw = (int) (scale*(bounding_box.width+bounding_box.x))+image_offset_x;
            int bh = (int) (scale*(bounding_box.height+bounding_box.y))+image_offset_y;
            if(bx <= x && bw >= x &&
               by <= y && bh >= y){
               hit = true;
               originialimagelabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); 
               break;
            }
        }
        if(!hit){
           originialimagelabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));  
        }
        
        }      
    }//GEN-LAST:event_originialimagelabelMouseMoved

    
    NSFW_BoundingBox pressed = null;
    public static NSFW_BoundingBox del_buf = null;
    Point pressed_event = null;
    int p_x;
    int p_y;
    
    NSFW_BoundingBox pop_selection = null;
    
    public boolean doPopup(Component component, MouseEvent evt){
        if(!converter.isEmpty()){
        NSWF_Image get = converter.get(displayed_index);
        List<NSFW_BoundingBox> results = get.getResults();
        int x = evt.getX();
        int y = evt.getY();
        int image_offset_x = 0;
        int image_offset_y = 0;
        Rectangle viewportsize = originialimagelabel.getBounds();
        BufferedImage org = get.getBufferedImage();
        double scalex = (double) originialimagelabel.getWidth() / org.getWidth();
        double scaley = (double) originialimagelabel.getHeight() / org.getHeight();
        double scale = Math.min(scalex, scaley);
        int r_h = (int) (org.getHeight()*scale);
        int r_w = (int) (org.getWidth()*scale); 
        image_offset_y = (viewportsize.height-r_h)/2;
        image_offset_x = (viewportsize.width-r_w)/2; 
        for (int i = 0; i < results.size(); i++) {
            NSFW_BoundingBox get1 = results.get(i);
            Rectangle bounding_box = get1.bounding_box;
            int bx = (int) (scale*bounding_box.x)+image_offset_x;
            int by = (int) (scale*bounding_box.y)+image_offset_y;
            int bw = (int) (scale*(bounding_box.width+bounding_box.x))+image_offset_x;
            int bh = (int) (scale*(bounding_box.height+bounding_box.y))+image_offset_y;
            if(bx <= x && bw >= x &&
               by <= y && bh >= y){
               jPopupMenu1.show(component, evt.getX(), evt.getY());
                switch (get1.getName()) {
                    case "Stomach / Belly - Exposed":
                        belly_e_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Stomach / Belly - Covered":
                        belly_c_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Buttocks - Exposed":
                        buttocks_e_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Buttocks - Covered":
                        buttocks_c_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Male Breast - Exposed":
                        mbreast_e_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Male Breast - Covered":
                        mbreast_c_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Male Genitalia - Covered":
                        mgen_c_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Male Genitalia - Exposed":
                        mgen_e_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Female Breast - Exposed":
                        fbreast_e_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Female Breast - Covered":
                        fbreast_c_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Female Genitalia - Covered":
                        fgen_c_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Female Genitalia - Exposed":
                        fgen_e_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Feet - Exposed":
                        fe_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Feet - Covered":
                        fc_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Armpits - Exposed":
                        ae_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Armpits - Covered":
                        ac_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Anus - Covered":
                        anc_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Anus - Exposed":
                        ane_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Face - Female":
                        ff_RadioButtonMenuItem.setSelected(true);
                        break;
                    case "Face - Male":
                        fm_RadioButtonMenuItem.setSelected(true);
                        break;
                    default:
                        break;
                }
               pop_selection = get1;
               return true;
            }
        } 
        }
        return false;
    }
    
    private void originialimagelabelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_originialimagelabelMouseDragged
        mouse_m_x = evt.getX();
        mouse_m_y = evt.getY();
        if (pressed != null) {
            NSWF_Image get = converter.get(displayed_index);
            BufferedImage bufferedImage = get.getBufferedImage();
            double scalex = (double) originialimagelabel.getWidth() / bufferedImage.getWidth();
            double scaley = (double) originialimagelabel.getHeight() / bufferedImage.getHeight();
            double scale = 1 / Math.min(scalex, scaley);

            //int new_x = (int) (scale*Math.max(0,Math.min(bufferedImage.getWidth(),(int) (p_x - pressed_event.getX() + evt.getXOnScreen()))));
            // int new_y = (int) (scale*Math.max(0,Math.min(bufferedImage.getHeight(),(int) (p_y - pressed_event.getY() + evt.getYOnScreen()))));
            int deltaX = (int) (evt.getXOnScreen() - pressed_event.getX());
            int deltaY = (int) (evt.getYOnScreen() - pressed_event.getY());

            pressed.getBounding_box().setLocation(
                    Math.min(bufferedImage.getWidth() - pressed.getBounding_box().width, Math.max(0, p_x + (int) (scale * deltaX))),
                    Math.min(bufferedImage.getHeight() - pressed.getBounding_box().height, Math.max(0, p_y + (int) (scale * deltaY))));
            if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
            display(displayed_index);
            return;
        }
        if (new_bb != null) {
            display(displayed_index);
        }
      
    }//GEN-LAST:event_originialimagelabelMouseDragged

   Point new_bb;
   int bb_x;
   int bb_y;
    
    private void originialimagelabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_originialimagelabelMousePressed
        del_buf = null;
        if(!converter.isEmpty()){
        NSWF_Image get = converter.get(displayed_index);
        List<NSFW_BoundingBox> results = get.getResults();
        int x = evt.getX();
        int y = evt.getY();
        int image_offset_x = 0;
        int image_offset_y = 0;
        Rectangle viewportsize = originialimagelabel.getBounds();
        BufferedImage org = get.getBufferedImage();
        double scalex = (double) originialimagelabel.getWidth() / org.getWidth();
        double scaley = (double) originialimagelabel.getHeight() / org.getHeight();
        double scale = Math.min(scalex, scaley);
        int r_h = (int) (org.getHeight()*scale);
        int r_w = (int) (org.getWidth()*scale); 
        image_offset_y = (viewportsize.height-r_h)/2;
        image_offset_x = (viewportsize.width-r_w)/2; 
        for (int i = 0; i < results.size(); i++) {
            NSFW_BoundingBox get1 = results.get(i);
            Rectangle bounding_box = get1.bounding_box;
            int bx = (int) (scale*bounding_box.x)+image_offset_x;
            int by = (int) (scale*bounding_box.y)+image_offset_y;
            int bw = (int) (scale*(bounding_box.width+bounding_box.x))+image_offset_x;
            int bh = (int) (scale*(bounding_box.height+bounding_box.y))+image_offset_y;
            if(bx <= x && bw >= x &&
               by <= y && bh >= y){
               originialimagelabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); 
               pressed = get1;
               del_buf = get1;
               if (evt.isPopupTrigger()){         
                   if (doPopup(originialimagelabel, evt)) {
                       return;
                   }
               }
               pressed_event = evt.getLocationOnScreen();
               p_x = get1.getBounding_box().x;
               p_y = get1.getBounding_box().y;
               return;
            }
        }       
        }
        new_bb = evt.getLocationOnScreen();
        bb_x = evt.getX();
        bb_y = evt.getY();
    }//GEN-LAST:event_originialimagelabelMousePressed

    private void originialimagelabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_originialimagelabelMouseReleased
        if(jPopupMenu1.isShowing()){
            return;
        }
        if (evt.isPopupTrigger()) {
            if (doPopup(originialimagelabel, evt)) {
                return;
            }
        }
        pressed = null;
     if(new_bb != null && Math.abs(evt.getLocationOnScreen().x-new_bb.x) > 5 && Math.abs(evt.getLocationOnScreen().y-new_bb.y) > 5 && !converter.isEmpty()){
            NSWF_Image get = converter.get(displayed_index);
            List<NSFW_BoundingBox> results = get.getResults();
            BufferedImage org = get.getBufferedImage();
            //BufferedImage org_rsize = rsize(org, originialimagelabel);
            double scalex = (double) originialimagelabel.getWidth() / org.getWidth();
            double scaley = (double) originialimagelabel.getHeight() / org.getHeight();
            double scale = Math.min(scalex, scaley);
            int r_h = (int) (org.getHeight() * scale);
            int r_w = (int) (org.getWidth() * scale);
            int image_offset_y = (int) (1/scale*(originialimagelabel.getHeight()- r_h) / 2);
            int image_offset_x =  (int) (1/scale*(originialimagelabel.getWidth()- r_w) / 2);          
            int deltaX =  Math.min(org.getWidth(),(int) (1/scale*(mouse_m_x - bb_x)));
            int deltaY = Math.min(org.getHeight(),(int) (1/scale*(mouse_m_y - bb_y)));
            int x = Math.min(org.getWidth(),Math.max(0,(int)(1/scale*bb_x)-image_offset_x));
            int y = Math.min(org.getHeight(),Math.max(0,(int)(1/scale*bb_y)-image_offset_y));
            if(deltaX > 0 && deltaY > 0){
                    x = Math.min(org.getWidth(),Math.max(0,(int)(1/scale*bb_x)-image_offset_x));
                    y = Math.min(org.getHeight(),Math.max(0,(int)(1/scale*bb_y)-image_offset_y));
                }else if(deltaX < 0 && deltaY < 0){
                    x = Math.min(org.getWidth(),Math.max(0,(int)(1/scale*bb_x)-image_offset_x+deltaX));
                    y = Math.min(org.getHeight(),Math.max(0,(int)(1/scale*bb_y)-image_offset_y+deltaY));
                    deltaX = -deltaX;
                    deltaY = -deltaY;
                }else if(deltaX < 0 && deltaY > 0){
                    x = Math.min(org.getWidth(),Math.max(0,(int)(1/scale*bb_x)-image_offset_x+deltaX));
                    y = Math.min(org.getHeight(),Math.max(0,(int)(1/scale*bb_y)-image_offset_y));
                    deltaX = -deltaX;
                }else if(deltaX > 0 && deltaY < 0){
                    x = Math.min(org.getWidth(),Math.max(0,(int)(1/scale*bb_x)-image_offset_x));
                    y = Math.min(org.getHeight(),Math.max(0,(int)(1/scale*bb_y)-image_offset_y+deltaY));
                    deltaY = -deltaY;
                }           
            if(Math.abs(deltaX*deltaY) > 20 && Math.abs(deltaX) > 5 && Math.abs(deltaY) > 5){
            results.add(new NSFW_BoundingBox("Stomach / Belly - Exposed", 1.0, new int[]{x, y, deltaX+x, deltaY+y}));
            if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
            }
            new_bb = null;
            display(displayed_index);
        }else if(!converter.isEmpty()){
         display(displayed_index);
     }
     new_bb = null;
     
     originialimagelabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); 
    }//GEN-LAST:event_originialimagelabelMouseReleased

    private void originialimagelabelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_originialimagelabelMouseWheelMoved
       if(!converter.isEmpty()){
        NSWF_Image get = converter.get(displayed_index);
        List<NSFW_BoundingBox> results = get.getResults();
        int x = evt.getX();
        int y = evt.getY();
        int image_offset_x = 0;
        int image_offset_y = 0;
        Rectangle viewportsize = originialimagelabel.getBounds();
        BufferedImage org = get.getBufferedImage();
        double scalex = (double) originialimagelabel.getWidth() / org.getWidth();
        double scaley = (double) originialimagelabel.getHeight() / org.getHeight();
        double scale = Math.min(scalex, scaley);
        int r_h = (int) (org.getHeight()*scale);
        int r_w = (int) (org.getWidth()*scale); 
        image_offset_y = (viewportsize.height-r_h)/2;
        image_offset_x = (viewportsize.width-r_w)/2; 
        for (int i = 0; i < results.size(); i++) {
            NSFW_BoundingBox get1 = results.get(i);
            Rectangle bounding_box = get1.bounding_box;
            int bx = (int) (scale*bounding_box.x)+image_offset_x;
            int by = (int) (scale*bounding_box.y)+image_offset_y;
            int bw = (int) (scale*(bounding_box.width+bounding_box.x))+image_offset_x;
            int bh = (int) (scale*(bounding_box.height+bounding_box.y))+image_offset_y;
            if(bx <= x && bw >= x &&
               by <= y && bh >= y){
               int nw = (int) (evt.getWheelRotation() > 0 ? get1.bounding_box.width*0.9 : get1.bounding_box.width*1.1);
               int nh = (int) (evt.getWheelRotation() > 0 ? get1.bounding_box.height*0.9 : get1.bounding_box.height*1.1);
               int nx = evt.getWheelRotation() < 0 ? get1.bounding_box.x-Math.abs(get1.bounding_box.width-nw)/2 : get1.bounding_box.x+Math.abs(get1.bounding_box.width-nw)/2;
               int ny = evt.getWheelRotation() < 0 ? get1.bounding_box.y-Math.abs(get1.bounding_box.height-nh)/2: get1.bounding_box.y+Math.abs(get1.bounding_box.height-nh)/2;
               get1.bounding_box.setBounds(nx, ny, nw, nh);
               display(displayed_index);
               break;
            }
        }       
        }
    }//GEN-LAST:event_originialimagelabelMouseWheelMoved

    private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
        NSWF_Image get = converter.get(displayed_index);
        boolean remove = get.getResults().remove(pop_selection);
        display(displayed_index);
        if(editmode){
                get.setEdited(true);
        }
    }//GEN-LAST:event_deleteMenuItemActionPerformed

    private void belly_e_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_belly_e_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
           pressed.setName("Stomach / Belly - Exposed");
           if(editmode){
                converter.get(displayed_index).setEdited(true);
           }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_belly_e_RadioButtonMenuItemActionPerformed

    private void belly_c_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_belly_c_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Stomach / Belly - Covered");
            if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_belly_c_RadioButtonMenuItemActionPerformed

    private void fbreast_c_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fbreast_c_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Female Breast - Covered");
            if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_fbreast_c_RadioButtonMenuItemActionPerformed

    private void fbreast_e_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fbreast_e_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Female Breast - Exposed");
            if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_fbreast_e_RadioButtonMenuItemActionPerformed

    private void buttocks_c_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttocks_c_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Buttocks - Covered");
                    if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_buttocks_c_RadioButtonMenuItemActionPerformed

    private void buttocks_e_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttocks_e_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Buttocks - Exposed");
                    if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_buttocks_e_RadioButtonMenuItemActionPerformed
        
    private void fgen_c_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgen_c_RadioButtonMenuItemActionPerformed
        if(pressed!=null)pressed.setName("Female Genitalia - Covered");
        display(displayed_index);
        pressed = null;
         
    }//GEN-LAST:event_fgen_c_RadioButtonMenuItemActionPerformed
        
    private void fgen_e_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgen_e_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Female Genitalia - Exposed");
                    if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_fgen_e_RadioButtonMenuItemActionPerformed

    private void mbreast_c_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mbreast_c_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Male Breast - Covered");
                    if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_mbreast_c_RadioButtonMenuItemActionPerformed

    private void mbreast_e_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mbreast_e_RadioButtonMenuItemActionPerformed
        if(pressed!=null)pressed.setName("Male Breast - Exposed");
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_mbreast_e_RadioButtonMenuItemActionPerformed

    private void mgen_c_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mgen_c_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Male Genitalia - Covered");
                    if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_mgen_c_RadioButtonMenuItemActionPerformed

    private void mgen_e_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mgen_e_RadioButtonMenuItemActionPerformed
        if(pressed!=null)pressed.setName("Male Genitalia - Exposed");
        display(displayed_index);
    pressed = null;
    }//GEN-LAST:event_mgen_e_RadioButtonMenuItemActionPerformed

    private void ff_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ff_RadioButtonMenuItemActionPerformed
        if(pressed!=null){
            pressed.setName("Face - Female");
                    if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_ff_RadioButtonMenuItemActionPerformed

    private void fm_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fm_RadioButtonMenuItemActionPerformed
        if(pressed!=null)pressed.setName("Face - Male");
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_fm_RadioButtonMenuItemActionPerformed

    private void jSpinner2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner2StateChanged
        display(displayed_index);
    }//GEN-LAST:event_jSpinner2StateChanged

    private void fe_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fe_RadioButtonMenuItemActionPerformed
        if (pressed != null) {
            pressed.setName("Feet - Exposed");
                        if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_fe_RadioButtonMenuItemActionPerformed

    private void fc_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fc_RadioButtonMenuItemActionPerformed
        if (pressed != null) {
            pressed.setName("Feet - Covered");
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_fc_RadioButtonMenuItemActionPerformed

    private void ac_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ac_RadioButtonMenuItemActionPerformed
        if (pressed != null) {
            pressed.setName("Armpits - Covered");
                        if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_ac_RadioButtonMenuItemActionPerformed

    private void ae_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ae_RadioButtonMenuItemActionPerformed
        if (pressed != null) {
            pressed.setName("Armpits - Exposed");
                        if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_ae_RadioButtonMenuItemActionPerformed

    private void ane_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ane_RadioButtonMenuItemActionPerformed
        if (pressed != null) {
            pressed.setName("Anus - Exposed");
                        if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_ane_RadioButtonMenuItemActionPerformed

    private void anc_RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anc_RadioButtonMenuItemActionPerformed
        if (pressed != null) {
            pressed.setName("Anus - Covered");
            if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_anc_RadioButtonMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    File modelserveruploads = new File("model/uploads");
    for(File file: modelserveruploads.listFiles()){
        if (!file.isDirectory()){
             file.delete();
        }          
    }  
    }//GEN-LAST:event_formWindowClosing

    private void jLabel12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseClicked
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {        
                desktop.browse(new URI("https://www.patreon.com/cti"));
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_jLabel12MouseClicked

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {        
                desktop.browse(new URI("https://pury.fi/"));
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_jLabel1MouseClicked

    private void labelmodeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelmodeComboBoxActionPerformed
        display(displayed_index);
    }//GEN-LAST:event_labelmodeComboBoxActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int dialogResult = 0; 
        
        if(!converter.isEmpty()){
            int dialogButton = JOptionPane.YES_NO_OPTION;
            dialogResult = JOptionPane.showConfirmDialog(null, "This will clear all currently loaded data!\n"
                    + "Would you like to proceed?","Warning",dialogButton);
            
        }else{
            dialogResult = JOptionPane.YES_OPTION;
        }
        if (dialogResult == JOptionPane.YES_OPTION) {
            converter.clear();
        File[] files = getFiles("output/source/", new String[]{".txt"});
        if (files != null) {
            editmode = true;
            for (int i = 0; i < files.length; i++) {
                File file = new File("output/source/" + files[i].getName().replace(".txt", ".png"));
                if (file.exists()) {
                    NSWF_Image nswf_Image = new NSWF_Image(file, 1.0, null);
                    nswf_Image.setEditedsourcefileimage(file);
                    nswf_Image.setEditedsourcefiletxt(files[i]);
                    String[] readFile = readFile(files[i].getPath());
                    for (int j = 0; j < readFile.length; j++) {
                        if (readFile[j] != null) {
                            String[] splited = readFile[j].split("\\s+");

                            /*
                            int x_center = bounding_box.x+bounding_box.width/2;
                            int y_center = bounding_box.y+bounding_box.height/2;
                            double x_p = (double)x_center/(double)bufferedImage.getWidth();
                            double y_p = (double)y_center/(double)bufferedImage.getHeight();
                            double w_p = (double)bounding_box.width/(double)bufferedImage.getWidth();
                            double h_p = (double)bounding_box.height/(double)bufferedImage.getHeight();
                             */
                            int label = Integer.parseInt(splited[0]);
                            String name = NSFW_BoundingBox.getType(label).name();
                            double x_p = Double.parseDouble(splited[1]);
                            double y_p = Double.parseDouble(splited[2]);
                            double w_p = Double.parseDouble(splited[3]);
                            double h_p = Double.parseDouble(splited[4]);
                            int w = (int) (w_p * nswf_Image.getBufferedImage().getWidth());
                            int h = (int) (h_p * nswf_Image.getBufferedImage().getHeight());
                            int x = (int) (x_p * nswf_Image.getBufferedImage().getWidth() - w / 2);
                            int y = (int) (y_p * nswf_Image.getBufferedImage().getHeight() - h / 2);
                            int[] boxes = new int[4];
                            boxes[0] = x;
                            boxes[1] = y;
                            boxes[2] = w + x;
                            boxes[3] = h + y;
                            NSFW_BoundingBox box = new NSFW_BoundingBox(name, 1.0, boxes);
                            nswf_Image.getResults().add(box);
                        }
                    }
                    converter.add(nswf_Image);
                    if (converter.size() == 1) {
                        display(converter.size() - 1);
                        saveButton.setText("Save images");
                        saveButton.setEnabled(true);
                    }
                    jLabel9.setText("Preview: " + (displayed_index + 1) + " / " + converter.size());
                }
            }
        }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void stickerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stickerMenuItemActionPerformed
       if (pressed != null) {
            if(pressed.getSticker() != null){
                pressed.setSticker(null);
                display(displayed_index);
                pressed = null;
                return;
            }
            Frame parent = new Frame();  
            FileDialog fc = new FileDialog(parent, "Select Images", FileDialog.LOAD);
            fc.setDirectory("sticker");  
            fc.setFile("*.jpg;*.jpeg;*.png");

            fc.setVisible(true);
            File[] files = fc.getFiles();
            if(files.length != 1){
                pressed = null;
                return;
            }
            File file = files[0];
           try {
                BufferedImage read = ImageIO.read(file);
                pressed.setSticker(read);
           } catch (IOException ex) {
               Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
           }
            
            if(editmode){
                converter.get(displayed_index).setEdited(true);
            }
        }
        display(displayed_index);
        pressed = null;
    }//GEN-LAST:event_stickerMenuItemActionPerformed

    static  Process process;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            System.out.println("Could not load system LookAndFeel, loading Java L&F \"Metal\"");
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException exe) {
                ex.printStackTrace();
            }
       
            

        }

        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                final NSWFAPI nswfapi = new NSWFAPI();
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        if(process != null){
                            process.destroy();
                             System.err.println("Process killed");
                        }
                        System.err.println("Shutdown");
                    }
                }, "Shutdown-thread"));
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                       
                        
                        try {
                            File file = new File("model/localserver.exe");
                            ProcessBuilder processBuilder = new ProcessBuilder(file.getAbsolutePath());
                            processBuilder.directory(file.getParentFile());
                            processBuilder.redirectErrorStream(true);
                            process = processBuilder.start();
                             
                            InputStream stderr = process.getInputStream();
                            InputStreamReader isr = new InputStreamReader(stderr);
                            BufferedReader br = new BufferedReader(isr);
                            String line = null;
                            boolean started = false;
                            while ((line = br.readLine()) != null) {
                                if(line.contains("Running")){
                                    if (nswfapi != null) {
                                        nswfapi.setAIstate(1);
                                        started = true;
                                        break;
                                    }
                                }
                            }
                            if(started){
                                nswfapi.puryRadioButton.setSelected(false);
                                nswfapi.localRadioButton.setSelected(true);
                            }else{
                                nswfapi.puryRadioButton.setSelected(true);
                                nswfapi.localRadioButton.setSelected(false);
                            }
                            
                            process.waitFor();    
                           
                            System.out.println("Waiting ...");
                            try{
                            System.out.println("Returned Value :" + process.exitValue());
                            }catch(java.lang.IllegalThreadStateException ex){
                                ex.printStackTrace();
                            }                          
                        } catch (IOException ex) {
                            Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                          
                    }
                });
                t.start();
                
                nswfapi.setVisible(true);
                if (args.length > 0) {
                 String s = args[0];
                }
            }
        });
        
    }
    
    public void restartAI(){
         Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                       
                        
                        try {
                            if(process != null){
                                process.destroy();
                                System.err.println("Process killed");
                            }
                            setAIstate(3);
                            File file = new File("model/localserver.exe");
                            ProcessBuilder processBuilder = new ProcessBuilder(file.getAbsolutePath());
                            processBuilder.directory(file.getParentFile());
                            processBuilder.redirectErrorStream(true);
                            process = processBuilder.start();
                             
                            InputStream stderr = process.getInputStream();
                            InputStreamReader isr = new InputStreamReader(stderr);
                            BufferedReader br = new BufferedReader(isr);
                            String line = null;
                            boolean started = false;
                            while ((line = br.readLine()) != null) {
                                if(line.contains("Running")){
                                        setAIstate(1);
                                        started = true;
                                        break;                                    
                                }
                            }
                            if(started){
                                puryRadioButton.setSelected(false);
                                localRadioButton.setSelected(true);
                            }else{
                                puryRadioButton.setSelected(true);
                                localRadioButton.setSelected(false);
                            }
                            
                            process.waitFor();    
                            System.out.println("Waiting ...");
                        } catch (IOException ex) {
                            Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                          
                    }
                });
         t.start();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButtonMenuItem ac_RadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem ae_RadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem anc_RadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem ane_RadioButtonMenuItem;
    public static javax.swing.JCheckBox anus_c_button;
    public static javax.swing.JCheckBox anus_e_button;
    private javax.swing.ButtonGroup apiGroup;
    public static javax.swing.JCheckBox armpits_c_button;
    public static javax.swing.JCheckBox armpits_e_button;
    public static javax.swing.JRadioButton barButton;
    private javax.swing.JRadioButtonMenuItem belly_c_RadioButtonMenuItem;
    public static javax.swing.JCheckBox belly_c_button;
    private javax.swing.JRadioButtonMenuItem belly_e_RadioButtonMenuItem;
    public static javax.swing.JCheckBox belly_e_button;
    public static javax.swing.JRadioButton blurButton;
    private javax.swing.JRadioButtonMenuItem buttocks_c_RadioButtonMenuItem;
    public static javax.swing.JCheckBox buttocks_c_button;
    private javax.swing.JRadioButtonMenuItem buttocks_e_RadioButtonMenuItem;
    public static javax.swing.JCheckBox buttocks_e_button;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JLabel censoredlimagelabel;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JButton editButton;
    private javax.swing.JCheckBox faceplusplusCheckBox;
    private javax.swing.JRadioButtonMenuItem fbreast_c_RadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem fbreast_e_RadioButtonMenuItem;
    public static javax.swing.JCheckBox fbreats_c_button;
    public static javax.swing.JCheckBox fbreats_e_button;
    private javax.swing.JRadioButtonMenuItem fc_RadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem fe_RadioButtonMenuItem;
    public static javax.swing.JCheckBox feet_c_button;
    public static javax.swing.JCheckBox feet_e_button;
    public static javax.swing.JCheckBox ff_CheckBox;
    private javax.swing.JRadioButtonMenuItem ff_RadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem fgen_c_RadioButtonMenuItem;
    public static javax.swing.JCheckBox fgen_c_button;
    private javax.swing.JRadioButtonMenuItem fgen_e_RadioButtonMenuItem;
    public static javax.swing.JCheckBox fgen_e_button;
    public static javax.swing.JCheckBox fm_CheckBox;
    private javax.swing.JRadioButtonMenuItem fm_RadioButtonMenuItem;
    private javax.swing.JCheckBox ignoreCheckBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    public static javax.swing.JSpinner jSpinner1;
    public static javax.swing.JSpinner jSpinner2;
    private javax.swing.JToggleButton jToggleButton1;
    public static javax.swing.JComboBox<String> labelmodeComboBox;
    private javax.swing.JRadioButton localRadioButton;
    private javax.swing.JRadioButtonMenuItem mbreast_c_RadioButtonMenuItem;
    public static javax.swing.JCheckBox mbreast_c_button;
    private javax.swing.JRadioButtonMenuItem mbreast_e_RadioButtonMenuItem;
    public static javax.swing.JCheckBox mbreast_e_button;
    private javax.swing.JRadioButtonMenuItem mgen_c_RadioButtonMenuItem;
    public static javax.swing.JCheckBox mgen_c_button;
    private javax.swing.JRadioButtonMenuItem mgen_e_RadioButtonMenuItem;
    public static javax.swing.JCheckBox mgen_e_button;
    private javax.swing.JRadioButton noaiRadioButton;
    private javax.swing.JLabel originialimagelabel;
    public static javax.swing.JRadioButton pixelButton;
    private javax.swing.JRadioButton puryRadioButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel scoreLabel;
    private javax.swing.JButton startButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JMenuItem stickerMenuItem;
    // End of variables declaration//GEN-END:variables
}
