/*L
 *  Copyright SAIC, Ellumen and RSNA (CTP)
 *
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/national-biomedical-image-archive/LICENSE.txt for details.
 */

package gov.nih.nci.midi;

import java.util.Map;


public abstract class DomainOperation {

	
	/**
	 * The name of this method is a little bit mystifying.... it does far more than
	 * validate... it actually takes values parsed from the DICOM file and stores
	 * them into persistent domain objects that will ulitmately be saved...
	 * The return value is one of these domain objects.  If this method was defined
	 * a little more sensibly.... might make sense to make DomainOperation<T> where
	 * T would be return type for "validate".
	 */
	public Object validate(Map numbers) throws Exception {
		return null;
	}

}
