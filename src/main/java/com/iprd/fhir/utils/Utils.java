package com.iprd.fhir.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iprd.report.FhirPath;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.time.LocalDate;

public class Utils {
	public static final int SHORT_ID_LENGTH = 12;

	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	public static String convertToTitleCaseSplitting(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		return Arrays
			.stream(text.split("_"))
			.map(word -> word.isEmpty()
				? word
				: Character.toTitleCase(word.charAt(0)) + word
				.substring(1)
				.toLowerCase())
			.collect(Collectors.joining(" "));
	}

	public static String md5Bytes(byte[] bytes){
		String digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(bytes);
			//converting byte array to Hexadecimal String
			StringBuilder sb = new StringBuilder(2*hash.length);
			for(byte b : hash){
				sb.append(String.format("%02x", b&0xff));
			}
			digest = sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			logger.warn(ExceptionUtils.getStackTrace(ex));
		}
		return digest;
	}
	
	public static String getStringFromFhirPath(FhirPath fhirPath) {
		String fhirPathString = "";
		if(fhirPath.getOperand().isEmpty()) {
			return fhirPathString;
		}
		else if(fhirPath.getOperand().size() == 1) {
			return fhirPath.getOperand().get(0);
		}
		else if(fhirPath.getOperator() == null && fhirPath.getOperand().size() > 1) {
			throw new IllegalArgumentException("Multiple Operands passed without operator");
		}
		fhirPathString = fhirPath.getOperator() + "," + String.join(",",fhirPath.getOperand());
		return fhirPathString;
	}
	
	public static List<List<String>> partitionFacilities(List<String> facilities, int batchSize) {
	    List<List<String>> facilityBatches = new ArrayList<>();
	    int numBatches = (int) Math.ceil((double) facilities.size() / batchSize);
	    for (int i = 0; i < numBatches; i++) {
	        int fromIndex = i * batchSize;
	        int toIndex = Math.min(fromIndex + batchSize, facilities.size());
	        facilityBatches.add(facilities.subList(fromIndex, toIndex));
	    }
	    return facilityBatches;
	}


	public static String getMd5StringFromFhirPath(FhirPath fhirPath) {
		String fhirPathString = getStringFromFhirPath(fhirPath);
		return md5Bytes(fhirPathString.getBytes(StandardCharsets.UTF_8));
	}
	
	

	public static List<String> getMd5StringsFromFhirPaths(List<String> fhirPathStrings){
		List<String> md5Strings = new ArrayList<>();
		for (String fhirPathString: fhirPathStrings){
			String md5Bytes = md5Bytes(fhirPathString.getBytes(StandardCharsets.UTF_8));
			md5Strings.add(md5Bytes);
		}
		return md5Strings;
	}

	public static List<String> getStringListFromFhirPathList(List<FhirPath> fhirPaths) {
		List<String> fhirPathStrings = new ArrayList<>();
		for (FhirPath fhirPath : fhirPaths) {
			String fhirPathString = "";
			if (fhirPath.getOperand().isEmpty()) {
				fhirPathStrings.add(fhirPathString);
			} else if (fhirPath.getOperand().size() == 1) {
				fhirPathStrings.add(fhirPath.getOperand().get(0));
			} else if (fhirPath.getOperator() == null && fhirPath.getOperand().size() > 1) {
				throw new IllegalArgumentException("Multiple Operands passed without operator");
			} else {
				fhirPathString = fhirPath.getOperator() + "," + String.join(",", fhirPath.getOperand());
				fhirPathStrings.add(fhirPathString);
			}
		}
		return fhirPathStrings;
	}

	public static String getMd5KeyForLineCacheMd5(String fhirPath, Integer lineId, Integer chartId) {
		String combinedString = fhirPath+"_"+lineId.toString()+"_"+chartId.toString();
		return  md5Bytes(combinedString.getBytes(StandardCharsets.UTF_8));
	}
	public static String getMd5KeyForLineCacheMd5WithCategory(String fhirPath, Integer lineId, Integer chartId, String categoryId) {
		String combinedString = fhirPath+"_"+lineId.toString()+"_"+chartId.toString()+"_"+categoryId;
		return  md5Bytes(combinedString.getBytes(StandardCharsets.UTF_8));
	}
	public static byte[] getBytesFromFile(String filePath)  {
		byte[] bFile = null;
		try {
			File file = new File(filePath);
			int size = (int) file.length();
			bFile = new byte[size];
			try {
				BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
				buf.read(bFile, 0, bFile.length);
				buf.close();
			} catch (FileNotFoundException e) {
				logger.warn(ExceptionUtils.getStackTrace(e));
			}
		} catch (IOException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return bFile;
	}

	public static String getGUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public static String getShortIDFromGUID(String guid) {
		if (guid.length() >= 36) {
			String strNew = guid.replace("-", "");
			if (strNew.length() >= 32) guid = strNew;
		}
		String bin = guidToBinaryString(guid);
		String shortID = generateShortBase32(bin);
		return shortID.substring(0, 24);
	}

	static String guidToBinaryString(String guid) {
		int i = 0;
		StringBuilder ret = new StringBuilder();
		char[] hexdec = new char[33];
		String md5 = md5(guid);
		hexdec = md5.toCharArray();
		while (i < md5.length()) {
			switch (hexdec[i]) {
				case '0':
					ret.append("0000");
					break;
				case '1':
					ret.append("0001");
					break;
				case '2':
					ret.append("0010");
					break;
				case '3':
					ret.append("0011");
					break;
				case '4':
					ret.append("0100");
					break;
				case '5':
					ret.append("0101");
					break;
				case '6':
					ret.append("0110");
					break;
				case '7':
					ret.append("0111");
					break;
				case '8':
					ret.append("1000");
					break;
				case '9':
					ret.append("1001");
					break;
				case 'A':
				case 'a':
					ret.append("1010");
					break;
				case 'B':
				case 'b':
					ret.append("1011");
					break;
				case 'C':
				case 'c':
					ret.append("1100");
					break;
				case 'D':
				case 'd':
					ret.append("1101");
					break;
				case 'E':
				case 'e':
					ret.append("1110");
					break;
				case 'F':
				case 'f':
					ret.append("1111");
					break;
				default:
					System.out.print("\nInvalid hexadecimal digit " + hexdec[i]);
			}
			i++;
		}
		return ret.toString();
	}

	public static String md5(final String s) {
		final String MD5 = "MD5";
		try {
			MessageDigest digest = java.security.MessageDigest
				.getInstance(MD5);
			digest.update(s.getBytes());
			byte[] messageDigest = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return "";
	}

	static String generateShortBase32(String binaryString) {
		StringBuilder base32Str = new StringBuilder();
		while (binaryString.length() > 5) {
			String fiveBitData = (binaryString.substring(0, 5));
			base32Str.append(fiveBitDataToBase32(fiveBitData));
			binaryString = binaryString.substring(5);
		}
		return base32Str.toString();
	}

	static String fiveBitDataToBase32(String inp) {
		String b = "123456789ABCDEFGHJKLMNPQRSTVWXYZ";
		int index = -1;
		String ret = "";
		switch (inp) {
			case "0":
			case "00":
			case "000":
			case "0000":
			case "00000":
				index = 0;
				break;
			case "1":
			case "01":
			case "001":
			case "0001":
			case "00001":
				index = 1;
				break;
			case "10":
			case "010":
			case "0010":
			case "00010":
				index = 2;
				break;
			case "11":
			case "011":
			case "0011":
			case "00011":
				index = 3;
				break;
			case "100":
			case "0100":
			case "00100":
				index = 4;
				break;
			case "101":
			case "0101":
			case "00101":
				index = 5;
				break;
			case "110":
			case "0110":
			case "00110":
				index = 6;
				break;
			case "111":
			case "0111":
			case "00111":
				index = 7;
				break;
			case "1000":
			case "01000":
				index = 8;
				break;
			case "1001":
			case "01001":
				index = 9;
				break;
			case "1010":
			case "01010":
				index = 10;
				break;
			case "1011":
			case "01011":
				index = 11;
				break;
			case "1100":
			case "01100":
				index = 12;
				break;
			case "1101":
			case "01101":
				index = 13;
				break;
			case "1110":
			case "01110":
				index = 14;
				break;
			case "1111":
			case "01111":
				index = 15;
				break;
			case "10000":
				index = 16;
				break;
			case "10001":
				index = 17;
				break;
			case "10010":
				index = 18;
				break;
			case "10011":
				index = 19;
				break;
			case "10100":
				index = 20;
				break;
			case "10101":
				index = 21;
				break;
			case "10110":
				index = 22;
				break;
			case "10111":
				index = 23;
				break;
			case "11000":
				index = 24;
				break;
			case "11001":
				index = 25;
				break;
			case "11010":
				index = 26;
				break;
			case "11011":
				index = 27;
				break;
			case "11100":
				index = 28;
				break;
			case "11101":
				index = 29;
				break;
			case "11110":
				index = 30;
				break;
			case "11111":
				index = 31;
				break;
			default:
				System.out.print("\nInvalid base32 digit " + index);
		}
		if (index >= 0) ret = b.charAt(index) + "";
		return ret;
	}

	public static String getBase32CharFromMd5(String s) {
		try {
			//To upper
			s = s.toUpperCase();
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			String hexString = new String();
			int b = 0xF8 & messageDigest[0]; // upper nibble
			b = b >> 3;
			String bin = Integer.toBinaryString(b);
			hexString = fiveBitDataToBase32(bin);
			return hexString.toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
		return "";
	}

	public static String getOpenLinkJsonFormattedString(
		String baseUrl,
		String openCampLinkIdGUID,
		String openCampLinkId,
		String campaignGuid,
		String campaignDescription,
		String campaignURL,
		String anonymizedLocation,
		String locationPrecision,
		String anonymizedTimeEpoch,
		String humanReadableTime,
		String timePrecision,
		String verticalCode,
		String verticalDescription,
		String userDefinedDetails,
		boolean humanReadableFlag
	) {

		JsonParser jsonParser = new JsonParser();
		JsonObject openCampLinkJsonObject = new JsonObject();
		openCampLinkJsonObject.addProperty("id", openCampLinkIdGUID);
		openCampLinkJsonObject.addProperty("l", anonymizedLocation);
		openCampLinkJsonObject.addProperty("lp", locationPrecision);
		openCampLinkJsonObject.addProperty("t", anonymizedTimeEpoch);
		openCampLinkJsonObject.addProperty("tp", timePrecision);
		openCampLinkJsonObject.addProperty("cid", campaignGuid);
		openCampLinkJsonObject.addProperty("vc", verticalCode);
		openCampLinkJsonObject.addProperty("curl", campaignURL);
		openCampLinkJsonObject.addProperty("cname", campaignDescription);
		openCampLinkJsonObject.add("udf", jsonParser.parse(userDefinedDetails));

		String openCampLinkSuffix =
			"ID      : " + openCampLinkId + "\n" +
				"Version : 1\n" +
				"Location: " + anonymizedLocation + "\n" +
				"Date    : " + humanReadableTime + "\n" +
				"Campaign: " + campaignDescription + "\n" +
				campaignURL + "\n" +
				"Vertical: " + verticalDescription;

		String base64String = Base64.getEncoder().encodeToString(zipStringData(openCampLinkJsonObject.toString()));
		String finalQrString = baseUrl + "?s=" + openCampLinkId + "&v=3&d=" + base64String;
		if(humanReadableFlag) {
			finalQrString += "\0\n" + openCampLinkSuffix;
		}
		return finalQrString;
	}

	public static byte[] zipStringData(String s) {
		if (s == null || s.length() == 0)
			return null;
		try {
			ByteArrayOutputStream obj = new ByteArrayOutputStream(s.length());
			GZIPOutputStream gzip = new GZIPOutputStream(obj);
			gzip.write(s.getBytes());
			gzip.flush();
			gzip.close();
			return obj.toByteArray();
		} catch (IOException e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	public static StringBuilder formatTimeDifferenceToHumanReadableString(long days, long hours, long minutes, long seconds) {
		StringBuilder lastSync = new StringBuilder();
		if (days > 0) {
			lastSync.append(days).append(" day").append(days > 1 ? "s" : "");
			if(hours > 0){
				lastSync.append(", ").append(hours).append(" hour").append(hours > 1 ? "s" : "");
			}
		}
		else if (hours > 0) {
			lastSync.append(hours).append(" hour").append(hours > 1 ? "s" : "");
			if(minutes > 0){
				lastSync.append(", ").append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
			}
		} else if (minutes > 0) {
			lastSync.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
			if(seconds > 0){
				lastSync.append(", ").append(seconds).append(" second").append(seconds > 1 ? "s" : "");
			}
		} else {
			if(seconds > 0){
				lastSync.append(seconds).append(" second").append(seconds > 1 ? "s" : "");
			}
		}
		return lastSync.append(" ago");
	}

	public static String calculateAndFormatTimeDifference(Timestamp oldestTimestamp) {
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		// Calculate the time difference in milliseconds
		long timeDifferenceMillis = currentTimestamp.getTime() - oldestTimestamp.getTime();

		// Calculate the number of days, hours, minutes, and seconds
		long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);
		long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis) % 24;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis) % 60;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis) % 60;

		// Format the result based on criteria
		if (days > 0 || hours > 0 || minutes > 0 || seconds > 0) {
			return (formatTimeDifferenceToHumanReadableString(days, hours, minutes, seconds).toString());
		}
		return "Not found";
	}


	public static boolean noneMatchDates(List<Date> dates, Date currentDate) {
		LocalDate currentLocalDate = currentDate.toLocalDate();
		return dates.stream()
			.map(Date::toLocalDate)
			.noneMatch(date -> date.equals(currentLocalDate));
	}

}
