package com.expirywise.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ImageUtil {

    private static final String IMAGE_FOLDER = "food-images";

    public static String chooseAndSaveImage(Window window) {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Choose Food Image");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif"));

        var selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile == null) {
            return null;
        }

        try {

            Path imageDirectory = Paths.get(IMAGE_FOLDER);

            if (!Files.exists(imageDirectory)) {
                Files.createDirectories(imageDirectory);
            }

            String fileName = System.currentTimeMillis()
                    + "_"
                    + selectedFile.getName();

            Path destination = imageDirectory.resolve(fileName);

            Files.copy(
                    selectedFile.toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);

            return destination.toString();

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }
}