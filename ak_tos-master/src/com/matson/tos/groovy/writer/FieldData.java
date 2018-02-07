/**
 * 
 */
package com.matson.tos.groovy.writer;

/**
 * @author JZF
 *
 */
public class FieldData 
{
	private String _fieldName;
	private String _msgTag;
	private String _dataType;
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
	public void setMsgTag( String msgTag) {
		_msgTag = msgTag;
	}
	public String getMsgTag() {
		return _msgTag;
	}
	public void setDataType( String dataType) {
		_dataType = dataType;
	}
	public String getDataType() {
		return _dataType;
	}
	
}
