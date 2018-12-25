/**
 * 
 */
package com.ningze.entity;

/**
 * @author ptero
 *
 */
public final class FieldEntity {

	/**
	 * name
	 */
	private String name;

	/**
	 * type
	 */
	private String type;

	/**
	 * nullAble
	 */
	private boolean nullAble;

	/**
	 * 注释
	 */
	private String comment;

	/**
	 * 格式
	 */
	private String pattern;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the nullAble
	 */
	public boolean isNullAble() {
		return nullAble;
	}

	/**
	 * @param nullAble the nullAble to set
	 */
	public void setNullAble(boolean nullAble) {
		this.nullAble = nullAble;
	}

	/**
	 * @return the pattern
	 */
	public final String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public final void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
