package javax.megaco.pkg.ToneDetPkg;

import javax.megaco.pkg.MegacoPkg;
import javax.megaco.pkg.PkgEventItem;

public class ToneStdEvent extends PkgEventItem {

	/**
	 * Identifies Start tone detect event of the MEGACO Tone Detect Package. Its
	 * value shall be set equal to 0x0001.
	 */
	public static final int TONE_DET_STD_EVENT = 0x0001;

	/**
	 * Constructs a Jain MEGACO Object representing Event Item of the MEGACO
	 * Package for Event Start Tone Detected and Package as ToneDet.
	 */
	public ToneStdEvent() {
		super();
		super.itemId = TONE_DET_STD_EVENT;
		super.eventId = TONE_DET_STD_EVENT;
		super.packageId = new ToneDetPkg();

	}

	/**
	 * This method is used to get the event identifier from an Event Item
	 * object. The implementations of this method in this class returns the id
	 * of the Start Tone Detect event of Tone Detect Package. * @return It shall
	 * return {@link TONE_DET_STD_EVENT}.
	 */
	public int getEventId() {

		return super.itemId;
	}

	/**
	 * This method is used to get the item identifier from an Item object. The
	 * implementations of this method in this class returns the id of the Start
	 * Tone Detect event of Tone Detect Package.
	 * 
	 * @return It shall return {@link TONE_DET_STD_EVENT}.
	 */
	public int getItemId() {

		return super.itemId;
	}

	/**
	 * This method gets the package to which the item belongs. Since the Start
	 * Tone Detect event is defined in the Tone Detect Package of MEGACO
	 * protocol, this method returns the ToneDetPkg class object .
	 * 
	 * @return The package is {@link ToneDetPkg}.
	 */
	public MegacoPkg getItemsPkgId() {
		return super.packageId;
	}

}
