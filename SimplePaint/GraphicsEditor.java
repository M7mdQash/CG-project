
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.awt.geom.AffineTransform;

public class GraphicsEditor {

    private static Vector vector = null;

    private static JPanel canvas;
    private static Color currentColor = Color.BLACK; // Default color
    private static int currentSize = 2; // Default size
    private static DrawingPanel drawingPanel;
    private static List<BufferedImage> history = new ArrayList<>();
    private static int historyPointer = -1;
    private static BufferedImage currentImage;

    //======variable for transformations
    private static int shapeX = 100;
    private static int shapeY = 100;
    private static int shapeSize = 50;
    private static int rotationAngle = 0; // in radians
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
        frame.setResizable(false);
        createMenuBar(frame);
        createDrawingPanel(frame);
        createColorPalette(frame);
        createShapesPanel();
        createDrawingToolsPanel(frame);
        createAnimationPanel(frame);

        frame.setVisible(true);
    }
    //==============
    //code for saving images
    //==============
    private static void createMenuBar(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");

        // New Option
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(e -> {
            clearDrawingPanel();
        });
        fileMenu.add(newMenuItem);

        // Save Option
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // Implement your save logic here, using the selected 'file' object.
                // You can save your drawing data to the chosen file.
            }
        });
        fileMenu.add(saveMenuItem);

        // Open Option
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // Implement your open logic here, using the selected 'file' object.
                // You can load the drawing data from the chosen file.
            }
        });
        fileMenu.add(openMenuItem);

        menuBar.add(fileMenu);

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

    //code from faris


    //code from faris
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
        JLabel angleLabel = new JLabel("angle:");
        JTextField angleTextField = new JTextField(3);
        panel.add(angleLabel);
        panel.add(angleTextField);


// ================== This part for transformation ===============================



        JButton moveButton = new JButton("Move");
        JButton scaleButton = new JButton("Scale");
        JButton rotateButton = new JButton("Rotate");
        JButton flipButton = new JButton("Flip");


        panel.add(moveButton);
        panel.add(scaleButton);
        panel.add(rotateButton);
        panel.add(flipButton);


        //==================
        //add action to pressing the draw/move/scale etc buttons
        //==================
        moveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedShape = shapeComboBox.getSelectedItem().toString();
                int size = Integer.parseInt(sizeTextField.getText());

                int x = Integer.parseInt(xTextField.getText());
                int y = Integer.parseInt(yTextField.getText());
                boolean fill = fillButton.isSelected();


                moveShape(selectedShape,x,y,size,fill);
            }
        });

        //--------------
        //scaling button
        //--------------
        scaleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedShape = shapeComboBox.getSelectedItem().toString();
                int x = Integer.parseInt(xTextField.getText());
                int y = Integer.parseInt(yTextField.getText());
                int size = Integer.parseInt(sizeTextField.getText());
                boolean fill = fillButton.isSelected();

                scaleShape(selectedShape,x,y,size,fill);
            }
        });


        //-------
        //rotate button
        //-------
        rotateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedShape = shapeComboBox.getSelectedItem().toString();
                int x = Integer.parseInt(xTextField.getText());
                int y = Integer.parseInt(yTextField.getText());
                double angle =Double.parseDouble(angleTextField.getText());
                int size = Integer.parseInt(sizeTextField.getText());
                boolean fill = fillButton.isSelected();

                rotateShape(selectedShape,x,y,size,fill);

            }
        });


        //======
        //flip button
        //======
        flipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedShape = shapeComboBox.getSelectedItem().toString();
                int x = Integer.parseInt(xTextField.getText());
                int y = Integer.parseInt(yTextField.getText());
                int size = Integer.parseInt(sizeTextField.getText());
                boolean fill = fillButton.isSelected();

                fliper(selectedShape,x,y,size,fill);
            }
        });


        //==============
        //normal button
        //==============
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


//    public static void moveShape(String shape,int deltaX, int deltaY,int size,int x ,int y,boolean fill) {
//        AffineTransform moving = new AffineTransform();
//        moving.translate(deltaX,deltaY);
//
//        drawShape( shape,deltaX, deltaY, size,fill);    }

