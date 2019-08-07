/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package puryfi;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import static puryfi.NSWFAPI.output_folder;
import static puryfi.NSWFAPI.rsize;
import org.json.JSONObject;

/**
 *
 * @author 0131
 */
public class NSWF_Image {
    
    
    List<NSFW_BoundingBox> results = new ArrayList<>();
    Double nsfw_score;
    File image;
    JSONObject json;
    String alias = "image_"+hashCode();
    
    boolean ignore = false;
    
    boolean edited = false;
    File editedsourcefileimage = null;
    File editedsourcefiletxt = null;

    public NSWF_Image(File image, Double nsfw_score,JSONObject json) {
        this.image = image;
        this.nsfw_score = nsfw_score;
        this.json = json;
    }

    public List<NSFW_BoundingBox> getResults() {
        return results;
    }

    public void setResults(List<NSFW_BoundingBox> results) {
        this.results = results;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public Double getNsfw_score() {
        return nsfw_score;
    }

    public void setNsfw_score(Double nsfw_score) {
        this.nsfw_score = nsfw_score;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public File getEditedsourcefileimage() {
        return editedsourcefileimage;
    }

    public void setEditedsourcefileimage(File editedsourcefileimage) {
        this.editedsourcefileimage = editedsourcefileimage;
    }

    public File getEditedsourcefiletxt() {
        return editedsourcefiletxt;
    }

    public void setEditedsourcefiletxt(File editedsourcefiletxt) {
        this.editedsourcefiletxt = editedsourcefiletxt;
    }

   
    
    
    
    BufferedImage img;
    
    public BufferedImage getBufferedImage(){
        if(img != null){
            return img;
        }
        try {
            img = ImageIO.read(image);         
            return img;
        } catch (IOException ex) {
            Logger.getLogger(NSWF_Image.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public BufferedImage getBufferedImageCopy(){
        if(img != null){
            return copyImage(img);
        }
        try {
            img = ImageIO.read(image);         
            return copyImage(img);
        } catch (IOException ex) {
            Logger.getLogger(NSWF_Image.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    public BufferedImage getCensoredImage(){
        BufferedImage paintedImage = getBufferedImageCopy();
        Graphics g = paintedImage.getGraphics();
        List<Rectangle> blurboxes = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Rectangle bounding_box = results.get(i).getBounding_box();
            if(results.get(i).getSticker() != null){
                    paintSticker(g, results.get(i).getSticker(), bounding_box);
            }else if (NSWFAPI.pixelButton.isSelected()) {
                if(results.get(i).isCensored()){
                    pixelate(paintedImage, bounding_box);
                }
            } else if(NSWFAPI.barButton.isSelected()) {
                if (results.get(i).isCensored()) {
                    g.setColor(Color.BLACK);
                    g.fillRect(bounding_box.x, bounding_box.y, bounding_box.width, bounding_box.height);
                }
            }else{
                if (results.get(i).isCensored()) {
                    blurboxes.add(bounding_box);
                }
            }
        }
        if(NSWFAPI.blurButton.isSelected()){
            blurbuff = copyImage(paintedImage);
            fastblur(blurbuff, Math.min(500,(int) NSWFAPI.jSpinner1.getValue()));
            blur(paintedImage, blurboxes);
        }
        g.dispose();
        return paintedImage;
    }
    
    BufferedImage cache_resized = null;
    
    public BufferedImage getCachedResizedImage(JLabel label){
        try {    
            if(cache_resized == null ){
                BufferedImage org = ImageIO.read(image);
                cache_resized = rsize(org, label);  
                return cache_resized;
            }         
            return cache_resized;
        } catch (IOException ex) {
            Logger.getLogger(NSWF_Image.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cache_resized;
    }
    
    public BufferedImage getPaintedImage() {
        BufferedImage org = getBufferedImageCopy();
        Graphics2D g = (Graphics2D) org.getGraphics();
        float thickness = 2;
        g.setStroke(new BasicStroke(thickness));
        for (int i = 0; i < results.size(); i++) {
            if(results.get(i).checkOptions()){
                Rectangle bounding_box = results.get(i).getBounding_box();
                g.setColor(getColor(results.get(i).getConfidence()));
                g.drawRect(bounding_box.x, bounding_box.y, bounding_box.width, bounding_box.height);
                String s = results.get(i).getHeadline();
                g.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g.getFontMetrics();
                int x = bounding_box.x;
                int y = bounding_box.y-fm.getHeight()+8;
                String[] splittedText = s.split("\n");
                for (String line : splittedText) {
                    y-= g.getFontMetrics().getHeight();
                }
                for (String line : splittedText) {
                    y+= g.getFontMetrics().getHeight();
                    g.setColor(Color.WHITE);
                    g.drawString(line, ShiftWest(x, 1), ShiftNorth(y, 1));
                    g.drawString(line, ShiftWest(x, 1), ShiftSouth(y, 1));
                    g.drawString(line, ShiftEast(x, 1), ShiftNorth(y, 1));
                    g.drawString(line, ShiftEast(x, 1), ShiftSouth(y, 1));
                    g.setColor(Color.BLACK);
                    g.drawString(line, x, y);
                }
            }
        }
        g.dispose();
        return org;
    }
    
    public BufferedImage getResizedPaintedImage(JLabel viewport) {
        BufferedImage org = getBufferedImage();
        BufferedImage org_r = NSWFAPI.rsize(org, viewport);
        double scalex = (double) viewport.getWidth() / org.getWidth();
        double scaley = (double) viewport.getHeight() / org.getHeight();
        double scale = Math.min(scalex, scaley);
        Graphics2D g = (Graphics2D) org_r.getGraphics();
        float thickness = 2;
        g.setStroke(new BasicStroke(thickness));
        for (int i = 0; i < results.size(); i++) {
            if(results.get(i).checkOptions()){
                Rectangle bounding_box = results.get(i).getBounding_box();
                if((results.get(i).equals(NSWFAPI.del_buf))){
                    g.setColor(Color.PINK);
                }else{
                    g.setColor(getColor(results.get(i).getConfidence()));
                }
                
                
                g.drawRect((int) (bounding_box.x*scale), (int) (bounding_box.y*scale), (int) (bounding_box.width*scale), (int) (bounding_box.height*scale));
                String s = results.get(i).getHeadline();
                
                
                g.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g.getFontMetrics();
                int x = (int) (bounding_box.x*scale);
                int y = (int) ((bounding_box.y)*scale)-fm.getHeight()+8;
                String[] splittedText = s.split("\n");
                for (String line : splittedText) {
                    y-= g.getFontMetrics().getHeight();
                }
                for (String line : splittedText) {
                    y+= g.getFontMetrics().getHeight();
                    g.setColor(Color.WHITE);
                    g.drawString(line, ShiftWest(x, 1), ShiftNorth(y, 1));
                    g.drawString(line, ShiftWest(x, 1), ShiftSouth(y, 1));
                    g.drawString(line, ShiftEast(x, 1), ShiftNorth(y, 1));
                    g.drawString(line, ShiftEast(x, 1), ShiftSouth(y, 1));
                    g.setColor(Color.BLACK);
                    g.drawString(line, x, y);
                }

            }
        }
        g.dispose();
        return org_r;
    }
    
    int ShiftNorth(int p, int distance) {
        return (p - distance);
    }

    int ShiftSouth(int p, int distance) {
        return (p + distance);
    }

    int ShiftEast(int p, int distance) {
        return (p + distance);
    }

    int ShiftWest(int p, int distance) {
        return (p - distance);
    }

    public Color getColor(double score){
        if(score > 0.8){
            return Color.RED;
        }
        if(score > 0.65){
            return Color.BLUE;
        }
        if(score > 0.5){
            return Color.GREEN;
        }
        return Color.YELLOW;
    }
    
    public void saveCensoredImage(){
        try {
            File dir = new File(output_folder+"/censored");
            if(!dir.exists())dir.mkdir();
            BufferedImage censoredImage = getCensoredImage();
            File outputfile = new File(NSWFAPI.output_folder+"/censored/"+alias+".png");
            ImageIO.write(censoredImage, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(NSWF_Image.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveImage(){
        saveCensoredImage();
        try {
            File dir = new File(output_folder+"/identified");
            if(!dir.exists())dir.mkdir();
            BufferedImage censoredImage = getPaintedImage();
            File outputfile = new File(NSWFAPI.output_folder+"/identified/"+alias+".png");
            ImageIO.write(censoredImage, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(NSWF_Image.class.getName()).log(Level.SEVERE, null, ex);
        }

         try {
            File dir = new File(output_folder+"/source");
            if (!dir.exists()) {
                dir.mkdir();
            }
            BufferedImage censoredImage = getBufferedImage();
            File outputfile = new File(NSWFAPI.output_folder + "/source/" + alias + ".png");
            ImageIO.write(censoredImage, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(NSWF_Image.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            File dir = new File(output_folder + "/source");
            if (!dir.exists()) {
                dir.mkdir();
            }
            PrintWriter writer = new PrintWriter(NSWFAPI.output_folder + "/source/" + alias+".txt", "UTF-8");
            for (int i = 0; i < results.size(); i++) {
                NSFW_BoundingBox get = results.get(i);
                //if(get.isCensored() || (get.getType().equals(NSFW_BoundingBox.Type.FACE_MALE) || get.getType().equals(NSFW_BoundingBox.Type.FACE_FEMALE))){
                    Rectangle bounding_box = get.bounding_box;
                    int index = get.getType().ordinal();
                    BufferedImage bufferedImage = getBufferedImage();                    
                    int x_center = bounding_box.x+bounding_box.width/2;
                    int y_center = bounding_box.y+bounding_box.height/2;
                    double x_p = (double)x_center/(double)bufferedImage.getWidth();
                    double y_p = (double)y_center/(double)bufferedImage.getHeight();
                    double w_p = (double)bounding_box.width/(double)bufferedImage.getWidth();
                    double h_p = (double)bounding_box.height/(double)bufferedImage.getHeight();
                    String hit = index+" "+x_p+" "+y_p+" "+w_p+" "+h_p;
                    writer.println(hit);
                //}
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(NSWF_Image.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(json != null){
        File jdir = new File(output_folder+"/json");
        if(!jdir.exists())jdir.mkdir();
        try (FileWriter file = new FileWriter(output_folder+ "/json/" + alias+".json")) {  
             file.write(json.toString());
         } catch (IOException ex) {
             Logger.getLogger(NSWFAPI.class.getName()).log(Level.SEVERE, null, ex);
         }
        }
    }
    
    
    public void pixelate(BufferedImage img, Rectangle boundingbox) {
        // How big should the pixelations be?
        int PIX_SIZE = (int) NSWFAPI.jSpinner1.getValue();

        Raster src = img.getData();

// Create an identically-sized output raster
        WritableRaster dest = src.createCompatibleWritableRaster();

// Loop through every PIX_SIZE pixels, in both x and y directions
        for (int y = 0; y < src.getHeight(); y += PIX_SIZE) {
            for (int x = 0; x < src.getWidth(); x += PIX_SIZE) {

                // Copy the pixel
                double[] pixel = new double[3];
                pixel = src.getPixel(x, y, pixel);

                // "Paste" the pixel onto the surrounding PIX_SIZE by PIX_SIZE neighbors
                // Also make sure that our loop never goes outside the bounds of the image
                for (int yd = y; (yd < y + PIX_SIZE) && (yd < dest.getHeight()); yd++) {
                    for (int xd = x; (xd < x + PIX_SIZE) && (xd < dest.getWidth()); xd++) {
                        if (xd >= boundingbox.x && xd <= boundingbox.x + boundingbox.width
                                && yd >= boundingbox.y && yd <= boundingbox.y + boundingbox.height) {
                            dest.setPixel(xd, yd, pixel);
                        } else {
                            double[] pix = new double[3];
                            dest.setPixel(xd, yd, src.getPixel(xd, yd, pix));
                        }

                    }
                }
            }
        }

// Save the raster back to the Image
        img.setData(dest);
    }

    BufferedImage blurbuff = null;
    
        private static BufferedImage blurBorder(BufferedImage input, double border)
    {
        int w = input.getWidth();
        int h = input.getHeight();
        BufferedImage output = new BufferedImage(
            w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = output.createGraphics();
        g.drawImage(input, 0, 0, null);

        g.setComposite(AlphaComposite.DstOut);
        Color c0 = new Color(0,0,0,255);
        Color c1 = new Color(0,0,0,0);

        double cy = border;
        double cx = border;

        // Left
        g.setPaint(new GradientPaint(
            new Point2D.Double(0, cy), c0,
            new Point2D.Double(cx,cy), c1));
        g.fill(new Rectangle2D.Double(
            0, cy, cx, h-cy-cy));

        // Right
        g.setPaint(new GradientPaint(
            new Point2D.Double(w-cx, cy), c1,
            new Point2D.Double(w,cy), c0));
        g.fill(new Rectangle2D.Double(
            w-cx, cy, cx, h-cy-cy));

        // Top
        g.setPaint(new GradientPaint(
            new Point2D.Double(cx, 0), c0,
            new Point2D.Double(cx, cy), c1));
        g.fill(new Rectangle2D.Double(
            cx, 0, w-cx-cx, cy));

        // Bottom
        g.setPaint(new GradientPaint(
            new Point2D.Double(cx, h-cy), c1,
            new Point2D.Double(cx, h), c0));
        g.fill(new Rectangle2D.Double(
            cx, h-cy, w-cx-cx, cy));


        // Top Left
        g.setPaint(new RadialGradientPaint(
            new Rectangle2D.Double(0, 0, cx+cx, cy+cy),
            new float[]{0,1}, new Color[]{c1, c0}, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(0, 0, cx, cy));

        // Top Right
        g.setPaint(new RadialGradientPaint(
            new Rectangle2D.Double(w-cx-cx, 0, cx+cx, cy+cy),
            new float[]{0,1}, new Color[]{c1, c0}, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(w-cx, 0, cx, cy));

        // Bottom Left
        g.setPaint(new RadialGradientPaint(
            new Rectangle2D.Double(0, h-cy-cy, cx+cx, cy+cy),
            new float[]{0,1}, new Color[]{c1, c0}, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(0, h-cy, cx, cy));

        // Bottom Right
        g.setPaint(new RadialGradientPaint(
            new Rectangle2D.Double(w-cx-cx, h-cy-cy, cx+cx, cy+cy),
            new float[]{0,1}, new Color[]{c1, c0}, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(w-cx, h-cy, cx, cy));

        g.dispose();

        return output;
    }

       
    
    public void blur(BufferedImage im, List<Rectangle> blurboxes) {
       // Copy rectanglearea

        List<BufferedImage> masks = new ArrayList<>();
        for (int i = 0; i < blurboxes.size(); i++) {
            Rectangle box = blurboxes.get(i);
            //BufferedImage center = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
            
            BufferedImage mask = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = mask.createGraphics();
            Color transparent = new Color(255, 0, 0, 0);
            Color fill = Color.RED;
            RadialGradientPaint rgp = new RadialGradientPaint(
                    new Point2D.Double(box.x+box.width/2, box.y+box.height/2),
                    box.width/2+box.height/2,
                    new float[]{0.0f, (Float)NSWFAPI.jSpinner2.getValue(), 1f},
                    new Color[]{transparent, transparent, fill});
            g2d.setPaint(rgp);
            AffineTransform tr2 = new AffineTransform();
            double scale_x = box.width > box.height? 1.0 : 1.0/((double)box.height/(double)box.width);
            double scale_y = box.height > box.width? 1.0 : 1.0/((double)box.width/(double)box.height);
           
            int center_x = box.x+box.width/2;
            int center_y = box.y+box.height/2;
            int new_center_x = (int) (center_x*scale_x);
            int new_center_y = (int) (center_y*scale_y);
            int sx = center_x-new_center_x;
            int sy = center_y-new_center_y;
            int sxm = -(int) (sx/scale_x);
            int sym = -(int) (sy/scale_y);
            tr2.translate(sx, sy);
            
            tr2.scale(scale_x, scale_y);         
            g2d.setTransform(tr2);  
           
           
            double r_x = 1.0/scale_x;
            double r_y = 1.0/scale_y;

            g2d.fill(new Rectangle(sxm, sym, (int) (mask.getWidth()*r_x), (int) (mask.getHeight()*r_y)));
            
            //g2d.fill(new Rectangle(0, 0, (int) (mask.getWidth()), (int) (mask.getHeight())));
            g2d.dispose();
            masks.add(mask);
        }
        
       
       
        BufferedImage masked = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = masked.createGraphics();
        
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, masked.getWidth(), masked.getHeight());
            g2d.drawImage(im, 0, 0, null);
            g2d.setComposite(AlphaComposite.DstIn);
            for (int i = 0; i < masks.size(); i++) {
                g2d.drawImage(masks.get(i), 0, 0, null);
            }
            g2d.dispose();
        
        
        //BufferedImage composite = new BufferedImage(blurbuff.getWidth(), blurbuff.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g2d = blurbuff.createGraphics();
        //g2d.drawImage(blurbuff, blurbuff.getWidth() - blurbuff.getWidth(), 0, null);
        g2d.drawImage(masked, 0, 0, null);
        g2d.dispose();
        
        Graphics2D g2 = im.createGraphics();
        g2.drawImage(blurbuff, 0,0,im.getWidth(),im.getHeight(), null);
        g2.dispose();
        
        
    }
    public static void fastblur(BufferedImage img, int radius) {

        if (radius < 1) {
            return;
        }
        int w = img.getWidth();
        int h = img.getHeight();
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, p1, p2, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];
        int vmax[] = new int[Math.max(w, h)];
        int[] pix = new int[w * h];

        //img.getPixels(pix, 0, w, 0,0,w, h);
        pix = img.getRGB(0, 0, w, h, pix, 0, w);
        int dv[] = new int[256 * div];
        for (i = 0; i < 256 * div; i++) {
            dv[i] = (i / div);
        }

        yw = yi = 0;

        for (y = 0; y < h; y++) {
            rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                rsum += (p & 0xff0000) >> 16;
                gsum += (p & 0x00ff00) >> 8;
                bsum += p & 0x0000ff;
            }
            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                    vmax[x] = Math.max(x - radius, 0);
                }
                p1 = pix[yw + vmin[x]];
                p2 = pix[yw + vmax[x]];

                rsum += ((p1 & 0xff0000) - (p2 & 0xff0000)) >> 16;
                gsum += ((p1 & 0x00ff00) - (p2 & 0x00ff00)) >> 8;
                bsum += (p1 & 0x0000ff) - (p2 & 0x0000ff);
                yi++;
            }
            yw += w;
        }

        for (x = 0; x < w; x++) {
            rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                rsum += r[yi];
                gsum += g[yi];
                bsum += b[yi];
                yp += w;
            }
            yi = x;
            for (y = 0; y < h; y++) {
                pix[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
                if (x == 0) {
                    vmin[y] = Math.min(y + radius + 1, hm) * w;
                    vmax[y] = Math.max(y - radius, 0) * w;
                }
                p1 = x + vmin[y];
                p2 = x + vmax[y];

                rsum += r[p1] - r[p2];
                gsum += g[p1] - g[p2];
                bsum += b[p1] - b[p2];

                yi += w;
            }
        }

        //img.setPixels(pix,0, w,0,0,w,h);
        img.setRGB(0, 0, w, h, pix, 0, w);
    }
    
    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    private void paintSticker(Graphics g, BufferedImage sticker, Rectangle bounding_box) {
        Dimension stickerdim = new Dimension(sticker.getWidth(), sticker.getHeight());
        Dimension box = new Dimension(bounding_box.width,  bounding_box.height);
        Dimension scaledDimension = getScaledDimension(stickerdim, box);
        g.drawImage(sticker, bounding_box.x+(box.width-scaledDimension.width)/2, bounding_box.y+(box.height-scaledDimension.height)/2, scaledDimension.width, scaledDimension.height, null);
        
    }

    Dimension getScaledDimension(Dimension imageSize, Dimension boundary) {

        double widthRatio = boundary.getWidth() / imageSize.getWidth();
        double heightRatio = boundary.getHeight() / imageSize.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
    }
    
    
    
}
