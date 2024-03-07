package ch.ahdis.matchbox.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.binary.svc.NullBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.model.dao.JpaPid;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.util.BinaryUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.instance.model.api.IBaseBinary;
import org.hl7.fhir.r5.model.Element;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.PrimitiveType;

import java.io.IOException;

/**
 * A utility class for Matchbox Server.
 *
 * @author Quentin Ligier
 **/
public class MatchboxServerUtils {

	/**
	 * This class is not instantiable.
	 */
	private MatchboxServerUtils() {
	}

	/**
	 * Helper method to retrieve a Binary from its ID.
	 *
	 * @param binaryId 		the ID of the Binary
	 * @param myDaoRegistry the DAO registry
	 * @return the Binary
	 */
	public static IBaseBinary getBinaryFromId(final long binaryId,
													      final DaoRegistry myDaoRegistry) {
		return (IBaseBinary) myDaoRegistry.getResourceDao("Binary").readByPid(JpaPid.fromId(binaryId));
	}

	/**
	 * Helper method which will attempt to use the IBinaryStorageSvc to resolve the binary blob if available. If the bean
	 * is unavailable, fallback to assuming we are using an embedded base64 in the data element.
	 *
	 * @param theBinary          the Binary who's `data` blob you want to retrieve
	 * @param myBinaryStorageSvc the binary storage service
	 * @param myCtx              the FHIR context
	 * @return a byte array containing the blob.
	 * @throws IOException
	 */
	public static byte[] fetchBlobFromBinary(final IBaseBinary theBinary,
														  final @Nullable IBinaryStorageSvc myBinaryStorageSvc,
														  final FhirContext myCtx) throws IOException {
		if (myBinaryStorageSvc != null && !(myBinaryStorageSvc instanceof NullBinaryStorageSvcImpl)) {
			return myBinaryStorageSvc.fetchDataBlobFromBinary(theBinary);
		} else {
			byte[] value = BinaryUtil.getOrCreateData(myCtx, theBinary).getValue();
			if (value == null) {
				throw new InternalErrorException(
					Msg.code(1296) + "Failed to fetch blob from Binary/" + theBinary.getIdElement());
			}
			return value;
		}
	}

	/**
	 * A helper to add an R5 extension to an element only if its value is non-null.
	 */
	public static void addExtension(final Element element,
											  final String url,
											  final @Nullable PrimitiveType<?> value) {
		if (value != null && value.getValue() != null) {
			element.addExtension(url, value);
		}
	}
}
