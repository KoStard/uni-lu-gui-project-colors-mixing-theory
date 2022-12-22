package uni.lu.gui.project;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

public class MainFrame {
    public MainFrame() {
        JFrame frame = new JFrame("Ruben's GUI Project - Color Mixing Theory");
        frame.setSize(800, 600);

        ContentPanel content = new ContentPanel();
        content.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        frame.add(content, "Center");

        ControlPanel controlPanel = new ControlPanel();
        controlPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 300));

        frame.add(controlPanel, "South");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        ImageCreatingWorker imageCreatingWorker = new ImageCreatingWorker(30, controlPanel, content);
        imageCreatingWorker.execute();

        // Triggering the image update when the controls are changed.
        controlPanel.addControlChangeListener(imageCreatingWorker);

        // Triggering the image update when the window is resized.
        content.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                imageCreatingWorker.controlChanged();
            }
        });

    }
}
