package uni.lu.gui.project;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// JSplitPane
// Possibly JTabbedPane
// JComboBox
// JSlider
// JLabel

// Variables
// Angle
// Distance
// Intensity
// Radius
// Additive vs Subtractive

// Possible additions
// Shape
// Background color
public class ControlPanel extends JPanel {

    private SynchronizedSliderSpinnerPair angleInput;
    private SynchronizedSliderSpinnerPair distanceInput;
    private SynchronizedSliderSpinnerPair intensityInput;
    private SynchronizedSliderSpinnerPair radiusInput;
    private JComboBox<PixelChangeMode> pixelChangeModeComboBox;
    private SynchronizedSliderSpinnerPair intensityDropEffectInput;

    public ControlPanel() {
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
        setLayout(new GridLayout(6, 2));

        angleInput = new SynchronizedSliderSpinnerPair(60, 0, 360, "Angle");
        angleInput.addToComponent(this);

        distanceInput = new SynchronizedSliderSpinnerPair(50, 0, 200, "Distance");
        distanceInput.addToComponent(this);

        intensityInput = new SynchronizedSliderSpinnerPair(100, 0, 100, "Intensity");
        intensityInput.addToComponent(this);

        intensityDropEffectInput = new SynchronizedSliderSpinnerPair(0, 0, 100, "Intensity drop effect");
        intensityDropEffectInput.addToComponent(this);

        radiusInput = new SynchronizedSliderSpinnerPair(75, 0, 150, "Radius");
        radiusInput.addToComponent(this);

        final PixelChangeMode defaultPixelChangeMode = PixelChangeMode.Additive;
        pixelChangeModeComboBox = new JComboBox<PixelChangeMode>(PixelChangeMode.values());
        pixelChangeModeComboBox.setBorder(BorderFactory.createTitledBorder("Pixel Update Mode"));
        pixelChangeModeComboBox.setSelectedItem(defaultPixelChangeMode);
        add(pixelChangeModeComboBox);

        JLabel pixelChangeModeLabel = new JLabel("");
        updatePixelChangeModeLabel(pixelChangeModeLabel, defaultPixelChangeMode);
        pixelChangeModeComboBox.addItemListener(e -> updatePixelChangeModeLabel(pixelChangeModeLabel, (PixelChangeMode) e.getItem()));
        add(pixelChangeModeLabel);
    }

    private void updatePixelChangeModeLabel(JLabel label, PixelChangeMode pixelChangeMode) {
        label.setText("Selected pixel update mode: " + pixelChangeMode);
    }

    public int getAngle() {
        return angleInput.getValue();
    }

    public int getDistance() {
        return distanceInput.getValue();
    }

    public int getIntensity() {
        return intensityInput.getValue();
    }

    public int getRadius() {
        return radiusInput.getValue();
    }

    public int getIntensityDropEffect() {
        return intensityDropEffectInput.getValue();
    }

    public void addControlChangeListener(ControlChangeListener controlChangeListener) {
        angleInput.addControlChangeListener(controlChangeListener);
        distanceInput.addControlChangeListener(controlChangeListener);
        intensityInput.addControlChangeListener(controlChangeListener);
        intensityDropEffectInput.addControlChangeListener(controlChangeListener);
        radiusInput.addControlChangeListener(controlChangeListener);
        pixelChangeModeComboBox.addActionListener(e -> controlChangeListener.controlChanged());
    }

    public PixelChangeMode getPixelChangeMode() {
        return (PixelChangeMode) pixelChangeModeComboBox.getSelectedItem();
    }
}

class SynchronizedSliderSpinnerPair implements ChangeListener {
    private JSlider slider;
    private JSpinner spinner;

    public SynchronizedSliderSpinnerPair(int initialValue, int min, int max, String title) {
        slider = new JSlider(min, max, initialValue);
        slider.setMinorTickSpacing(1);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
        slider.setPaintTrack(true);
        slider.setBorder(BorderFactory.createTitledBorder(title));

        spinner = new JSpinner(new SpinnerNumberModel(initialValue, min, max, 1));

        slider.addChangeListener(this);
        spinner.addChangeListener(this);
    }

    // When putting a value that matches the current value, the event is not fired,
    // hence preventing an infinite loop.
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == slider) {
            spinner.setValue(slider.getValue());
        } else if (e.getSource() == spinner) {
            slider.setValue((int) spinner.getValue());
        } else {
            throw new UnsupportedOperationException("The event source for this listener is not supported.");
        }
    }

    public JSlider getSlider() {
        return slider;
    }

    public JSpinner getSpinner() {
        return spinner;
    }

    public int getValue() {
        return slider.getValue();
    }

    public void addToComponent(JComponent component) {
        component.add(slider);
        component.add(spinner);
    }

    public void addControlChangeListener(ControlChangeListener controlChangeListener) {
        slider.addChangeListener((e) -> controlChangeListener.controlChanged());
        spinner.addChangeListener((e) -> controlChangeListener.controlChanged());
    }
}