
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class TagExtractor {
    JFileChooser fileChooser = new JFileChooser();
    JFrame frame = new JFrame();
    JTextArea textArea = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(textArea);
    JButton extractBtn = new JButton("Extract");
    JButton changeFilterBtn = new JButton("Change Filter");
    JButton changeFileBtn = new JButton("Change File");
    
    private File targetFile;
    private File filterFile;
    
    public File pickTarget(){
        fileChooser.showOpenDialog(frame);
        File file = fileChooser.getSelectedFile();
        return file;
    }

    public TagExtractor() {
        frame = new JFrame();
        frame.setTitle("Tag Extractor");
        frame.setLayout(new BorderLayout());
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(changeFileBtn);
        buttonPanel.add(extractBtn);
        buttonPanel.add(changeFilterBtn);
        
        // Add action listeners
        changeFileBtn.addActionListener(e -> {
            targetFile = pickTarget();
            if (targetFile != null) {
                textArea.setText("Selected target file: " + targetFile.getName() + "\n");
            }
        });
        
        changeFilterBtn.addActionListener(e -> {
            filterFile = pickTarget();
            if (filterFile != null) {
                textArea.append("Selected filter file: " + filterFile.getName() + "\n");
            }
        });
        
        extractBtn.addActionListener(e -> {
            if (targetFile == null || filterFile == null) {
                JOptionPane.showMessageDialog(frame, 
                    "Please select both target and filter files first!", 
                    "Missing Files", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            extractTags();
        });
        
        // Add components to frame
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        // Initial file selection
        targetFile = pickTarget();
        filterFile = pickTarget();
        
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    private void extractTags() {
        try {
            // First, read filter words into an array
            ArrayList<String> filterWords = new ArrayList<>();
            Scanner filterScanner = new Scanner(filterFile);
            while (filterScanner.hasNext()) {
                filterWords.add(filterScanner.next().toLowerCase());
            }
            filterScanner.close();

            // First pass: count unique words
            ArrayList<String> words = new ArrayList<>();
            ArrayList<Integer> counts = new ArrayList<>();
            
            textArea.setText("Processing files...\n");
            textArea.append("Target: " + targetFile.getName() + "\n");
            textArea.append("Filter: " + filterFile.getName() + "\n\n");
            
            Scanner targetScanner = new Scanner(targetFile);
            while (targetScanner.hasNext()) {
                String word = targetScanner.next().toLowerCase();
                // Remove any punctuation
                word = word.replaceAll("[^a-zA-Z]", "");
                
                // Skip empty strings and filtered words
                if (word.isEmpty() || filterWords.contains(word)) {
                    continue;
                }
                
                // Check if word exists in our array
                int index = words.indexOf(word);
                if (index == -1) {
                    // New word
                    words.add(word);
                    counts.add(1);
                } else {
                    // Existing word
                    counts.set(index, counts.get(index) + 1);
                }
            }
            targetScanner.close();

            // Convert to arrays for sorting
            String[] wordsArray = words.toArray(new String[0]);
            Integer[] countsArray = counts.toArray(new Integer[0]);
            
            // Sort both arrays based on counts
            for (int i = 0; i < wordsArray.length - 1; i++) {
                for (int j = i + 1; j < wordsArray.length; j++) {
                    if (countsArray[j] > countsArray[i] || 
                        (countsArray[j].equals(countsArray[i]) && 
                         wordsArray[j].compareTo(wordsArray[i]) < 0)) {
                        // Swap counts
                        Integer tempCount = countsArray[i];
                        countsArray[i] = countsArray[j];
                        countsArray[j] = tempCount;
                        // Swap words
                        String tempWord = wordsArray[i];
                        wordsArray[i] = wordsArray[j];
                        wordsArray[j] = tempWord;
                    }
                }
            }

            // Display results
            textArea.append("Word Frequencies:\n");
            textArea.append("================\n");
            for (int i = 0; i < wordsArray.length; i++) {
                textArea.append(String.format("%-20s: %d\n", wordsArray[i], countsArray[i]));
            }
            
            textArea.append("\nTotal unique words: " + wordsArray.length);
            
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(frame,
                "Error: File not found - " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                "Error processing files: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        TagExtractor tagExtractor = new TagExtractor();
    }
}