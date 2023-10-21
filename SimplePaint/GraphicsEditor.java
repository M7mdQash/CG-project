
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GraphicsEditor {

    private static Color currentColor = Color.BLACK; // Default color
    private static int currentSize = 2; // Default size
    private static DrawingPanel drawingPanel;
    private static List<BufferedImage> history = new ArrayList<>();
    private static int historyPointer = -1;
    private static BufferedImage currentImage;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    //this code handels saving the actions of the user to undo and redo
    private static void saveCurrentState() {
        BufferedImage image = new BufferedImage(drawingPanel.getWidth(), drawingPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        drawingPanel.paint(g2d);
        g2d.dispose();
        updateHistory(image);
    }

    private static final int MAX_HISTORY_SIZE = 50;

    private static void updateHistory(BufferedImage image) {
        // If the pointer is not at the end of the list, remove all states after the pointer.
        // This is done because when you draw something new after undoing, you can't redo to the previous states.
        while (historyPointer < history.size() - 1) {
            history.remove(history.size() - 1);
        }

        // Add the new state to the end of the list.
        history.add(image);

        // Move the pointer to the new state.
        historyPointer++;

        // Optional: If history gets too big, remove the earliest state to save memory.
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
            historyPointer--;  // adjust the pointer due to removal at the start.
        }
    }


    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Graphics Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 760);
        frame.setLayout(new BorderLayout());

        createMenuBar(frame);
        createDrawingPanel(frame);
        createColorPalette(frame);
        createShapesPanel();
        createDrawingToolsPanel(frame);
        createAnimationPanel(frame);

        frame.setVisible(true);
    }

    private static void createMenuBar(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        frame.setJMenuBar(menuBar);
    }

    private static void createDrawingPanel(JFrame frame) {
        drawingPanel = new DrawingPanel();
        drawingPanel.setBackground(Color.WHITE);


        frame.add(drawingPanel, BorderLayout.CENTER);
    }

    /**===========
     * this area is for the shapes dialouge
     * ===========
     * */
    private static void showShapesPanel() {
        JPanel panel = createShapesPanel();
        if (panel == null) {
            panel = createShapesPanel();
        }
        JOptionPane.showMessageDialog(null, panel, "Shapes", JOptionPane.PLAIN_MESSAGE);
    }

    private static JPanel createShapesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JLabel shapeLabel = new JLabel("Select Shape:");
        JComboBox<String> shapeComboBox = new JComboBox<>(new String[]{"Rectangle", "Circle", "Triangle", "Polygon", "Line"});
        panel.add(shapeLabel);
        panel.add(shapeComboBox);

        JLabel xLabel = new JLabel("X Coordinate:");
        JTextField xTextField = new JTextField(5);
        panel.add(xLabel);
        panel.add(xTextField);

        JLabel yLabel = new JLabel("Y Coordinate:");
        JTextField yTextField = new JTextField(5);
        panel.add(yLabel);
        panel.add(yTextField);

        JLabel sizeLabel = new JLabel("Size:");
        JTextField sizeTextField = new JTextField(5);
        panel.add(sizeLabel);
        panel.add(sizeTextField);

        JButton drawButton = new JButton("Draw");
        panel.add(drawButton);

        JRadioButton fillButton = new JRadioButton("Fill");
        panel.add(fillButton);


        drawButton.addActionListener(e -> {

            String selectedShape = shapeComboBox.getSelectedItem().toString();

            int x = Integer.parseInt(xTextField.getText());
            int y = Integer.parseInt(yTextField.getText());
            int size = Integer.parseInt(sizeTextField.getText());
            boolean fill = fillButton.isSelected();
            drawShape(selectedShape, x, y, size,fill);
        });

        return panel;
    }


    //this method creates shapes in the image
    private static void drawShape(String shape, int x, int y, int size, boolean fill) {
        Graphics2D g = (Graphics2D) drawingPanel.getGraphics();
        g.setColor(currentColor);
        g.setStroke(new BasicStroke(currentSize));

        if (shape.equals("Rectangle")) {
            if (fill) g.fillRect(x, y, size, size); // Fill rectangle
            else g.drawRect(x, y, size, size);
            saveCurrentState();
        } else if (shape.equals("Circle")) {
            if (fill) g.fillOval(x, y, size, size); // Fill circle
            else g.drawOval(x, y, size, size);
            saveCurrentState();
        } else if (shape.equals("Triangle")) {
            int[] xPoints = {x, x + size / 2, x + size};
            int[] yPoints = {y + size, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill triangle
            else g.drawPolygon(xPoints, yPoints, 3);
            saveCurrentState();
        } else if (shape.equals("Polygon")) {
            int[] xPoints = {x, x + size, x + size / 2};
            int[] yPoints = {y, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill polygon
            else g.drawPolygon(xPoints, yPoints, 3);
            saveCurrentState();
        } else if (shape.equals("Line")) {
            g.drawLine(x, y, x + size, y + size);
            saveCurrentState();
        }

    }

    private static void createColorPalette(JFrame frame) {
        /**======================
         * this here creates the color choosing panel
         * using making an entire new panel
         * */
        JPanel colorPalettePanel = new JPanel();
        colorPalettePanel.setLayout(new FlowLayout());

        JButton colorButton = new JButton("Choose Color");
        colorPalettePanel.add(colorButton);

        JTextField colorTextField = new JTextField(10);
        colorPalettePanel.add(colorTextField);

        JSlider sizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, currentSize);
        sizeSlider.setMajorTickSpacing(1);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setSnapToTicks(true);

        sizeSlider.addChangeListener(e -> {
            currentSize = sizeSlider.getValue();
        });

        colorPalettePanel.add(sizeSlider);

        colorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(frame, "Choose a Color", currentColor);
            if (selectedColor != null) {
                currentColor = selectedColor;
                colorTextField.setBackground(currentColor);
            }
        });

        frame.add(colorPalettePanel, BorderLayout.NORTH);
    }

    private static void createDrawingToolsPanel(JFrame frame) {
        JPanel drawingToolsPanel = new JPanel();
        drawingToolsPanel.setLayout(new GridLayout());
        drawingToolsPanel.setLayout(new GridLayout(8,0));
        //using grid layout makes it verticle

        /**===========
         *
         * buttons adding section
         *
         * ===========
         * */
        addToolButton(drawingToolsPanel, "Pen", "pen.png");
        addToolButton(drawingToolsPanel, "Eraser", "eraser.png");
        addToolButton(drawingToolsPanel, "Shapes", "shape.png");
        addToolButton(drawingToolsPanel, "Fill", "color.png");
        addToolButton(drawingToolsPanel, "Text", "text.png");
        addToolButton(drawingToolsPanel, "Undo", "undo.png");
        addToolButton(drawingToolsPanel, "Redo", "redo.png");
        addToolButton(drawingToolsPanel, "Clear", "bin.png");
        frame.add(drawingToolsPanel, BorderLayout.WEST);
    }

    private static void addToolButton(JPanel panel, String toolName, String iconFileName) {
        BufferedImage iconImage = null;
        try {
            iconImage = ImageIO.read(GraphicsEditor.class.getResource("/icons/" + iconFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (iconImage != null) {
            int iconWidth = 32;
            int iconHeight = 32;
            Image scaledIconImage = iconImage.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledIconImage);
            JButton toolButton = new JButton("", scaledIcon);

            toolButton.addActionListener(e -> {
                // Perform the action associated with the selected tool
                performAction(toolName);
            });

            panel.add(toolButton);
        }
    }

    private static void performAction(String toolName) {//handels the choice of users
        // Implement the actions associated with each tool here
        switch (toolName) {
            case "Pen":
                drawingPanel.setCurrentTool(DrawingPanel.Tool.PEN);
                break;
            case "Eraser":
                drawingPanel.setCurrentTool(DrawingPanel.Tool.ERASER);
                break;
            case "Clear":
                clearDrawingPanel();
                break;
            case "Text":
                openTextDialog(); // bug: one text apears and only one that moves on last press and afther writing
                break;
            case "Shapes":
                showShapesPanel();
                break;
            case "Fill":
                break;
            case "Undo":
                if (historyPointer > 0) {
                    historyPointer--;
                    currentImage = history.get(historyPointer);
                    drawingPanel.repaint();
                }
                break;
            case "Redo":
                if (historyPointer < history.size() - 1) {
                    historyPointer++;
                    currentImage = history.get(historyPointer);
                    drawingPanel.repaint();
                }
                break;

            default:
                break;
        }
    }


    //=========================this method opens the text dialouge to add text on the image
    private static void openTextDialog() {
        // Create an instance of the TextDialog class
        TextDialog textDialog = new TextDialog((Frame) SwingUtilities.getWindowAncestor(drawingPanel));

        // Show the text dialog and get the user's response
        int userResponse = textDialog.showCustomDialog((Frame) SwingUtilities.getWindowAncestor(drawingPanel));

        // If the user pressed "Apply," update the text properties
        if (userResponse == TextDialog.APPLY_OPTION) {
            Font selectedFont = textDialog.getFont();
            String selectedText = textDialog.getText();
            drawingPanel.setTextProperties(selectedFont, selectedText);
            saveCurrentState();
        }

    }
    private static void createAnimationPanel(JFrame frame) {
        JPanel animationPanel = new JPanel();
        JButton createAnimationButton = new JButton("Create Animation Path");
        animationPanel.add(createAnimationButton);
        frame.add(animationPanel, BorderLayout.SOUTH);
    }
    private static void clearDrawingPanel() {
        // Clear the drawing canvas by filling it with the background color
        Graphics2D g = (Graphics2D) drawingPanel.getGraphics();
        g.setColor(drawingPanel.getBackground());
        g.fillRect(0, 0, drawingPanel.getWidth(), drawingPanel.getHeight());

        // Clear the history
        history.clear();
        historyPointer = -1;

    }
    //method for undo and redo


    static class DrawingPanel extends JPanel {
        public static Tool currentTool = Tool.PEN;
        private int lastX, lastY;

        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    lastX = e.getX();
                    lastY = e.getY();

                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    saveCurrentState();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    Graphics2D g = (Graphics2D) getGraphics();
                    g.setColor(currentColor);
                    g.setStroke(new BasicStroke(currentSize));

                    //========this code snippet is the code for the pen========
                    if (currentTool == Tool.PEN) {
                        g.drawLine(lastX, lastY, x, y);

                    //========and this is the code for the eraser tool=========
                    } else if (currentTool == Tool.ERASER) {
                        g.setColor(getBackground());
                        g.setStroke(new BasicStroke(5.0f));
                        g.drawLine(lastX, lastY, x, y);
                    }
                    lastX = x;
                    lastY = y;

                }


            });
        }

        //methods for text editor
        private Font textFont = new Font("Arial", Font.PLAIN, 20);
        private String text = "Sample Text";

        public void setTextProperties(Font font, String text) {
            this.textFont = font;
            this.text = text;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw the text using the selected properties
            g.setFont(textFont);
            g.setColor(currentColor);
            g.drawString(text,lastX , lastY);
            if (historyPointer >= 0) {
                BufferedImage image = history.get(historyPointer);
                g.drawImage(image, 0, 0, this);
            }
        }

        //==============end for methods of text editor

        public void setCurrentTool(Tool tool) {
            currentTool = tool;
        }

        enum Tool {
            PEN, ERASER, SHAPES, FILL, TEXT, UNDO, REDO
        }
    }
}