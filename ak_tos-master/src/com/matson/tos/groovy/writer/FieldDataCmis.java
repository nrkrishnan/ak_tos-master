/**
 * 
 */
package com.matson.tos.groovy.writer;

/**
 * @author JZF
 *
 */
public class FieldDataCmis 
{
	private String _fieldName;
	private int _sequenceNum;
	private int _fieldSize;
	
	
	public void setFieldSize( int fieldSize) {
		_fieldSize = fieldSize;
	}
	public int getFieldSize() {
		return _fieldSize;
	}
	public void setSequenceNum(int sequenceNum){
		_sequenceNum = sequenceNum;
	}
	public int getSequenceNum(){
		return _sequenceNum;
	}
	public void setFieldName( String fieldName) {
		_fieldName = fieldName;
	}
	public String getFieldName() {
		return _fieldName;
	}
	
}
