/**
 * A URI.java is a class with constructors and methods to return
 * the basic attributes of URI : scheme, user, host, port, path, fragment, query.
 * 
 * Throws in error if the format of URI is not correct 
 * (i.e) URI is passed in String format.
 * 
 * The regular expressions and examples used in this coding paradigm are attributed
 * from:
 * --> https://www.ietf.org/rfc/rfc2396.txt
 * --> https://en.wikipedia.org/wiki/Uniform_Resource_Identifier
 * --> https://docs.oracle.com/javase/7/docs/api/java/net/class-use/URI.html#java.net
 * 
 *  @author Aditya Chauhan(chauhanaditya3628@gmail.com);
 * 
 */

import java.net.URISyntaxException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

public final class URI implements Serializable{
	
	/**
	 * Regular expression for parsing URI. Doesn't parse IPv6 addresses.
	 * Based on RFC 2396, link in description above.
	 */
	
	private static final String URI_regexp = 
			"^(([^:/?#]+):)?((//([^/?#]*))?([^?#]*)(\\?([^#]*))?)?(#(.*))?";
	
	/**
	 * Regular expression for parsing Authority segment to extract
	 * userinfo, host, port.
	 */
	
	private static final String Auth_regexp = 
			"(([^?#]*)@)?([^?#:]*)(:([0-9]*))?";
	
	
	/**
	 * Regular expression for valid characters.
	 * Taken from RFC 2396, link in description on the top.
	 */
	
	private static final String Digits = "0123456789";
	private static final String Lower_Alpha = "abcdefghijklmnopqrstuvwxyz";
	private static final String Upper_Alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String Alpha = Lower_Alpha + Upper_Alpha;
	private static final String Alpha_num = Alpha + Digits;
	private static final String Unreserved = Alpha_num + "-._~";
	private static final String Delimitars = "!$&'()*+,;=";
	private static final String Regex_name = Unreserved + Delimitars + "%";
	private static final String Pchar = Unreserved + Delimitars + ":@%";
	private static final String Segment = Pchar;
	private static final String Path_Segment = Segment + "/";
	private static final String ssp = Pchar + "?/";
	private static final String host = Regex_name + "[]";
	private static final String user_info = Regex_name + ":";
	private static final String hex = "0123456789ABCDEF";
	
	/**
	 * Index assignmed to differnt components in parsed URI.
	 */
	private static final int scheme = 2;
	private static final int scheme_spec = 3;
	private static final int authority = 5;
	private static final int path = 6;
	private static final int query = 8;
	private static final int fragment = 10;
	
	/**
	 * Index assigned to different components 
	 * in parsed Authority segment of input URI
	 */
	
	private static final int auth_user_info = 2;
	private static final int auth_host = 3;
	private static final int auth_port = 5;
	
	/**
	 * Variable declaration to store compiled versions of URI and Authority segments
	 */
	
	private static final Pattern URI_Pattern;
	private static final Pattern Authority_Pattern;
	
	
	
	private transient String Scheme;
	private transient String RawSchemeSpec;
	private transient String SchemeSpec;
	private transient String RawAuthority;
	private transient String Authority;
	private transient String RawUserInfo;
	private transient String UserInfo;
	private transient String RawHost;
	private transient String Host;
	private transient String RawPath;
	private transient String Path;
	private transient String RawQuery;
	private transient String Query;
	private transient String RawFragment;
	private transient String Fragment;
	private transient int Port = -1;
	private String str;
	
	/**
	 * Initializer to pre-compile the regular expressions.
	 */
	static {
		URI_Pattern = Pattern.compile(URI_regexp);
		Authority_Pattern = Pattern.compile(Auth_regexp);
	}
	
