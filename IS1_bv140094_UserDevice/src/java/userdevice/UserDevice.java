package userdevice;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;

public class UserDevice {

    @Resource(lookup = "CommandTopic")
    private static Topic topic;
    @Resource(lookup = "PlayerQueue")
    private static Queue queuePlayer;
    @Resource(lookup = "PlannerQueue")
    private static Queue queuePlanner;
    @Resource(lookup = "jms/__defaultConnectionFactory")
    private static ConnectionFactory connectionFactory;

    private final JMSContext context;
    private final JMSProducer producer;
    private final JMSConsumer consumerPlayer;
    private final JMSConsumer consumerPlanner;

    public static final long TIMEOUT = 10_000;

    private JFrame frame;

    private UserDevice() {
        context = connectionFactory.createContext();
        producer = context.createProducer();
        consumerPlayer = context.createConsumer(queuePlayer, "for = 'userDevice'");
        consumerPlanner = context.createConsumer(queuePlanner, "for = 'userDevice'");
        createFrame();
    }

    private JPanel createPlayerPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(Color.BLACK, 2));
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Player");
        panel.add(label, BorderLayout.NORTH);

        JPanel functionPanel = new JPanel();

        JTextField textField = new JTextField(50);
        functionPanel.add(textField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener((e) -> {
            try {
                TextMessage textMessage = context.createTextMessage(textField.getText());
                textMessage.setStringProperty("for", "player");
                textMessage.setStringProperty("command", "search");
                producer.send(topic, textMessage);
            } catch (JMSException ex) {
                Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        functionPanel.add(searchButton);

        JButton historyButton = new JButton("History");
        historyButton.addActionListener((e) -> {
            try {
                TextMessage textMessage = context.createTextMessage();
                textMessage.setStringProperty("for", "player");
                textMessage.setStringProperty("command", "history");
                producer.send(topic, textMessage);

                SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
                    @Override
                    protected List<String> doInBackground() throws Exception {
                        Message message = consumerPlayer.receive(UserDevice.TIMEOUT);
                        if (message instanceof ObjectMessage) {
                            ObjectMessage objectMessage = (ObjectMessage) message;
                            if (objectMessage.getStringProperty("reply").equals("history")) {
                                return (List<String>) objectMessage.getObject();
                            }
                        }
                        return Collections.<String>emptyList();
                    }

                    @Override
                    protected void done() {
                        try {
                            JOptionPane pane = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE);
                            JList list = new JList(get().toArray(new String[0]));
                            list.setLayoutOrientation(JList.VERTICAL);
                            JScrollPane scrollPane = new JScrollPane(list);
                            pane.add(scrollPane, pane.getComponentCount() - 1); // Before the button!
                            JDialog dialog = pane.createDialog(frame, "History");
                            dialog.setVisible(true);
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                worker.execute();
            } catch (JMSException ex) {
                Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        functionPanel.add(historyButton);

        panel.add(functionPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAlarmPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(Color.BLACK, 2));
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Alarm");
        panel.add(label, BorderLayout.NORTH);
        
        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new GridLayout(1, 0));
        panel.add(centralPanel, BorderLayout.CENTER);

        JPanel timePanel = new JPanel();
        centralPanel.add(timePanel);
        timePanel.setLayout(new GridLayout(0, 1));

        JSpinner spinner = new JSpinner();
        spinner.setModel(new SpinnerDateModel(new Date(), new Date(), null, Calendar.HOUR_OF_DAY));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd.MM.yyyy HH:mm:ss"));
        spinner.addChangeListener((ChangeEvent e) -> {
            try {
                if (((Date) spinner.getValue()).before(new Date())) {
//                    Calendar cal = Calendar.getInstance();
//                    cal.add(Calendar.MINUTE, 1);
//                    spinner.setValue(cal.getTime());
                    spinner.setValue(new Date());
                    return;
                }
                spinner.commitEdit();
            } catch (ParseException ex) {
                spinner.setValue(new Date());
//                    Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        JPanel timePickerPanel = new JPanel();
        ButtonGroup timeButtonGroup = new ButtonGroup();
        JRadioButton timePickerButton = new JRadioButton("", true);
        timeButtonGroup.add(timePickerButton);
        timePickerPanel.add(timePickerButton);
        timePickerPanel.add(spinner);

        timePanel.add(timePickerPanel);
        final String[] predefinedTimes = {"09:00", "13:00", "17:00", "20:00"};
        for (String time : predefinedTimes) {
            JRadioButton timeButton = new JRadioButton(time);
            timeButtonGroup.add(timeButton);
            timePanel.add(timeButton);
        }

        JPanel repeatPanel = new JPanel();
        centralPanel.add(repeatPanel);
        repeatPanel.setLayout(new BoxLayout(repeatPanel, BoxLayout.Y_AXIS));

        ButtonGroup repeatButtonGroup = new ButtonGroup();
        JRadioButton repeatNever = new JRadioButton("do not repeat", true);
        JRadioButton repeatDays = new JRadioButton("days");
        JRadioButton repeatHours = new JRadioButton("hours");
        JRadioButton repeatMinutes = new JRadioButton("minutes");
        JRadioButton repeatSeconds = new JRadioButton("seconds");
        repeatButtonGroup.add(repeatNever);
        repeatButtonGroup.add(repeatDays);
        repeatButtonGroup.add(repeatHours);
        repeatButtonGroup.add(repeatMinutes);
        repeatButtonGroup.add(repeatSeconds);

        repeatPanel.add(repeatNever);
        JPanel repeatSubPanel = new JPanel();
        repeatSubPanel.add(new JLabel("repeat every"));
        JSpinner repeatSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        repeatSubPanel.add(repeatSpinner);
        JPanel repeatEveryPanel = new JPanel();
        repeatEveryPanel.setLayout(new BoxLayout(repeatEveryPanel, BoxLayout.Y_AXIS));
        repeatEveryPanel.add(repeatDays);
        repeatEveryPanel.add(repeatHours);
        repeatEveryPanel.add(repeatMinutes);
        repeatEveryPanel.add(repeatSeconds);
        repeatSubPanel.add(repeatEveryPanel);
        repeatPanel.add(repeatSubPanel);

        JPanel soundPanel = new JPanel();
        centralPanel.add(soundPanel);
        soundPanel.setLayout(new GridLayout(0, 1));
        soundPanel.add(new JLabel("sound:"));
        ButtonGroup soundButtonGroup = new ButtonGroup();
        for (int i = 1; i <= 3; i++) {
            JRadioButton soundButton = new JRadioButton("alarm" + i + ".wav", i == 1);
            soundButtonGroup.add(soundButton);
            soundPanel.add(soundButton);
        }
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        centralPanel.add(buttonPanel);

        JButton setAlarm = new JButton("Set alarm");
        buttonPanel.add(setAlarm);
        setAlarm.addActionListener((e) -> {
            try {
                TextMessage textMessage = context.createTextMessage();
                textMessage.setStringProperty("for", "alarm");
                textMessage.setStringProperty("command", "set");

                String due = null;
                for (Enumeration<AbstractButton> buttons = timeButtonGroup.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    if (button.isSelected()) {
                        due = button.getText();
                    }
                }
                if (due.equals(timePickerButton.getText())) {
                    textMessage.setLongProperty("due", ((Date) spinner.getValue()).getTime());
                } else {
                    int hours = Integer.parseInt(due.substring(0, due.indexOf(':')));
                    int minutes = Integer.parseInt(due.substring(due.indexOf(':') + 1, due.length()));
                    Calendar cal = Calendar.getInstance();
                    if (hours < cal.get(Calendar.HOUR_OF_DAY) || (hours == cal.get(Calendar.HOUR_OF_DAY) && minutes <= cal.get(Calendar.MINUTE))) {
                        cal.add(Calendar.DATE, 1);
                    }
                    cal.set(Calendar.HOUR_OF_DAY, hours);
                    cal.set(Calendar.MINUTE, minutes);
                    cal.set(Calendar.SECOND, 0);
                    textMessage.setLongProperty("due", cal.getTimeInMillis());
                }

                String repeat = null;
                for (Enumeration<AbstractButton> buttons = repeatButtonGroup.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    if (button.isSelected()) {
                        repeat = button.getText();
                    }
                }
                int repeatInterval = 0;
                switch (repeat) {
                    case "do not repeat":
                        repeatInterval = 0;
                        break;
                    case "seconds":
                        repeatInterval = (Integer) repeatSpinner.getValue();
                        break;
                    case "minutes":
                        repeatInterval = (Integer) repeatSpinner.getValue() * 60;
                        break;
                    case "hours":
                        repeatInterval = ((Integer) repeatSpinner.getValue()) * 60 * 60;
                        break;
                    case "days":
                        repeatInterval = ((Integer) repeatSpinner.getValue()) * 60 * 60 * 24;
                        break;
                    default:
                        break;
                }
                textMessage.setIntProperty("repeatInterval", repeatInterval);

                String sound = null;
                for (Enumeration<AbstractButton> buttons = soundButtonGroup.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    if (button.isSelected()) {
                        sound = button.getText();
                    }
                }
                textMessage.setStringProperty("sound", sound);
                producer.send(topic, textMessage);
            } catch (JMSException ex) {
                Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        JButton reactivateTimers = new JButton("Reactivate timers");
        buttonPanel.add(reactivateTimers);
        reactivateTimers.setToolTipText("Cancels all scheduled timers and then schedules those that are due on a future date.");
        reactivateTimers.addActionListener((e) -> {
            try {
                TextMessage textMessage = context.createTextMessage();
                textMessage.setStringProperty("for", "alarm");
                textMessage.setStringProperty("command", "reactivate");
                producer.send(topic, textMessage);
            } catch (JMSException ex) {
                Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        return panel;
    }

    private JPanel createPlannerPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(Color.BLACK, 2));
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Planner");
        panel.add(label, BorderLayout.NORTH);

        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new GridLayout(0, 2));

        JLabel dateLabel = new JLabel("Time:");
        entryPanel.add(dateLabel);

        JSpinner spinner = new JSpinner();
        spinner.setModel(new SpinnerDateModel(new Date(), new Date(), null, Calendar.HOUR_OF_DAY));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd.MM.yyyy HH:mm:ss"));
        spinner.addChangeListener((ChangeEvent e) -> {
            try {
                if (((Date) spinner.getValue()).before(new Date())) {
//                    Calendar cal = Calendar.getInstance();
//                    cal.add(Calendar.MINUTE, 1);
//                    spinner.setValue(new Date(cal.getTimeInMillis()));
                    spinner.setValue(new Date());
                    return;
                }
                spinner.commitEdit();
            } catch (ParseException ex) {
                spinner.setValue(new Date());
//                    Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        entryPanel.add(spinner);

        JLabel textLabel = new JLabel("Task text:");
        entryPanel.add(textLabel);

        JTextField textField = new JTextField();
        entryPanel.add(textField);

        JLabel startLabel = new JLabel("Starting location (leave empty for current):");
        entryPanel.add(startLabel);

        JTextField startField = new JTextField();
        entryPanel.add(startField);

        JLabel destinationLabel = new JLabel("Destination:");
        entryPanel.add(destinationLabel);

        JTextField destinationField = new JTextField();
        entryPanel.add(destinationField);

        JCheckBox setAlarm = new JCheckBox("Set alarm");
        entryPanel.add(setAlarm);

        panel.add(entryPanel, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane();
        JList list = new JList();
        list.setLayoutOrientation(JList.VERTICAL);
        panel.add(scrollPane, BorderLayout.CENTER);
        refreshTaskList(scrollPane, list);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1));
        JButton buttonNew = new JButton("New");
        buttonNew.addActionListener((e) -> {
            try {
                TextMessage textMessage = context.createTextMessage();
                textMessage.setStringProperty("for", "planner");
                textMessage.setStringProperty("command", "new");
                textMessage.setLongProperty("due", ((Date) spinner.getValue()).getTime());
                textMessage.setStringProperty("text", textField.getText());
                textMessage.setStringProperty("startingLocation", startField.getText());
                textMessage.setStringProperty("destination", destinationField.getText());
                textMessage.setBooleanProperty("alarm", setAlarm.isSelected());
                producer.send(topic, textMessage);
                refreshTaskList(scrollPane, list);
            } catch (JMSException ex) {
                Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        buttonPanel.add(buttonNew);
        JButton buttonChange = new JButton("Change");
        buttonChange.addActionListener((e) -> {
            try {
                if (list.getSelectedIndex() <= -1) {
                    return;
                }
                TextMessage textMessage = context.createTextMessage();
                textMessage.setStringProperty("for", "planner");
                textMessage.setStringProperty("command", "change");
                Matcher matcher = Pattern.compile("\\d+").matcher((String) list.getSelectedValue());
                matcher.find();
                long id = Long.valueOf(matcher.group());
                textMessage.setLongProperty("id", id);
                textMessage.setLongProperty("due", ((Date) spinner.getValue()).getTime());
                textMessage.setStringProperty("text", textField.getText());
                textMessage.setStringProperty("startingLocation", startField.getText());
                textMessage.setStringProperty("destination", destinationField.getText());
                textMessage.setBooleanProperty("alarm", setAlarm.isSelected());
                producer.send(topic, textMessage);
                refreshTaskList(scrollPane, list);
            } catch (JMSException ex) {
                Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        buttonPanel.add(buttonChange);
        JButton buttonDelete = new JButton("Delete");
        buttonDelete.addActionListener((e) -> {
            try {
                if (list.getSelectedIndex() <= -1) {
                    return;
                }
                TextMessage textMessage = context.createTextMessage();
                textMessage.setStringProperty("for", "planner");
                textMessage.setStringProperty("command", "delete");
                Matcher matcher = Pattern.compile("\\d+").matcher((String) list.getSelectedValue());
                matcher.find();
                long id = Long.valueOf(matcher.group());
                textMessage.setLongProperty("id", id);
                producer.send(topic, textMessage);
                refreshTaskList(scrollPane, list);
            } catch (JMSException ex) {
                Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        buttonPanel.add(buttonDelete);
        JButton buttonRefresh = new JButton("Refresh");
        buttonRefresh.addActionListener((e) -> {
            refreshTaskList(scrollPane, list);
        });
        buttonPanel.add(buttonRefresh);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void refreshTaskList(JScrollPane taskListPane, JList list) {
        try {
            TextMessage textMessage = context.createTextMessage();
            textMessage.setStringProperty("for", "planner");
            textMessage.setStringProperty("command", "list");
            producer.send(topic, textMessage);

            SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
                @Override
                protected List<String> doInBackground() throws Exception {
                    Message message = consumerPlanner.receive(UserDevice.TIMEOUT);
                    if (message instanceof ObjectMessage) {
                        ObjectMessage objectMessage = (ObjectMessage) message;
                        if (objectMessage.getStringProperty("reply").equals("list")) {
                            return (List<String>) objectMessage.getObject();
                        }
                    }
                    return Collections.<String>emptyList();
                }

                @Override
                protected void done() {
                    try {
                        list.setListData(get().toArray(new String[0]));
                        taskListPane.setViewportView(list); // Refreshes the JScrollPane.
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            worker.execute();
        } catch (JMSException ex) {
            Logger.getLogger(UserDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createFrame() {
        frame = new JFrame("User Device");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(createPlayerPanel());
        frame.add(createAlarmPanel());
        frame.add(createPlannerPanel());

        frame.setSize(1024, 768);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        UserDevice userDevice = new UserDevice();
    }

}
