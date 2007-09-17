package org.esa.beam.visat.toolviews.pin;

import org.esa.beam.framework.datamodel.GcpGeoCoding;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.Pin;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.ui.TableLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marco Peters.
 *
 * @author Marco Peters
 * @version $Revision:$ $Date:$
 */
public class GcpGeoCodingForm extends JPanel {

    private JTextField methodTextField;
    private JTextField rmseLatTextField;
    private JTextField rmseLonTextField;

    private JComboBox methodComboBox;
    private JToggleButton attachButton;
    private JTextField warningLabel;

    private Product currentProduct;
    private Format rmseNumberFormat;

    private Map<Product, GeoCoding> geoCodingMap;

    public GcpGeoCodingForm() {
        geoCodingMap = new HashMap<Product, GeoCoding>();
        rmseNumberFormat = new RmseNumberFormat();
        initComponents();
    }

    private void initComponents() {
        TableLayout layout = new TableLayout(2);
        this.setLayout(layout);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableWeightY(1.0);
        layout.setTableFill(TableLayout.Fill.BOTH);
        layout.setTablePadding(2, 2);
        layout.setColumnWeightX(0, 0.5);
        layout.setColumnWeightX(1, 0.5);

        add(createInfoPanel());
        add(createAttachDetachPanel());

        updateUIState();
    }

