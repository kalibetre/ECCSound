package com.eccsound.controller;

import com.eccsound.model.ProcessedFile;
import com.eccsound.utils.AudioRecorderThread;
import com.eccsound.utils.ECCipher;
import com.eccsound.utils.FileHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.AudioTrack;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.UUID;

public class MainController implements Initializable {
    public TextArea primaryKeyTextArea;
    public TextArea publicKeyTextArea;
    public TextArea receiverPublicKeyTextArea;
    public TextField filePathTextArea;
    public Label recordTimeLabel;
    public Label receiverPubKeyStatus;
    public Label userSessionId;
    private boolean isRecording = false;
    private Stage primaryStage;
    private ECCipher eCCipher;
    private AudioTrack audioTrack;
    private AudioRecorderThread recorderThread;
    private long startTimeMillis;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        eCCipher = new ECCipher();
        String userId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        userSessionId.setText("Session ID: " + userId);
    }

    public void closeWindow(ActionEvent actionEvent) {
        this.primaryStage.close();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setReceiverPublicKey(ActionEvent actionEvent) {
        boolean result = eCCipher.setReceiverPublicKey(receiverPublicKeyTextArea.getText());
        receiverPubKeyStatus.setText(result ? "OK" : "INVALID KEY");

        Alert msg;
        if (result) {
            msg = new Alert(Alert.AlertType.INFORMATION);
            msg.setTitle("Success");
            msg.setHeaderText("Receiver's Elliptic Cipher Public Key set successfully!");
        } else {
            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("Failed to set receiver's Elliptic Cipher Public Key. Try Again");
        }
        msg.showAndWait();
    }

    public void generateKeys(ActionEvent actionEvent) {
        boolean result = eCCipher.generateKeys();
        Alert msg;
        if (result) {
            primaryKeyTextArea.textProperty().set(eCCipher.getPrivateKey());
            publicKeyTextArea.textProperty().set(eCCipher.getPublicKey());

            msg = new Alert(Alert.AlertType.INFORMATION);
            msg.setTitle("Success");
            msg.setHeaderText("Elliptic Cipher Keys generated successfully!");
            msg.showAndWait();
        } else {
            primaryKeyTextArea.textProperty().set("");
            publicKeyTextArea.textProperty().set("");

            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("Failed to generate keys. Try Again");
            msg.showAndWait();
        }
    }

    public void loadKeys(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Private Public Key Pair");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter keyFileFilter = new FileChooser.ExtensionFilter("Key File", "*.ecckey");
        fileChooser.getExtensionFilters().add(keyFileFilter);
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            boolean result = eCCipher.loadKeyPairFromFile(selectedFile.getPath());
            Alert msg;
            if (result) {
                msg = new Alert(Alert.AlertType.INFORMATION);
                msg.setTitle("Success");
                msg.setHeaderText("Elliptic Cipher Keys loaded successfully!");
                primaryKeyTextArea.textProperty().set(eCCipher.getPrivateKey());
                publicKeyTextArea.textProperty().set(eCCipher.getPublicKey());
            } else {
                msg = new Alert(Alert.AlertType.ERROR);
                msg.setTitle("Error");
                msg.setHeaderText("Failed to load Elliptic Cipher Keys. Try Again");
            }
            msg.showAndWait();
        }
    }

    public void saveKeys(ActionEvent actionEvent) {
        if (eCCipher.getPrivateKey() == null || eCCipher.getPublicKey() == null) {
            Alert msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("No Elliptic Cipher Keys. Try Again");
            msg.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Private Public Key Pair");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter keyFileFilter = new FileChooser.ExtensionFilter("Key File", "*.ecckey");
        fileChooser.getExtensionFilters().add(keyFileFilter);
        File selectedFile = fileChooser.showSaveDialog(primaryStage);

        if (selectedFile != null) {
            boolean result = eCCipher.saveKeyPairToFile(selectedFile.getPath());
            Alert msg;
            if (result) {
                msg = new Alert(Alert.AlertType.INFORMATION);
                msg.setTitle("Success");
                msg.setHeaderText("Elliptic Cipher Keys saved successfully!");
            } else {
                msg = new Alert(Alert.AlertType.ERROR);
                msg.setTitle("Error");
                msg.setHeaderText("Failed to save Elliptic Cipher Keys. Try Again");
            }
            msg.showAndWait();
        }
    }

    public void chooseFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to encrypt or decrypt");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            filePathTextArea.textProperty().set(selectedFile.getPath());
        }
    }

    public void encryptFile(ActionEvent actionEvent) {
        String filePath = filePathTextArea.getText();
        Alert msg;

        if (eCCipher.getPublicKey() == null) {
            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("Receiver's Elliptic Cipher Public Key not set!");
            msg.showAndWait();
            return;
        }

        if (filePath == null || filePath.isEmpty()) {
            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("Please select a file to encrypt");
            msg.showAndWait();
        } else {
            ProcessedFile encryptedFile = eCCipher.encryptFile(filePath);
            boolean result = false;

            if (encryptedFile != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save encrypted file");
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("Encrypted File", "*." + encryptedFile.getExtension());
                fileChooser.getExtensionFilters().add(fileFilter);
                File selectedFile = fileChooser.showSaveDialog(primaryStage);

                if (selectedFile != null) {
                    try {
                        FileHelper.writeBytesToFile(encryptedFile.getData(), selectedFile);
                        result = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            if (result) {
                msg = new Alert(Alert.AlertType.INFORMATION);
                msg.setTitle("Success");
                msg.setHeaderText("Encrypted file saved successfully!");
            } else {
                msg = new Alert(Alert.AlertType.ERROR);
                msg.setTitle("Error");
                msg.setHeaderText("Failed to encrypt file. Try Again");
            }
            msg.showAndWait();
        }
    }

    public void decryptFile(ActionEvent actionEvent) {
        String filePath = filePathTextArea.getText();
        Alert msg;

        if (eCCipher.getPrivateKey() == null) {
            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("Your Elliptic Cipher Private Key not set!");
            msg.showAndWait();
            return;
        }

        if (filePath == null || filePath.isEmpty()) {
            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("Please select a file to decrypt");
            msg.showAndWait();
        } else {
            ProcessedFile decryptedFile = eCCipher.decryptFile(filePath);
            boolean result = false;

            if (decryptedFile != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save decrypted file");
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("Decrypted File", "*." + decryptedFile.getExtension());
                fileChooser.getExtensionFilters().add(fileFilter);
                File selectedFile = fileChooser.showSaveDialog(primaryStage);

                if (selectedFile != null) {
                    try {
                        FileHelper.writeBytesToFile(decryptedFile.getData(), selectedFile);
                        result = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            if (result) {
                msg = new Alert(Alert.AlertType.INFORMATION);
                msg.setTitle("Success");
                msg.setHeaderText("Decrypted file saved successfully!");
            } else {
                msg = new Alert(Alert.AlertType.ERROR);
                msg.setTitle("Error");
                msg.setHeaderText("Failed to decrypt file. Try Again");
            }
            msg.showAndWait();
        }
    }

    public void startRecording(ActionEvent actionEvent) {
        if (!isRecording) {
            recorderThread = new AudioRecorderThread();
            Thread thread = new Thread(recorderThread);
            isRecording = true;
            startTimeMillis = System.currentTimeMillis();
            startTimer();
            thread.start();
        }
    }

    public void stopRecording(ActionEvent actionEvent) {
        if (isRecording) {
            recorderThread.stopRecording();
            isRecording = false;
        }
    }

    public void saveEncryptedRecording(ActionEvent actionEvent) {
        Alert msg;
        if (eCCipher.getReceiverPublicKey() == null) {
            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("Receiver's Elliptic Cipher Public Key not set!");
            msg.showAndWait();
            return;
        }

        if (!recorderThread.getTempFile().exists()) {
            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("No audio recorded yet!");
            msg.showAndWait();
            return;
        }
        ProcessedFile decryptedFile = eCCipher.encryptFile(recorderThread.getTempFile().getPath());
        boolean result = false;

        if (decryptedFile != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save encrypted audio recording");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("Encrypted File", "*." + decryptedFile.getExtension());
            fileChooser.getExtensionFilters().add(fileFilter);
            File selectedFile = fileChooser.showSaveDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    FileHelper.writeBytesToFile(decryptedFile.getData(), selectedFile);
                    result = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (result) {
            msg = new Alert(Alert.AlertType.INFORMATION);
            msg.setTitle("Success");
            msg.setHeaderText("Encrypted audio recording saved successfully!");
            recorderThread.getTempFile().delete();
            updateRecordTimeLabel(0);
        } else {
            msg = new Alert(Alert.AlertType.ERROR);
            msg.setTitle("Error");
            msg.setHeaderText("Failed to save encrypted file. Try Again");
        }
        msg.showAndWait();
    }

    private void startTimer() {
        Thread timerThread = new Thread(() -> {
            while (isRecording) {
                long currentTimeMillis = System.currentTimeMillis();
                long elapsedTimeMillis = currentTimeMillis - startTimeMillis;
                updateRecordTimeLabel(elapsedTimeMillis);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.start();
    }

    private void updateRecordTimeLabel(long elapsedTimeMillis) {
        long seconds = elapsedTimeMillis / 1000;
        Platform.runLater(() -> {
            NumberFormat formatter = new DecimalFormat("00");
            String formattedTime = formatter.format(seconds / 60) + ":" + formatter.format(seconds % 60);
            recordTimeLabel.setText(formattedTime);
        });
    }

    public void clearKeys(ActionEvent actionEvent) {
        primaryKeyTextArea.textProperty().set("");
        publicKeyTextArea.textProperty().set("");
        eCCipher.clearKeys();
    }
}
