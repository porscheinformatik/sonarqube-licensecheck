package at.porscheinformatik.sonarqube.licensecheck.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
/**
 * This class is taken from stackoverflow
 * https://stackoverflow.com/a/53296095/12066835
 * the author there is SimoV8
 */
public class ExtendedParser extends DefaultParser {

	private final ArrayList<String> notParsedArgs = new ArrayList<>();

	public String[] getNotParsedArgs() {
		return notParsedArgs.toArray(new String[notParsedArgs.size()]);
	}

	@Override
	public CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption) throws ParseException {
		if (stopAtNonOption) {
			return parse(options, arguments);
		}
		List<String> knownArguments = new ArrayList<>();
		notParsedArgs.clear();
		boolean nextArgument = false;
		for (String arg : arguments) {
			if (options.hasOption(arg) || nextArgument) {
				knownArguments.add(arg);
			} else {
				notParsedArgs.add(arg);
			}

			nextArgument = options.hasOption(arg) && options.getOption(arg).hasArg();
		}
		return super.parse(options, knownArguments.toArray(new String[knownArguments.size()]));
	}

}
