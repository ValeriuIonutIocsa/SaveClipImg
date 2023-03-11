package com.personal.scripts.gen.save_clip_img;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import javax.imageio.ImageIO;

final class AppStartSaveClipImg {

	private AppStartSaveClipImg() {
	}

	public static void main(
			final String[] args) {

		final Instant start = Instant.now();

		if (args.length >= 1 && "-help".equals(args[0])) {

			final String helpMessage = createHelpMessage();
			System.out.println(helpMessage);
			System.exit(0);
		}

		if (args.length < 1) {

			final String helpMessage = createHelpMessage();
			System.err.println("insufficient arguments" + System.lineSeparator() + helpMessage);
			System.exit(1);
		}

		final String outputImageFolderPathString = args[0];

		String outputImageFileName;
		if (args.length >= 2) {

			outputImageFileName = args[1];
			String ext = ext(outputImageFileName);
			if (ext == null) {

				ext = "png";
				outputImageFileName += "." + ext;
			}

		} else {
			final String dateTimeString = createDateTimeString();
			outputImageFileName = "image_" + dateTimeString + ".png";
		}

		final Path outputImagePath =
				Paths.get(outputImageFolderPathString, outputImageFileName).toAbsolutePath().normalize();
		copyTo(outputImagePath);

		final Duration executionTime = Duration.between(start, Instant.now());
		System.out.println("done; execution time: " + durationToString(executionTime));
	}

	private static String createHelpMessage() {

		return "usage: save_clip_img FOLDER_PATH (OUTPUT_FILE_NAME)";
	}

	private static String createDateTimeString() {

		return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
	}

	static String ext(
			final String filename) {

		String ext = null;
		final int lastIndexOf = filename.lastIndexOf('.');
		if (lastIndexOf >= 0) {
			ext = filename.substring(lastIndexOf + 1);
		}
		return ext;
	}

	private static void copyTo(
			final Path outputImagePath) {

		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final Transferable content = clipboard.getContents(null);
		if (content == null) {
			System.err.println("ERROR - nothing found in clipboard");
			System.exit(1);
		}

		System.out.println("--> saving clipboard image to:");
		System.out.println(outputImagePath);

		try {
			final Path outputImageFolderPath = outputImagePath.getParent();
			if (!Files.isDirectory(outputImageFolderPath)) {
				Files.createDirectories(outputImageFolderPath);
			}

			if (content.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				saveImageRegular(content, outputImagePath);

			} else if (content.isDataFlavorSupported(DataFlavor.allHtmlFlavor)) {
				saveImageHtmlFlavor(content, outputImagePath);

			} else {
				noImageFound();
			}

		} catch (final Exception exc) {
			System.err.println("ERROR - failed to write image to file");
			exc.printStackTrace();
			System.exit(3);
		}
	}

	private static void saveImageRegular(
			final Transferable content,
			final Path outputImagePath) throws Exception {

		final BufferedImage img = (BufferedImage) content.getTransferData(DataFlavor.imageFlavor);
		final File outfile = outputImagePath.toFile();
		final boolean success = ImageIO.write(img, "png", outfile);
		if (!success) {
			throw new Exception();
		}
	}

	private static void saveImageHtmlFlavor(
			final Transferable content,
			final Path outputImagePath) throws Exception {

		String htmlString = (String) content.getTransferData(DataFlavor.allHtmlFlavor);

		final String prefix = "<img src=\"";
		int indexOf = htmlString.indexOf(prefix);
		if (indexOf < 0) {
			noImageFound();

		} else {
			htmlString = htmlString.substring(indexOf + prefix.length());
			indexOf = htmlString.indexOf("\"");
			if (indexOf < 0) {
				noImageFound();

			} else {
				final String imageUrl = htmlString.substring(0, indexOf);
                System.out.println("--> downloading image from URL:");
                System.out.println(imageUrl);
				try (InputStream inputStream = new URL(imageUrl).openStream()) {

					Files.copy(inputStream, outputImagePath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	private static void noImageFound() {

		System.err.println("ERROR - no image found in clipboard");
		System.exit(2);
	}

	private static String durationToString(
			final Duration duration) {

		final StringBuilder stringBuilder = new StringBuilder();
		final long allSeconds = duration.get(ChronoUnit.SECONDS);
		final long hours = allSeconds / 3600;
		if (hours > 0) {
			stringBuilder.append(hours).append("h ");
		}

		final long minutes = (allSeconds - hours * 3600) / 60;
		if (minutes > 0) {
			stringBuilder.append(minutes).append("m ");
		}

		final long nanoseconds = duration.get(ChronoUnit.NANOS);
		final double seconds = allSeconds - hours * 3600 - minutes * 60 +
				nanoseconds / 1_000_000_000.0;
		stringBuilder.append(doubleToString(seconds)).append('s');

		return stringBuilder.toString();
	}

	private static String doubleToString(
			final double d) {

		final String str;
		if (Double.isNaN(d)) {
			str = "";

		} else {
			final String format;
			format = "0.000";
			final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
			final DecimalFormat decimalFormat = new DecimalFormat(format, decimalFormatSymbols);
			str = decimalFormat.format(d);
		}
		return str;
	}
}