	/**
	 * @param match the matcher, which is used to match the regular expression
	 * to the input URI and saves the result.
	 * 
	 * @return either the matched content or null for the undefiend values.
	 */

private static String getURI(Matcher match, int group) {
	String matched = match.group(group);
	if(matched==null || matched.length()==0) {
		String prev_match = match.group(group-1);
		if(prev_match==null || prev_match.length()==0)
			return null;
		else
			return "";
	}
	return matched;
}



/**
 * Assign values to all the desired parameters of the URI.
 * 
 * @param 'st' is a URI string to be parsed.
 * @exception URISyntaxException if the given string is not in accordance with 
 * the initialized URI regular expression.
 */

private void parseURI(String st) throws URISyntaxException{
	Matcher match = URI_Pattern.matcher(str);
	if(match.matches()) {
		Scheme = getURI(match, scheme);
		RawSchemeSpec = match.group(scheme_spec);
		SchemeSpec = unquote(RawSchemeSpec);
		if(!isOpaque()) {
			RawAuthority = getURI(match, authority);
			RawPath = match.group(path);
			RawQuery = getURI(match, query);
		}
		RawFragment = getURI(match, fragment);
	}
	else {
		throw new URISyntaxException(st, "string don't match URI regular expression");
	}
	parseAuthority();
	Authority = unquote(RawAuthority);
	UserInfo = unquote(RawUserInfo);
	Host = unquote(RawHost);
	Path = unquote(RawPath);
	Query = unquote(RawQuery);
	Fragment = unquote(RawFragment);
}
	
/**
 * @param st is the string that is supposed to be unquoted.
 * @return return the unquoted string or null if it was null.
 * @exception URISyntaxException if the given string contains invalid
 * characters as per the intilization of the parameter's charaters above
 */
	private static String unquote(String st) throws URISyntaxException{
		if(st==null) {
			return null;
		}
		byte[] b = new byte[st.length()];
		int pos = 0;
		for(int i=0;i<st.length();i++) {
			char c = st.charAt(i);
			if(c=='%') {
				if(i+2 >= st.length())
					throw new URISyntaxException(st, "Invalid character is quoted here");
				int high = Character.digit(st.charAt(++i), 16);
				int low = Character.digit(st.charAt(++i), 16);
				if(low < 0 || high < 0)
					throw new URISyntaxException(st, "Invalid character is quoted here");
				b[pos++] = (byte) (high*16 + low);
			}
			else {
				b[pos++] = (byte) c;
			}
		}
		try {
			return new String(b,0,pos,"utf-8");
		}catch(java.io.UnsupportedEncodingException x2){
			throw (Error) new InternalError().initCause(x2);
		}
	}
	
	/**
	 * Creates URI based on th given string input for URI.
	 * 
	 * @param st the string to create URI from.
	 * @exception URISyntaxException if the given string is not
	 * according to RFC 2396 rules of regular expression for URI.
	 */
	
	public URI(String st) throws URISyntaxException{
		this.str = st;
		parseURI(st);
	}
	
	
	/**
	 * Used to parse the authority component of the URI string.
	 * 
	 * @return the URI with authority section parsed into 
	 * userinfo, host, port.
	 * 
	 * @throws URISyntaxException if the given string is not according
	 * to the initialized URI regular expression.
	 */
	public URI parseAuthority() throws URISyntaxException{
		if(RawAuthority != null) {
			Matcher match = Authority_Pattern.matcher(RawAuthority);
			if(match.matches()) {
				RawUserInfo = getURI(match, auth_user_info);
				RawHost = getURI(match, auth_host);
				String port_str = getURI(match, auth_port);
				if(port_str != null && !port_str.isEmpty()) {
					try {
						Port = Integer.parseInt(port_str);
					}catch(NumberFormatException e){
						URISyntaxException use = 
								new URISyntaxException(str, "Doesn't match URI Regex");
						use.initCause(e);
						throw use;
					}
				}
			}
			else throw new URISyntaxException(str, "Doesn't match URI Regex");
		}
		return this;
	}
	
	/**
	* Returns the scheme of the URI
	*/
	
	public String getScheme() {
		return Scheme;
	}
	
	/**
	* Returns if URI is absolute or not
	*/
	
	public boolean isAbsolute() {
		return Scheme != null;
	}
	
	/**
	* Returns if URI is opaque or not
	*/
	
	public boolean isOpaque() {
		return ((Scheme != null) && !(SchemeSpec.startsWith("/")));
	}
	
	/**
	* Returns the UserInfo of the URI
	*/
	
	public String getUserInfo() {
		return UserInfo;
	}
	
	/**
	* Returns the Host of the URI
	*/
	
	public String getHost() {
		return Host;
	}
	
	/**
	* Returns the Port of the URI
	*/
	
	public int getPort() {
		return Port;
	}
	
	/**
	* Returns the Query of the URI
	*/
	
	public String getQuery() {
		return Query;
	}
	
	/**
	* Returns the Fragment of the URI
	*/
	
	public String getFragment() {
		return Fragment;
	}
	
	/**
	* Returns the Path of the URI
	*/
	
	public String get_Path() {
		return Path;
	}
	
	/**
	 * Main Function
	 */
	
	public static void main(String[] args) throws Exception{
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter URI string : ");
		System.out.println();
		String uri_string = sc.next();
		URI uri = new URI(uri_string);
		System.out.println("=============================================");
		System.out.println();
		System.out.println("==============URI PARAMETERS=================");
		System.out.println();
		System.out.println("Scheme : " + uri.getScheme());
		System.out.println("Host : " + uri.getHost());
		System.out.println("User : " + uri.getUserInfo());
		System.out.println("Query : " + uri.getQuery());
		System.out.println("Port : " + uri.getPort());
		System.out.println("Fragment : " + uri.getFragment());
		System.out.println("Path : " + uri.get_Path());
		System.out.println();
		System.out.println("=============================================");
			
	}
	
}