import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;

public class Main implements ActionListener, MouseWheelListener {
    private JFrame frame;
    private ArtPanel renderPane;
    private JPanel menuPane;
    private JPanel mainPane;
    private JButton openFile, autoButton;
    private File file;
    private Camera camera;
    private int zoom = 100;
    private int degreesX = 0, degreesY = 0, degreesZ = 0;
    private Timer timer;
    private boolean auto = false;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        // Up
        if (notches < 0) {
            zoom += -1 * notches;
        } else {
            if (zoom - notches < 10) {
                zoom = 10;
            } else {
                zoom -= notches;
            }
        }
        this.renderPane.repaint();
    }

    class ArtPanel extends JPanel {
        final static int WIDTH = 600;
        final static int HEIGHT = 600;
        ArrayList<Triangle> triangles;
        ArrayList<Color> colors;

        public ArtPanel() {
            triangles = new ArrayList<Triangle>();
            colors = new ArrayList<Color>();
            // this.setIgnoreRepaint(true);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            camera.setZoom(zoom);
            BufferedImage screen = new BufferedImage(WIDTH, HEIGHT,
                    BufferedImage.TYPE_INT_RGB);
            double[][] zBuffer = new double[HEIGHT][WIDTH];
            for (int i = 0; i < zBuffer.length; i++) {
                for (int j = 0; j < zBuffer[i].length; j++) {
                    screen.setRGB(i, j, Color.WHITE.getRGB());
                    zBuffer[i][j] = Double.MAX_VALUE;
                }
            }
            for (int i = 0; i < triangles.size(); i++) {
                Triangle convertedTriangle = camera.convert(triangles.get(i));
                Matrix rotationMatrix = Camera.
                        getRotationMatrix(degreesX, degreesY, degreesZ);
                convertedTriangle.getVertex1().leftMultiply(rotationMatrix);
                convertedTriangle.getVertex2().leftMultiply(rotationMatrix);
                convertedTriangle.getVertex3().leftMultiply(rotationMatrix);
                // Moves (0,0) from top left to middle of screen
                convertedTriangle.getVertex1().increment(HEIGHT / 2);
                convertedTriangle.getVertex2().increment(HEIGHT / 2);
                convertedTriangle.getVertex3().increment(HEIGHT / 2);
                screen = convertedTriangle.project(zBuffer, screen,
                        colors.get(i));
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(screen, 0, 0, this);
        }

        public void addTriangle(Triangle triangle) {
            triangles.add(triangle);
            colors.add(new Color((float) Math.random(),
                    (float) Math.random(), (float) Math.random()));
        }

        public void clear() {
            triangles = new ArrayList<Triangle>();
            colors = new ArrayList<Color>();
        }
    }

    public Main() {
        camera = new Camera(ArtPanel.WIDTH, ArtPanel.HEIGHT);
        frame = new JFrame("LiquidWorks");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        renderPane = new ArtPanel();
        renderPane.setPreferredSize(new Dimension(ArtPanel.WIDTH,
                ArtPanel.HEIGHT));
        renderPane.addMouseWheelListener(this);

        menuPane = new JPanel();
        openFile = new JButton("Load ASCII .STL file");
        openFile.addActionListener(this);
        menuPane.add(openFile);

        autoButton = new JButton("Enable Auto-rotate");
        autoButton.addActionListener(this);
        menuPane.add(autoButton);

        mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
        mainPane.add(menuPane);
        mainPane.add(renderPane);

        initControls();

        frame.setContentPane(mainPane);
        frame.pack();
        frame.setVisible(true);
    }

    private void initControls() {
        String up = "up";
        renderPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0,
                        false), up);
        renderPane.getActionMap().put(up, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                degreesX += 10;
                degreesX %= 360;
                renderPane.repaint();
            }
        });

        String down = "down";
        renderPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0,
                        false), down);
        renderPane.getActionMap().put(down, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                degreesX -= 10;
                if (degreesX < 0) {
                    degreesX += 360;
                }
                ;
                renderPane.repaint();
            }
        });

        String right = "right";
        renderPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0,
                        false), right);
        renderPane.getActionMap().put(right, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                degreesY += 10;
                degreesY %= 360;
                renderPane.repaint();
            }
        });

        String left = "left";
        renderPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0,
                        false), left);
        renderPane.getActionMap().put(left, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                degreesY -= 10;
                if (degreesY < 0) {
                    degreesY += 360;
                }
                renderPane.repaint();
            }
        });

        String z = "z";
        renderPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0,
                        false), z);
        renderPane.getActionMap().put(z, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                degreesZ += 10;
                degreesZ %= 360;
                renderPane.repaint();
            }
        });

        String x = "x";
        renderPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0,
                        false), x);
        renderPane.getActionMap().put(x, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                degreesZ -= 10;
                if (degreesZ < 0) {
                    degreesZ += 360;
                }
                renderPane.repaint();
            }
        });
    }

    private void processFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.trim().split("\\s+");
                if (split[0].equals("vertex")) {
                    Vector3D vertex1 =
                            new Vector3D(Double.parseDouble(split[1]),
                            Double.parseDouble(split[2]),
                                    Double.parseDouble(split[3]));
                    line = br.readLine().trim();
                    split = line.split("\\s+");
                    Vector3D vertex2 =
                            new Vector3D(Double.parseDouble(split[1]),
                            Double.parseDouble(split[2]),
                                    Double.parseDouble(split[3]));
                    line = br.readLine().trim();
                    split = line.split("\\s+");
                    Vector3D vertex3 =
                            new Vector3D(Double.parseDouble(split[1]),
                            Double.parseDouble(split[2]),
                                    Double.parseDouble(split[3]));
                    renderPane.addTriangle(
                            new Triangle(vertex1, vertex2, vertex3));
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(frame,
                    "File not found. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame,
                    "Error loading file. Ensure" +
                            " that the STL file is valid.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (auto) {
            degreesY++;
            this.renderPane.repaint();
        }
        if (event.getSource() == autoButton) {
            if (auto) {
                autoButton.setText("Enable Auto-rotate");
                auto = false;
                timer.stop();
            } else {
                autoButton.setText("Disable Auto-rotate");
                auto = true;
                degreesZ = 210;
                degreesY = 45;
                degreesX = 0;
                timer = new Timer(25, this);
                timer.start();
            }
        }
        if (event.getSource() == openFile) {
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(renderPane);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                String line;
                String[] split = null;
                try {
                    BufferedReader br =
                            new BufferedReader(new FileReader(file));
                    line = br.readLine();
                    split = line.trim().split("\\s+");
                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(frame,
                            "File not found. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame,
                            "Error. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                // Checks to ensure valid file format
                if (!file.getName().endsWith(".STL") ||
                        !split[0].equals("solid")) {
                    JOptionPane.showMessageDialog(frame,
                            "File loaded must be " +
                                    ".STL file in ASCII format.",
                            "Invalid file",
                            JOptionPane.ERROR_MESSAGE);
                    file = null;
                }
            }
            if (file != null) {
                renderPane.clear();
                processFile();
            }
        }
    }

    public static void runGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        new Main();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                runGUI();
            }
        });
    }
}


