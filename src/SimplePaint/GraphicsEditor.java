import javax.imageio.ImageIO;
import javax.swing.*;
import javax.tools.Tool;

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
    private static Stack<BufferedImage> undoStack = new Stack<>();
    private static Stack<BufferedImage> redoStack = new Stack<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        // Create the main frame
        JFrame frame = new JFrame("Graphics Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        frame.setJMenuBar(menuBar);

        // Create the canvas area
        drawingPanel = new DrawingPanel();
        drawingPanel.setBackground(Color.WHITE);
        frame.add(drawingPanel, BorderLayout.CENTER);

        // Create the drawing tools panel
        JPanel drawingToolsPanel = new JPanel();
        drawingToolsPanel.setLayout(new FlowLayout());

        // Add drawing tools buttons
        addToolButton(drawingToolsPanel, "Pen", "pen.png", Tool.PEN);
        addToolButton(drawingToolsPanel, "Eraser", "eraser.png", Tool.ERASER);
        addToolButton(drawingToolsPanel, "Shapes", "shape.png", Tool.SHAPES);
        addToolButton(drawingToolsPanel, "Fill", "color.png", Tool.FILL);
        addToolButton(drawingToolsPanel, "Text", "text.png", Tool.TEXT);
        addToolButton(drawingToolsPanel, "Undo", "undo.png", Tool.UNDO);
        addToolButton(drawingToolsPanel, "Redo", "redo.png", Tool.REDO);

        frame.add(drawingToolsPanel, BorderLayout.WEST);

        // Create the color palette panel
        JPanel colorPalettePanel = new JPanel();
        colorPalettePanel.setLayout(new FlowLayout());

        // Add color selection components
        JButton colorButton = new JButton("Choose Color");
        colorPalettePanel.add(colorButton);

        // Create a text field to display the selected color
        JTextField colorTextField = new JTextField(10);
        colorPalettePanel.add(colorTextField);

        // Create the size selector
        JSlider sizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, currentSize);
        sizeSlider.setMajorTickSpacing(1);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setSnapToTicks(true);

        sizeSlider.addChangeListener(e -> {
            currentSize = sizeSlider.getValue();
        });

        colorPalettePanel.add(sizeSlider);        

        // Action listener for the color button
        colorButton.addActionListener(e -> {
            // Show the color chooser dialog
            Color selectedColor = JColorChooser.showDialog(frame, "Choose a Color", currentColor);
            if (selectedColor != null) {
                // Update the current color and text field
                currentColor = selectedColor;
                colorTextField.setBackground(currentColor);
            }
        });

        frame.add(colorPalettePanel, BorderLayout.NORTH);

        // Create the transformations and selection tools panel
        JPanel toolsPanel = new JPanel();
        toolsPanel.setLayout(new GridLayout(2, 1));
        JPanel transformationsPanel = new JPanel();
        JPanel selectionToolsPanel = new JPanel();
        // Add transformation and selection tool components here
        toolsPanel.add(transformationsPanel);
        toolsPanel.add(selectionToolsPanel);
        frame.add(toolsPanel, BorderLayout.EAST);

        // Create the animation panel
        JPanel animationPanel = new JPanel();
        JButton createAnimationButton = new JButton("Create Animation Path");
        animationPanel.add(createAnimationButton);
        frame.add(animationPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void addToolButton(JPanel panel, String toolName, String iconFileName, Tool tool) {
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
                drawingPanel.setCurrentTool(tool);
            });
            panel.add(toolButton);
        }
    }
    
    static class DrawingPanel extends JPanel {
        private Tool currentTool = Tool.PEN;
        private int lastX, lastY;

        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    lastX = e.getX();
                    lastY = e.getY();
                    if (currentTool == Tool.PEN || currentTool == Tool.ERASER) {
                        history.add(copyImage());
                        historyPointer++;
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    Graphics2D g = (Graphics2D) getGraphics();
                    g.setColor(currentColor);
                    if (currentTool == Tool.PEN) {
                        g.drawLine(lastX, lastY, x, y);
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

        public void setCurrentTool(Tool tool) {
            currentTool = tool;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!history.isEmpty()) {
                g.drawImage(history.get(historyPointer), 0, 0, this);
            }
        }

        private BufferedImage copyImage() {
            int width = getWidth();
            int height = getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            paint(g);
            g.dispose();
            return image;
        }
    }

    enum Tool {
        PEN, ERASER, SHAPES, FILL, TEXT, UNDO, REDO
    }
}