package com.iprd.fhir.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QrReceipt {

	private static final String BASE_PATH = "./data/QR_Lines.png";
	private static final String LOGO_PATH = "./data/ocl.png";

	private static final Logger logger = LoggerFactory.getLogger(QrReceipt.class);

    public static byte[] generatePlainQRReceipt(String data) {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 0);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            BufferedImage receipt = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = (Graphics2D) receipt.getGraphics();
            RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHints(qualityHints);


            // Create a qr code with the url as content and a size of WxH px
            bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 550, 550, hints);
            // Load QR image
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, getMatrixConfig());
            // draw entire component as receipt
            g.drawImage(receipt, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(25, 25, receipt.getWidth() - 50, receipt.getHeight() - 50, 40, 40);
            // Write QR code to new image at position 0/0
            g.drawImage(qrImage, 25, 25, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            ImageIO.write(receipt, "PNG", os);
            return os.toByteArray();

        } catch (WriterException | IOException e) {
            // TODO Auto-generated catch block
			  logger.warn(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

	public static byte[] generateQrReceipt(String qrString, String campaignName, String date, String openCampLinkId, int errorCorrectionLevelBits) {
		// Create new configuration that specifies the error correction
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.forBits(errorCorrectionLevelBits));
		hints.put(EncodeHintType.MARGIN, 1);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			QRCodeWriter writer = new QRCodeWriter();
			// Create a qr code with the url as content and a size of WxH px
			BitMatrix bitMatrix = writer.encode(qrString, BarcodeFormat.QR_CODE, 450, 450, hints);
			// Load QR image
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, getMatrixConfig());
			// Load template image
			BufferedImage receipt = getOverlay(BASE_PATH);
			// Load logo image
			BufferedImage logo = getOverlay(LOGO_PATH);

			// Initialize combined image
			BufferedImage combined = new BufferedImage(receipt.getWidth(), receipt.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

			Graphics2D g = (Graphics2D) combined.getGraphics();

			RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
			qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			g.setRenderingHints(qualityHints);

			// draw entire component as receipt
			g.drawImage(receipt, 0, 0, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

			g.setColor(new Color(255, 255, 255));
			g.fillRoundRect(25, 25, receipt.getWidth() - 50, receipt.getHeight() - 50, 40, 40);


			qrImage = qrImage.getSubimage(0, 0, 450, 450);

			// Write QR code to new image at position 0/0
			g.drawImage(qrImage, 25, 30, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

			Font font = new Font("Arial", Font.BOLD, 30);
			g.setFont(font);
			FontMetrics fms = g.getFontMetrics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g.setColor(new Color(0, 0, 0));

			drawCampaignName(campaignName, g, receipt.getWidth());

			int w = fms.stringWidth(date);
			g.drawString(date, (receipt.getWidth() - w) / 2, 610);

			int height = 150;
			int width = logo.getWidth();
			float ratio = 1;
			if (logo.getHeight() > height) {
				ratio = logo.getHeight() * 1.0f / height;
			}
			width = (int) (width / ratio);
			height = (int) (logo.getHeight() / ratio);

			logo = Scalr.resize(logo, Scalr.Method.QUALITY, width, height);
			g.drawImage(logo, (receipt.getWidth() - logo.getWidth()) / 2, 620, null);
			g.setColor(new Color(0, 0, 0));

			String l1 = openCampLinkId.substring(0, 4);
			String l2 = openCampLinkId.substring(4, 8);
			String l3 = openCampLinkId.substring(8, 12);

			Font header = new Font("Arial", Font.BOLD, 33);
			g.setFont(header);
			fms = g.getFontMetrics();

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

			String text = "MWI";
			float maxWidth = 0;
			for (int i = 0; i < text.length(); i++) {
				maxWidth = Math.max(fms.stringWidth(String.valueOf(text.charAt(i))), maxWidth);
			}
			float sz = 4 * maxWidth;

			float monoWidth = maxWidth;
			for (int i = 0; i < l1.length(); i++) {
				float offset = ((receipt.getWidth() - sz) / 2 + monoWidth / 6 + ((monoWidth + 4) * i));
				g.drawString(String.valueOf(l1.charAt(i)), offset, 810);
			}

			for (int i = 0; i < l2.length(); i++) {
				float offset = ((receipt.getWidth() - sz) / 2 + monoWidth / 6 + ((monoWidth + 4) * i));
				g.drawString(String.valueOf(l2.charAt(i)), offset, 870);
			}

			for (int i = 0; i < l3.length(); i++) {
				float offset = ((receipt.getWidth() - sz) / 2 + monoWidth / 6 + ((monoWidth + 4) * i));
				g.drawString(String.valueOf(l3.charAt(i)), offset, 930);
			}

			ImageIO.write(combined, "PNG", os);
			return os.toByteArray();

		} catch (WriterException | IOException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}

		return null;
	}

	private static void drawCampaignName(String name, Graphics2D graphics2D, int receiptWidth) {
		int padding = 50;
		FontMetrics fontMetrics = graphics2D.getFontMetrics();
		String[] splits = name.split(" ");
		StringBuilder tempString = new StringBuilder();
		StringBuilder suffixString = new StringBuilder();
		ArrayList<String> splitStringList = new ArrayList<>();
		for (String s : splits) {
			tempString.append(s).append(" ");
			int width = fontMetrics.stringWidth(tempString.toString());
			if (width > (receiptWidth - padding)) {
				splitStringList.add(suffixString.toString());
				tempString = new StringBuilder(s);
				suffixString = new StringBuilder();
			} else {
				suffixString.append(s).append(" ");
			}
		}
		splitStringList.add(tempString.toString());
		int y = 490;
		switch (splitStringList.size()) {
			case 1 : {
				y = 540;
				break;
			}
			case 2 : {
				y = 510;
				break;
			}
		}
		for (String str : splitStringList) {
			int width = fontMetrics.stringWidth(str);
			graphics2D.drawString(str, (receiptWidth - width) / 2, y);
			y += 40;
		}
	}

	private static BufferedImage getOverlay(String filePath) throws IOException {
		byte[] b = Utils.getBytesFromFile(filePath);
		InputStream in = new ByteArrayInputStream(b);
		return ImageIO.read(in);
	}


	private static MatrixToImageConfig getMatrixConfig() {
        // ARGB Colors
        // Check Colors ENUM
        return new MatrixToImageConfig(QrReceipt.Colors.BLACK.getArgb(), QrReceipt.Colors.WHITE.getArgb());
    }


    public enum Colors {

        BLUE(0xFF40BAD0), RED(0xFFE91C43), PURPLE(0xFF8A4F9E), ORANGE(0xFFF4B13D), WHITE(0xFFFFFFFF), BLACK(0xFF000000);

        private final int argb;

        Colors(final int argb) {
            this.argb = argb;
        }

        public int getArgb() {
            return argb;
        }
    }

}
