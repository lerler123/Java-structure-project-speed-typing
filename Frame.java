import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;


@SuppressWarnings("serial")
public class Frame extends JFrame implements ActionListener, KeyListener {
    String passage = ""; //Passage we get
    String typedPass = ""; //Passage the user types
    String message = ""; //Message to display at the end of the TypingTest

    int typed = 0; //typed stores till which character the user has typed
    int count = 0;
    int WPM;

    double start;
    double end;
    double elapsed;
    double seconds;

    boolean running; //If the person is typing
    boolean ended; //Whether the typing test has ended or not

    final int SCREEN_WIDTH;
    final int SCREEN_HEIGHT;
    final int DELAY = 100;

    JButton button;
    Timer timer;
    JLabel label;

    private String username;
    private UserScoreTable userScoreTable = new UserScoreTable();

    private JButton viewScoresButton;
    private void promptForUsername() {
        username = JOptionPane.showInputDialog(this, "Enter your username:", "Username Entry", JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            // Handle case where the user canceled the input or entered an empty username
            username = "Anonymous"; // Set a default username
        }
    }

    public Frame() {
        viewScoresButton = new JButton("View Scores");
        viewScoresButton.setFont(new Font("MV Boli", Font.BOLD, 20));
        viewScoresButton.setForeground(Color.RED);
        viewScoresButton.setVisible(true);
        viewScoresButton.addActionListener(this);
        viewScoresButton.setFocusable(false);

        // Add the viewScoresButton to the frame
        this.add(viewScoresButton, BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SCREEN_WIDTH = 720;
        SCREEN_HEIGHT = 400;
        this.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        this.setVisible(true);
        this.setLocationRelativeTo(null);

        button = new JButton("Start");
        button.setFont(new Font("MV Boli", Font.BOLD, 30));
        button.setForeground(Color.BLUE);
        button.setVisible(true);
        button.addActionListener(this);
        button.setFocusable(false);

        label = new JLabel();
        label.setText("Click the Start Button");
        label.setFont(new Font("MV Boli", Font.BOLD, 30));
        label.setVisible(true);
        label.setOpaque(true);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBackground(Color.lightGray);

        this.add(button, BorderLayout.SOUTH);
        this.add(label, BorderLayout.NORTH);
        this.getContentPane().setBackground(Color.WHITE);
        this.addKeyListener(this);
        this.setFocusable(true);
        this.setResizable(false);
        this.setTitle("Typing Test");
        this.revalidate();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.setFont(new Font("MV Boli", Font.BOLD, 25));

        if (running) {
            //This will put our passage on the screen
            if (passage.length() > 1) {
                g.drawString(passage.substring(0, 50), g.getFont().getSize(), g.getFont().getSize() * 5);
                g.drawString(passage.substring(50, 100), g.getFont().getSize(), g.getFont().getSize() * 7);
                g.drawString(passage.substring(100, 150), g.getFont().getSize(), g.getFont().getSize() * 9);
                g.drawString(passage.substring(150, 200), g.getFont().getSize(), g.getFont().getSize() * 11);
            }

            //Displaying correctly typed passage in GREEN
            g.setColor(Color.GREEN);
            if (typedPass.length() > 0) {
                //This is if the typedPassages length is greater than 0 and less than 50
                if (typed < 50) //if the typed index is in the first line
                    g.drawString(typedPass.substring(0, typed), g.getFont().getSize(), g.getFont().getSize() * 5); //From the first letter to the currently typed one in green
                else
                    g.drawString(typedPass.substring(0, 50), g.getFont().getSize(), g.getFont().getSize() * 5); //If the typed character exceeds 50 we can directly show the whole line in green
            }
            if (typedPass.length() > 50) {
                if (typed < 100)
                    g.drawString(typedPass.substring(50, typed), g.getFont().getSize(), g.getFont().getSize() * 7);
                else
                    g.drawString(typedPass.substring(50, 100), g.getFont().getSize(), g.getFont().getSize() * 7);
            }
            if (typedPass.length() > 100) {
                if (typed < 150)
                    g.drawString(typedPass.substring(100, typed), g.getFont().getSize(), g.getFont().getSize() * 9);
                else
                    g.drawString(typedPass.substring(100, 150), g.getFont().getSize(), g.getFont().getSize() * 9);
            }
            if (typedPass.length() > 150)
                g.drawString(typedPass.substring(150, typed), g.getFont().getSize(), g.getFont().getSize() * 11);
            running = false; //Once we have made the line green we can make running false so that it does not keep repainting
            //Since when we type again running will become true and it will make the substring from the start of line to next character green
        }
        if (ended) {
            if (WPM <= 40)
                message = "You are an Average Typist";
            else if (WPM > 40 && WPM <= 60)
                message = "You are a Good Typist";
            else if (WPM > 60 && WPM <= 100)
                message = "You are an Excellent Typist";
            else
                message = "You are an Elite Typist";

            FontMetrics metrics = getFontMetrics(g.getFont());
            g.setColor(Color.BLUE);
            g.drawString("Typing Test Completed!", (SCREEN_WIDTH - metrics.stringWidth("Typing Test Completed!")) / 2, g.getFont().getSize() * 6);

            g.setColor(Color.BLACK);
            g.drawString("Typing Speed: " + WPM + " Words Per Minute", (SCREEN_WIDTH - metrics.stringWidth("Typing Speed: " + WPM + " Words Per Minute")) / 2, g.getFont().getSize() * 9);
            g.drawString(message, (SCREEN_WIDTH - metrics.stringWidth(message)) / 2, g.getFont().getSize() * 11);

            timer.stop();
            ended = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) //keyTyped uses the key Character which can identify capital and lowercase difference in keyPressed it takes unicode so it also considers shift which creates a problem
    {
        if (passage.length() > 1) {
            if (count == 0)
                start = LocalTime.now().toNanoOfDay();
            else if (count == 200) //Once all 200 characters are typed we will end the time and calculate time elapsed
            {
                end = LocalTime.now().toNanoOfDay();
                elapsed = end - start;
                seconds = elapsed / 1000000000.0; //nano/1000000000.0 is seconds
                WPM = (int) (((200.0 / 5) / seconds) * 60); //number of character by 5 is one word by seconds is words per second * 60 WPM
                ended = true;
                running = false;
                count++;
                userScoreTable.addUserScore(username, WPM);
//
            }
            char[] pass = passage.toCharArray();
            if (typed < 200) {
                running = true;
                if (e.getKeyChar() == pass[typed]) {
                    typedPass = typedPass + pass[typed]; //To the typed Passage we are adding what is currently typed
                    typed++;
                    count++;

                }
                //If the typed character is not equal to the - position it will not add it to the typedPassage, so the user needs to type the right thing
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            promptForUsername();
            passage = TypingTestUtil.getPassage();
            timer = new Timer(DELAY, this);
            timer.start();
            running = true;
            ended = false;

            typedPass = "";
            message = "";

            typed = 0;
            count = 0;
        }

//        else if (e.getSource() == viewScoresButton) {
//            userScoreTable.displayUserScores(username);
//        }
        if (running)
            repaint();
        if (ended) {
            userScoreTable.addUserScore(username, WPM);
            userScoreTable.saveScoresToFile("savetext.txt");
            repaint();
        }
    }


}
