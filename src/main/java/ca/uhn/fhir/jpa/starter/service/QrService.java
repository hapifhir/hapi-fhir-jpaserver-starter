package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.model.OCLQrRequest;
import ca.uhn.fhir.jpa.starter.model.OCLQrResponse;
import com.iprd.fhir.utils.QrReceipt;
import com.iprd.fhir.utils.Utils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.iprd.fhir.utils.Utils.SHORT_ID_LENGTH;

@Service
public class QrService {

	public OCLQrResponse getOclQr(OCLQrRequest oclQrRequest) {
		String guid = Utils.getGUID();
		String openCampLinkId = Utils.getShortIDFromGUID(guid).substring(0, SHORT_ID_LENGTH - 1);
		String suffix = Utils.getBase32CharFromMd5(openCampLinkId);
		openCampLinkId = openCampLinkId + suffix;

		long currentTimeEpoch = System.currentTimeMillis() / 1000;
		String date = new SimpleDateFormat("MMMM yyyy").format(new Date());

		String qrString = Utils.getOpenLinkJsonFormattedString(
			oclQrRequest.getBaseUrl(),
			guid,
			openCampLinkId,
			oclQrRequest.getCampGuid(),
			oclQrRequest.getCampName(),
			oclQrRequest.getCampUrl(),
			oclQrRequest.getLocation(),
			oclQrRequest.getLocationPre(),
			String.valueOf(currentTimeEpoch),
			date,
			oclQrRequest.getTimePre(),
			oclQrRequest.getVerticalCode(),
			oclQrRequest.getVerticalDescription(),
			oclQrRequest.getUserDefinedData(),
			oclQrRequest.isHumanReadableFlag()
		);
		byte[] qrBytes = QrReceipt.generateQrReceipt(qrString, oclQrRequest.getCampName(), date, openCampLinkId, oclQrRequest.getErrorCorrectionLevelBits());
		return new OCLQrResponse(openCampLinkId, qrBytes);
	}
}
