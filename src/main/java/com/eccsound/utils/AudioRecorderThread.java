package com.eccsound.utils;

import javax.sound.sampled.*;
import java.io.File;

public class AudioRecorderThread implements Runnable {
    private File tempFile;
    private boolean stopRequested = false;

    public void stopRecording() {
        stopRequested = true;
    }

    @Override
    public void run() {
        try {
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }

            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            tempFile = File.createTempFile("temp_rec", ".wav");
            AudioInputStream ais = new AudioInputStream(line);
            Thread savingThread = new Thread(() -> {
                try {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, tempFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            savingThread.start();
            while (!stopRequested && savingThread.isAlive()) {
                Thread.sleep(100);
            }

            line.drain();
            line.stop();
            line.close();

            System.out.println(tempFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getTempFile() {
        return tempFile;
    }
}
