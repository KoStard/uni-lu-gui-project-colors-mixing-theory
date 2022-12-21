package uni.lu.gui.project;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JComponent;

public class ContentPanel extends JComponent {
    Image image = null;

    public ContentPanel() {
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public void paintComponent(Graphics g) {
        g = (Graphics2D) g;
        g.setColor(Color.red);
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }
}
