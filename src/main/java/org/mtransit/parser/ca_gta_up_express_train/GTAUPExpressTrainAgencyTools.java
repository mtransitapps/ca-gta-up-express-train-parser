package org.mtransit.parser.ca_gta_up_express_train;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.mt.data.MTrip;

// https://www.gotransit.com/en/information-resources/software-developers
// https://www.gotransit.com/fr/ressources-informatives/dveloppeurs-de-logiciel
// https://www.gotransit.com/static_files/gotransit/assets/Files/UP-GTFS.zip
public class GTAUPExpressTrainAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-gta-up-express-train-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new GTAUPExpressTrainAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating UP Express train data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating UP Express train data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_TRAIN;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public long getRouteId(GRoute gRoute) {
		Matcher matcher = DIGITS.matcher(gRoute.getRouteId());
		if (matcher.find()) {
			return Long.parseLong(matcher.group());
		}
		if ("UP".equals(gRoute.getRouteId())) {
			return 0L;
		}
		System.out.printf("\nUnexpected route ID %s\n", gRoute);
		System.exit(-1);
		return -1L;
	}

	private static final String AGENCY_COLOR_BROWN = "555025"; // BROWN (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BROWN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 0L) {
			if (Arrays.asList( //
					"Weston GO/UP", //
					"Pearson Airport" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pearson Airport", mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnepected trips to merge %s & %s\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern AEROPORT = Pattern.compile("(a[e|é]roport)", Pattern.CASE_INSENSITIVE);
	private static final Pattern GARE = Pattern.compile("(gare)", Pattern.CASE_INSENSITIVE);

	private static final Pattern UP_EXPRESS = Pattern.compile("(UP Express )", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = AEROPORT.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = GARE.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = UP_EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STATION.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern STATION = Pattern.compile("(station)", Pattern.CASE_INSENSITIVE);

	private static final Pattern UP_EXPRESS_GO = Pattern.compile("(up express|up|go[/]?)", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STATION.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = UP_EXPRESS_GO.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	private static final String STOP_CODE_WESTON = "WE";
	private static final int STOP_ID_WESTON = 10000;
	private static final String STOP_CODE_UNION = "UN";
	private static final int STOP_ID_UNION = 10001;
	private static final String STOP_CODE_PEARSON = "PA";
	private static final int STOP_ID_PEARSON = 10002;
	private static final String STOP_CODE_BLOOR = "BL";
	private static final int STOP_ID_BLOOR = 10003;

	@Override
	public int getStopId(GStop gStop) {
		if (STOP_CODE_WESTON.equals(gStop.getStopId())) {
			return STOP_ID_WESTON;
		} else if (STOP_CODE_UNION.equals(gStop.getStopId())) {
			return STOP_ID_UNION;
		} else if (STOP_CODE_PEARSON.equals(gStop.getStopId())) {
			return STOP_ID_PEARSON;
		} else if (STOP_CODE_BLOOR.equals(gStop.getStopId())) {
			return STOP_ID_BLOOR;
		}
		System.out.println("Unexpected stop ID " + gStop);
		System.exit(-1);
		return -1;
	}
}