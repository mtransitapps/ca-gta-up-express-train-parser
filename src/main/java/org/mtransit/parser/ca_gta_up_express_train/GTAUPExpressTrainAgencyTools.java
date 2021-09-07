package org.mtransit.parser.ca_gta_up_express_train;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mtransit.commons.StringUtils.EMPTY;

// https://www.gotransit.com/en/information-resources/software-developers
// https://www.gotransit.com/fr/ressources-informatives/dveloppeurs-de-logiciel
// https://www.gotransit.com/static_files/gotransit/assets/Files/UP-GTFS.zip
public class GTAUPExpressTrainAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new GTAUPExpressTrainAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "UP Express";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_TRAIN;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		Matcher matcher = DIGITS.matcher(routeId);
		if (matcher.find()) {
			return Long.parseLong(matcher.group());
		}
		if ("UP".equals(routeId)) {
			return 0L;
		}
		throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern AEROPORT = Pattern.compile("(a[e|é]roport)", Pattern.CASE_INSENSITIVE);
	private static final Pattern GARE = Pattern.compile("(gare)", Pattern.CASE_INSENSITIVE);

	private static final Pattern UP_EXPRESS = Pattern.compile("(UP Express )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = AEROPORT.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = GARE.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = UP_EXPRESS.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = STATION.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern STATION = Pattern.compile("(station)", Pattern.CASE_INSENSITIVE);

	private static final Pattern UP_EXPRESS_GO = Pattern.compile("(up express|up|go[/]?)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = STATION.matcher(gStopName).replaceAll(EMPTY);
		gStopName = UP_EXPRESS_GO.matcher(gStopName).replaceAll(EMPTY);
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
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId = gStop.getStopId();
		switch (stopId) {
		case STOP_CODE_WESTON:
			return STOP_ID_WESTON;
		case STOP_CODE_UNION:
			return STOP_ID_UNION;
		case STOP_CODE_PEARSON:
			return STOP_ID_PEARSON;
		case STOP_CODE_BLOOR:
			return STOP_ID_BLOOR;
		default:
			throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
		}
	}
}
