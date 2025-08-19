import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Application extends JFrame {

    private List<Email> searchEmaillist = new ArrayList<>();
	private CardLayout cardLayout;
    private JPanel cardPanel;
    private JTextArea resultArea;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JComboBox<Integer> pageSizeComboBox;
    private JLabel pageInfoLabel;
    private JButton prevButton, nextButton;
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalPages = 1;
    private String token;
    private List<Object[]> allData = new ArrayList<>();
    private List<JTextField> searchFields = new ArrayList<>();
    private List<JSpinner> searchSpinner = new ArrayList<>();

    public Application() {
        setTitle("Azure-Data-Guardian");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 850);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createFirstPanel(), "panel1");
        cardPanel.add(createSecondPanel(), "panel2");

        add(cardPanel);
    }

    private JPanel createFirstPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JTextField fieldA = new JTextField();
        JTextField fieldB = new JTextField();
        JTextField fieldC = new JTextField();

        topPanel.add(createLabeledField("TENANT_ID:", fieldA, 5));
        topPanel.add(createLabeledField("CLIENT_ID:", fieldB, 5));
        topPanel.add(createLabeledField("CLIENT_SECRET:", fieldC, 5));

        JButton processButton = new JButton("Generate Token");
        processButton.addActionListener(e -> {
        	String result = null;
			try {
				result = GraphApiTokenGenerator.getAccessToken(fieldA.getText(),fieldB.getText(),fieldC.getText());
			} catch (IOException e1) {
				result = e1.toString();
			}
            resultArea.setText(result);
        });
        topPanel.add(processButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        resultArea = new JTextArea(5, 30);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JButton nextButton = new JButton("Email Management");
        nextButton.addActionListener(e -> {
        	token = resultArea.getText();
        	cardLayout.show(cardPanel, "panel2");
        });
        bottomPanel.add(nextButton, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSecondPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = createTopSection();
        JPanel bottomPanel = createBottomSection();

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopSection() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setPreferredSize(new Dimension(0, 140));
        topPanel.setBackground(new Color(245, 245, 245));

        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        inputPanel.setBackground(new Color(245, 245, 245));
        
        JPanel emailPanel = createInputPanel("Email Address:");
        JPanel startDateTimePanel = createInputPanel("Start Date/Time:");
        JPanel endDateTimePanel = createInputPanel("End Date/Time:");
        JPanel subjectPanel = createInputPanel("Subject:");
        JPanel FromAddressPanel = createInputPanel("from address:");
   
        inputPanel.add(emailPanel);
        inputPanel.add(startDateTimePanel);
        inputPanel.add(endDateTimePanel);
        inputPanel.add(subjectPanel);
        inputPanel.add(FromAddressPanel);
        
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(70, 130, 180));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.setPreferredSize(new Dimension(120, 32));
        searchButton.addActionListener(this::performSearch);

        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createInputPanel(String label) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(new Color(245, 245, 245));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.WEST);
        
        if (label.contains("Date/Time")) {
        	if (label.contains("Start Date/Time")){
                JSpinner spinner = createDateTimeSpinner();
                panel.add(spinner, BorderLayout.CENTER);
                searchSpinner.add(spinner);
        	}
        	if (label.contains("End Date/Time")){
                JSpinner spinner = createEndDateTimeSpinner();
                panel.add(spinner, BorderLayout.CENTER);
                searchSpinner.add(spinner);
        	}
        } else {
            JTextField textField = new JTextField();
            panel.add(textField, BorderLayout.CENTER);
            searchFields.add(textField);
        }
       
        return panel;
    }

    
    private JPanel createBottomSection() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(Color.WHITE);

        String[] columnNames = {"Select","id", "subject", "bodyPreview", "from address", "toRecipients address", "receivedDateTime"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(25);
        dataTable.setFont(new Font("Arial", Font.PLAIN, 12));
        dataTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        dataTable.setSelectionBackground(new Color(173, 216, 230));
        dataTable.setFillsViewportHeight(true);

        JTableHeader header = dataTable.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new GridLayout(1, columnNames.length));
        headerPanel.setBackground(new Color(70, 130, 180));
        for (String columnName : columnNames) {
            JLabel label = new JLabel(columnName, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setForeground(Color.WHITE);
            label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            headerPanel.add(label);
        }
        header.add(headerPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setBackground(Color.WHITE);

        JPanel paginationPanel = new JPanel(new BorderLayout(10, 10));
        paginationPanel.setBackground(Color.WHITE);

        JPanel pageSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pageSizePanel.setBackground(Color.WHITE);
        pageSizePanel.add(new JLabel("Items per page:"));
        pageSizeComboBox = new JComboBox<>(new Integer[]{20, 50, 100});
        pageSizeComboBox.setSelectedItem(pageSize);
        pageSizeComboBox.addActionListener(e -> {
            pageSize = (Integer) pageSizeComboBox.getSelectedItem();
            currentPage = 1;
            totalPages = 1;
            updateTableForCurrentPage();
        });
        pageSizePanel.add(pageSizeComboBox);

        JPanel pageNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pageNavPanel.setBackground(Color.WHITE);
        prevButton = new JButton("Previous");
        prevButton.setBackground(new Color(70, 130, 180));
        prevButton.setForeground(Color.WHITE);
        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableForCurrentPage();
            }
        });
        
        pageInfoLabel = new JLabel("Page 1");
        pageInfoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        nextButton = new JButton("Next");
        nextButton.setBackground(new Color(70, 130, 180));
        nextButton.setForeground(Color.WHITE);
        nextButton.addActionListener(e -> {
                currentPage++;
                updateTableForCurrentPage();
        });
        
        pageNavPanel.add(prevButton);
        pageNavPanel.add(pageInfoLabel);
        pageNavPanel.add(nextButton);

        paginationPanel.add(pageSizePanel, BorderLayout.WEST);
        paginationPanel.add(pageNavPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton downloadSelectedButton = createActionButton("Download Selected", Color.GREEN.darker());
        JButton downloadAllButton = createActionButton("Download All", Color.ORANGE.darker());
        JButton backButton = createActionButton("Back to Generate Token", Color.GRAY);

        backButton.addActionListener(e -> cardLayout.show(cardPanel, "panel1"));
        downloadSelectedButton.addActionListener(this::downloadSelected);
        downloadAllButton.addActionListener(this::downloadAll);

        buttonPanel.add(downloadSelectedButton);
        buttonPanel.add(downloadAllButton);
        buttonPanel.add(backButton);

        controlPanel.add(paginationPanel, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        bottomPanel.add(controlPanel, BorderLayout.SOUTH);

        return bottomPanel;
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        return button;
    }

    private JSpinner createDateTimeSpinner() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        JSpinner spinner = new JSpinner(new SpinnerDateModel(
                calendar.getTime(),
                null,
                null,
                Calendar.HOUR_OF_DAY
        ));

        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "MM/dd/yyyy HH:mm:ss");
        spinner.setEditor(editor);

        return spinner;
    }

    private JSpinner createEndDateTimeSpinner() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        JSpinner spinner = new JSpinner(new SpinnerDateModel(
                calendar.getTime(),
                null,
                null,
                Calendar.HOUR_OF_DAY
        ));

        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "MM/dd/yyyy HH:mm:ss");
        spinner.setEditor(editor);

        return spinner;
    }

    private void performSearch(ActionEvent e) {
        allData.clear();
        searchEmaillist.clear();
        
        String searchFilter = GraphEmailSearcher.buildFilterQuery(searchFields.get(1).getText(),null,searchFields.get(2).getText(),null, searchSpinner.get(0).toString(),searchSpinner.get(1).toString());
        try {
			List<Email> searchEmaillisttemp = GraphEmailSearcher.searchEmails(token,searchFields.get(0).getText(), searchFilter, pageSize, currentPage);
			for (int i = 0; i < searchEmaillisttemp.size(); i++) {
				searchEmaillist.add(searchEmaillisttemp.get(i));
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        for (int i = 0; i < searchEmaillist.size(); i++) {
            allData.add(new Object[]{
                    false,
                    searchEmaillist.get(i).id,
                    searchEmaillist.get(i).subject,
                    searchEmaillist.get(i).bodyPreview,
                    searchEmaillist.get(i).from,
                    searchEmaillist.get(i).toRecipients,
                    searchEmaillist.get(i).receivedDateTime
        });}
        
        currentPage = 1;
        totalPages = 1;
        updateTableForCurrentPage();
    }

    private void updateTableForCurrentPage() {
        tableModel.setRowCount(0);        
        pageInfoLabel.setText("Page " + currentPage);
        prevButton.setEnabled(currentPage > 1);
        int start = (currentPage - 1) * pageSize;
        int end = start + pageSize;
        
        if (end <= allData.size() || currentPage == 1 || currentPage == totalPages) {
        	for (int i = start; i < min(end,allData.size()); i++) {
              tableModel.addRow(allData.get(i));
          }
        	return;
        }
        if (end > allData.size()) {
            
            String searchFilter = GraphEmailSearcher.buildFilterQuery(searchFields.get(1).getText(),null,searchFields.get(2).getText(),null,searchSpinner.get(0).toString(),searchSpinner.get(1).toString());
            try {
    			List<Email> searchEmaillisttemp = GraphEmailSearcher.searchEmails(token,searchFields.get(0).getText(), searchFilter, pageSize, currentPage);
                if (searchEmaillisttemp.size() == 0 ) {
                	totalPages = currentPage -1;
                	JOptionPane.showMessageDialog(null, "No more data avaliable","Information",JOptionPane.INFORMATION_MESSAGE);
                }
    			for (int i = 0; i < searchEmaillisttemp.size(); i++) {
    				searchEmaillist.add(searchEmaillisttemp.get(i));
    			}} catch (IOException e1) {
    			e1.printStackTrace();
    		}

            for (int i = start; i < searchEmaillist.size(); i++) {
                allData.add(new Object[]{
                        false,
                        searchEmaillist.get(i).id,
                        searchEmaillist.get(i).subject,
                        searchEmaillist.get(i).bodyPreview,
                        searchEmaillist.get(i).from,
                        searchEmaillist.get(i).toRecipients,
                        searchEmaillist.get(i).receivedDateTime
            });}
            for (int i = start; i < allData.size(); i++) {
                tableModel.addRow(allData.get(i));
            }
        }
    }

    private int min(int a, int b) {
		if (a<= b) {
			return a;
		}
		return b;
	}

	private void downloadSelected(ActionEvent e) {
    	String savePath = showDirectorySelectionDialog();
    	
    	
        StringBuilder sb = new StringBuilder("Downloading selected items:\n");
        int count = 0;
        List<Email> selectedEmaillist = new ArrayList<>();
        Email selectedTempEmail = new Email("","","","","","");
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean selected = (Boolean) tableModel.getValueAt(i, 0);
            if (selected != null && selected) {
                sb.append("Row ").append(i + 1).append(": ")
                        .append(tableModel.getValueAt(i, 1)).append("\n");
                selectedTempEmail.id = (String) tableModel.getValueAt(i, 1);
                selectedTempEmail.subject =  (String) tableModel.getValueAt(i, 2);
                selectedTempEmail.from =  (String) tableModel.getValueAt(i, 4);
                selectedTempEmail.receivedDateTime =  (String) tableModel.getValueAt(i, 6);
                selectedEmaillist.add(selectedTempEmail);
                count++;
            }
        }

        if (count == 0) {
            sb.append("No items selected");
        }
        try {
        	for (int i = 0; i < selectedEmaillist.size(); i++) {
        		GraphApiEmailDownloader.downloadEmailAsEML(selectedEmaillist.get(i), token,savePath,searchFields.get(0).getText());
        	}	
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        JOptionPane.showMessageDialog(this, sb.toString(), "Download Selected", JOptionPane.INFORMATION_MESSAGE);
    }

    private void downloadAll(ActionEvent e) {
    	List<Email> searchTempEmaillist = null;
    	String savePath = showDirectorySelectionDialog();


    	String searchFilter = GraphEmailSearcher.buildFilterQuery(searchFields.get(1).getText(),null,searchFields.get(2).getText(),null,searchSpinner.get(0).toString(),searchSpinner.get(1).toString());
        
        try {
        	searchTempEmaillist = GraphEmailSearcher.searchAllEmails(token,searchFields.get(0).getText(), searchFilter);
        	for (int i = 0; i < searchTempEmaillist.size(); i++) {
        		GraphApiEmailDownloader.downloadEmailAsEML(searchTempEmaillist.get(i), token,savePath,searchFields.get(0).getText());
        	}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        JOptionPane.showMessageDialog(this,
                "Downloading all " + searchTempEmaillist.size() + " items",
                "Download All",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private static String showDirectorySelectionDialog() {
    	JFileChooser chooser = new JFileChooser();
    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	chooser.setDialogTitle("choose path to save");
    	int result = chooser.showOpenDialog(null);
    	if (result == JFileChooser.APPROVE_OPTION) {
    		File selectedDir = chooser.getSelectedFile();
    		return selectedDir.getAbsolutePath();
    	}
    	return null;
    }
    

    private JPanel createLabeledField(String label, JTextField field, int hgap) {
        JPanel panel = new JPanel(new BorderLayout(hgap, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        	Application app = new Application();
            app.setVisible(true);
        });
    }
}