//    public static void scaleShape(String shape,double scaleFactor,int x,int y,int size,boolean fill) {
//        size = (int) (size * scaleFactor);
//
//        drawShape( shape,x, y, size,fill);
//    }
private static void scaleShape(String shape, int x, int y, int size, boolean fill) {
    int moX = 10 ,  moY = 10;
    Graphics2D g = (Graphics2D) drawingPanel.getGraphics();
    g.setColor(currentColor);
    g.setStroke(new BasicStroke(currentSize));
    //the scaling is set to 2 for the X and 1.2 for the Y
    AffineTransform mover = new AffineTransform();
    mover.scale(2,2);

    g.setTransform(mover);

    if (shape.equals("Rectangle")) {
        if (fill) g.fillRect(x, y, size, size); // Fill rectangle
        else g.drawRect(x, y, size, size);
    } else if (shape.equals("Circle")) {
        if (fill) g.fillOval(x, y, size, size); // Fill circle
        else g.drawOval(x, y, size, size);
    } else if (shape.equals("Triangle")) {
        int[] xPoints = {x, x + size / 2, x + size};
        int[] yPoints = {y + size, y, y + size};
        if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill triangle
        else g.drawPolygon(xPoints, yPoints, 3);

    } else if (shape.equals("Polygon")) {
        int[] xPoints = {x, x + size, x + size / 2};
        int[] yPoints = {y, y, y + size};
        if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill polygon
        else g.drawPolygon(xPoints, yPoints, 3);
    } else if (shape.equals("Line")) {
        g.drawLine(x, y, x + size, y + size);
    }

}
    private static void rotateShape(String shape, int x, int y, int size, boolean fill) {
        double angle = 45;
        Graphics2D g = (Graphics2D) drawingPanel.getGraphics();
        g.setColor(currentColor);
        g.setStroke(new BasicStroke(currentSize));
        //the scaling is set to 2 for the X and 1.2 for the Y
        AffineTransform mover = new AffineTransform();
        mover.setToRotation(Math.toRadians(angle));
        g.setTransform(mover);

        if (shape.equals("Rectangle")) {
            if (fill) g.fillRect(x, y, size, size); // Fill rectangle
            else g.drawRect(x, y, size, size);
        } else if (shape.equals("Circle")) {
            if (fill) g.fillOval(x, y, size, size); // Fill circle
            else g.drawOval(x, y, size, size);
        } else if (shape.equals("Triangle")) {
            int[] xPoints = {x, x + size / 2, x + size};
            int[] yPoints = {y + size, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill triangle
            else g.drawPolygon(xPoints, yPoints, 3);

        } else if (shape.equals("Polygon")) {
            int[] xPoints = {x, x + size, x + size / 2};
            int[] yPoints = {y, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill polygon
            else g.drawPolygon(xPoints, yPoints, 3);
        } else if (shape.equals("Line")) {
            g.drawLine(x, y, x + size, y + size);
        }

    }

//    public static void flipShape(String shape,int x ,int y ,int size,boolean fill) {
//        size *= -1;
//        drawShape( shape,x, y, size,fill);
//    }

    //flip button gives no output
    private static void fliper(String shape,int x,int y, int size, boolean fill){
        int moX = 10 ,  moY = 10;
        Graphics2D g = (Graphics2D) drawingPanel.getGraphics();
        g.setColor(currentColor);
        g.setStroke(new BasicStroke(currentSize));
        AffineTransform mover = new AffineTransform();
        mover.scale(1, -1);
        g.setTransform(mover);

        if (shape.equals("Rectangle")) {
            if (fill) g.fillRect(x, y, size, size); // Fill rectangle
            else g.drawRect(x, y, size, size);
        } else if (shape.equals("Circle")) {
            if (fill) g.fillOval(x, y, size, size); // Fill circle
            else g.drawOval(x, y, size, size);
        } else if (shape.equals("Triangle")) {
            int[] xPoints = {x, x + size / 2, x + size};
            int[] yPoints = {y + size, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill triangle
            else g.drawPolygon(xPoints, yPoints, 3);

        } else if (shape.equals("Polygon")) {
            int[] xPoints = {x, x + size, x + size / 2};
            int[] yPoints = {y, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill polygon
            else g.drawPolygon(xPoints, yPoints, 3);
        } else if (shape.equals("Line")) {
            g.drawLine(x, y, x + size, y + size);
        }

    }
    private static void moveShape(String shape, int x, int y, int size, boolean fill) {
        Graphics2D g = (Graphics2D) drawingPanel.getGraphics();
        g.setColor(currentColor);
        g.setStroke(new BasicStroke(currentSize));
        AffineTransform mover = new AffineTransform();
        mover.translate(size * 2,size);
        g.setTransform(mover);

        if (shape.equals("Rectangle")) {
            if (fill) g.fillRect(x, y, size, size); // Fill rectangle
            else g.drawRect(x, y, size, size);
        } else if (shape.equals("Circle")) {
            if (fill) g.fillOval(x, y, size, size); // Fill circle
            else g.drawOval(x, y, size, size);
        } else if (shape.equals("Triangle")) {
            int[] xPoints = {x, x + size / 2, x + size};
            int[] yPoints = {y + size, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill triangle
            else g.drawPolygon(xPoints, yPoints, 3);

        } else if (shape.equals("Polygon")) {
            int[] xPoints = {x, x + size, x + size / 2};
            int[] yPoints = {y, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill polygon
            else g.drawPolygon(xPoints, yPoints, 3);
        } else if (shape.equals("Line")) {
            g.drawLine(x, y, x + size, y + size);
        }

    }

        //this method creates shapes in the image
    private static void drawShape(String shape, int x, int y, int size, boolean fill) {
        Graphics2D g = (Graphics2D) drawingPanel.getGraphics();
        g.setColor(currentColor);
        g.setStroke(new BasicStroke(currentSize));

        if (shape.equals("Rectangle")) {
            if (fill) g.fillRect(x, y, size, size); // Fill rectangle
            else g.drawRect(x, y, size, size);
        } else if (shape.equals("Circle")) {
            if (fill) g.fillOval(x, y, size, size); // Fill circle
            else g.drawOval(x, y, size, size);
        } else if (shape.equals("Triangle")) {
            int[] xPoints = {x, x + size / 2, x + size};
            int[] yPoints = {y + size, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill triangle
            else g.drawPolygon(xPoints, yPoints, 3);

        } else if (shape.equals("Polygon")) {
            int[] xPoints = {x, x + size, x + size / 2};
            int[] yPoints = {y, y, y + size};
            if (fill) g.fillPolygon(xPoints, yPoints, 3); // Fill polygon
            else g.drawPolygon(xPoints, yPoints, 3);
        } else if (shape.equals("Line")) {
            g.drawLine(x, y, x + size, y + size);
        }

    }
    //-------------------------------------------------------------
    //                  end of section for shapes
    //-------------------------------------------------------------

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
        addToolButton(drawingToolsPanel, "Vector", "point-transformation.png");
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

                break;
            case "Redo":

                break;
            case "Vector":
                startVector();
                break;
            default:
                break;
        }
    }


    //=======
    //method for vectors
    //=======
    private static void startVector() {

            // Prompt the user for the destination point (x, y)
            int x = Integer.parseInt(JOptionPane.showInputDialog("Enter the X coordinate:"));
            int y = Integer.parseInt(JOptionPane.showInputDialog("Enter the Y coordinate:"));

            vector = new Vector(x, y);
            drawVector(vector.x, vector.y);

    }
    private static void drawVector(int x, int y) {
        Graphics2D g = (Graphics2D) drawingPanel.getGraphics();
        g.setColor(currentColor);
        g.setStroke(new BasicStroke(currentSize));
        g.drawLine(0, 0, x, y);
    }
    //end methods for vectors
    //========================

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
            drawingPanel.repaint();
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
    // Add a Vector object to track the vector's destination point
    static class Vector {
        int x;
        int y;

        public Vector(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}