    private JPanel createInfoPanel() {
        TableLayout layout = new TableLayout(2);
        layout.setTablePadding(2, 4);
        layout.setColumnWeightX(0, 0.0);
        layout.setColumnWeightX(1, 1.0);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.BOTH);

        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder("Current GCP Geo-Coding"));
        panel.add(new JLabel("Method:"));
        methodTextField = new JTextField();
        methodTextField.setEditable(false);
        methodTextField.setHorizontalAlignment(JLabel.TRAILING);
        panel.add(methodTextField);
        rmseLatTextField = new JTextField();
        rmseLatTextField.setEditable(false);
        rmseLatTextField.setHorizontalAlignment(JLabel.TRAILING);
        panel.add(new JLabel("RMSE Lat:"));
        panel.add(rmseLatTextField);

        rmseLonTextField = new JTextField();
        rmseLonTextField.setEditable(false);
        rmseLonTextField.setHorizontalAlignment(JLabel.TRAILING);
        panel.add(new JLabel("RMSE Lon:"));
        panel.add(rmseLonTextField);
        return panel;
    }

    private JPanel createAttachDetachPanel() {
        methodComboBox = new JComboBox(GcpGeoCoding.Method.values());
        methodComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateUIState();
            }
        });
        attachButton = new JToggleButton();
        attachButton.setName("attachButton");

        AbstractAction applyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                if (attachButton.isSelected()) {
                    attachGeoCoding(currentProduct);
                } else {
                    detachGeoCoding(currentProduct);
                }
            }
        };

        attachButton.setAction(applyAction);
        attachButton.setHideActionText(true);
        warningLabel = new JTextField();
        warningLabel.setEditable(false);

        TableLayout layout = new TableLayout(2);
        layout.setTablePadding(2, 4);
        layout.setColumnWeightX(0, 0.0);
        layout.setColumnWeightX(1, 1.0);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.BOTH);
        layout.setCellColspan(2, 0, 2);
        layout.setCellFill(2, 0, TableLayout.Fill.VERTICAL);
        layout.setCellAnchor(2, 0, TableLayout.Anchor.CENTER);

        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder("Attach / Detach GCP Geo-Coding"));
        panel.add(new JLabel("Method:"));
        panel.add(methodComboBox);
        panel.add(new JLabel("Status:"));
        panel.add(warningLabel);
        panel.add(attachButton);

        return panel;
    }

    void updateUIState() {
        if (currentProduct != null && currentProduct.getGeoCoding() instanceof GcpGeoCoding) {
            final GcpGeoCoding gcpGeoCoding = (GcpGeoCoding) currentProduct.getGeoCoding();

            rmseLatTextField.setText(rmseNumberFormat.format(gcpGeoCoding.getRmseLat()));
            rmseLonTextField.setText(rmseNumberFormat.format(gcpGeoCoding.getRmseLon()));
            methodTextField.setText(gcpGeoCoding.getMethod().getName());
            methodComboBox.setSelectedItem(gcpGeoCoding.getMethod());

//            if (attachButton.isSelected()) {
            methodComboBox.setEnabled(false);
            attachButton.setText("Detach");
            attachButton.setSelected(true);
            attachButton.setEnabled(true);
            warningLabel.setText("GCP geo-coding attached");
            warningLabel.setForeground(Color.BLACK);
//            }
//            else {
//                methodComboBox.setEnabled(true);
//                attachButton.setText("Attach");
//                updateAttachButtonAndWarningText();
//            }
        } else {
            methodComboBox.setEnabled(true);
            methodTextField.setText("Not available");
            rmseLatTextField.setText(rmseNumberFormat.format(Double.NaN));
            rmseLonTextField.setText(rmseNumberFormat.format(Double.NaN));
            attachButton.setText("Attach");
            attachButton.setSelected(false);
            updateAttachButtonAndStatus();
        }
    }

    private void updateAttachButtonAndStatus() {
        final GcpGeoCoding.Method method = (GcpGeoCoding.Method) methodComboBox.getSelectedItem();
        if (currentProduct != null && currentProduct.getGcpGroup().getNodeCount() >= method.getTermCountP()) {
            attachButton.setEnabled(true);
            warningLabel.setText("OK, enough GCP's for selected method");
            warningLabel.setForeground(Color.GREEN.darker());
        } else {
            attachButton.setEnabled(false);
            warningLabel.setText("Not enough GCP's for selected method");
            warningLabel.setForeground(Color.RED.darker());
        }
    }

    private void detachGeoCoding(Product product) {
        if (product.getGeoCoding() instanceof GcpGeoCoding) {
            product.getGeoCoding().dispose();
            product.setGeoCoding(geoCodingMap.get(product));
        }
        updateUIState();
    }

    private void attachGeoCoding(final Product product) {
        final GcpGeoCoding.Method method = (GcpGeoCoding.Method) methodComboBox.getSelectedItem();
        final ProductNodeGroup<Pin> gcpGroup = product.getGcpGroup();
        final Pin[] gcps = gcpGroup.toArray(new Pin[0]);
        final GeoCoding geoCoding = product.getGeoCoding();

        SwingWorker sw = new SwingWorker<GcpGeoCoding, GcpGeoCoding>() {
            protected GcpGeoCoding doInBackground() throws Exception {
                return new GcpGeoCoding(method, gcps,
                                        product.getSceneRasterWidth(),
                                        product.getSceneRasterHeight(),
                                        geoCoding.getDatum());
            }

            @Override
            protected void done() {
                final GcpGeoCoding gcpGeoCoding;
                try {
                    gcpGeoCoding = get();
                    product.setGeoCoding(gcpGeoCoding);
                    updateUIState();
                } catch (InterruptedException e) {
                    // ignore
                } catch (ExecutionException e) {
                    // ignore
                }
            }
        };
        sw.execute();
    }

    public void setProduct(Product product) {
        if (product == currentProduct) {
            return;
        }
        currentProduct = product;
        if (!geoCodingMap.containsKey(product) && !(product.getGeoCoding() instanceof GcpGeoCoding)) {
            geoCodingMap.put(product, product.getGeoCoding());
        }
    }

    private static class RmseNumberFormat extends NumberFormat {

        DecimalFormat format = new DecimalFormat("0.0####");

        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            if (Double.isNaN(number)) {
                return toAppendTo.append("Not available");
            } else {
                return format.format(number, toAppendTo, pos);
            }
        }

        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            return format.format(number, toAppendTo, pos);
        }

        public Number parse(String source, ParsePosition parsePosition) {
            return format.parse(source, parsePosition);
        }
    }

}
