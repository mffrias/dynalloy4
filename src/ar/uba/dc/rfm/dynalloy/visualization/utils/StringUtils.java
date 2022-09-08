package ar.uba.dc.rfm.dynalloy.visualization.utils;

public class StringUtils {

	public static String indent(int level) {
		StringBuilder sb = new StringBuilder();
		while (level-- > 0) {
			sb.append("\t");
		}
		
		return sb.toString();
	}
	
}
