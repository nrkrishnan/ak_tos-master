/**
 * 
 */
package com.matson.tos.groovy.formatter;

/**
 * @author JZF
 *
 */
public interface EmailFormatter {
	public String getContent();
	public String getToEmailAddr();
	public String getFromEmailAddr();
	public String getSubject();
	public void   logMsg(String contents);
}
