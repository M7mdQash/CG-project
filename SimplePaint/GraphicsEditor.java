
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Graphics Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        createMenuBar(frame);
        createDrawingPanel(frame);
        createColorPalette(frame);
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

    private static void createColorPalette(JFrame frame) {
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
        drawingToolsPanel.setLayout(new FlowLayout());

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

    private static void performAction(String toolName) {
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
            default:
                break;
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


    static class DrawingPanel extends JPanel {
        private Tool currentTool = Tool.PEN;
        private int lastX, lastY;

        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    lastX = e.getX();
                    lastY = e.getY();
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

        enum Tool {
            PEN, ERASER, SHAPES, FILL, TEXT, UNDO, REDO
        }
    }
}