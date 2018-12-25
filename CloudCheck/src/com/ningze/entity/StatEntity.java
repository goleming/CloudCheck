/**
 * 
 */
package com.ningze.entity;

/**
 * @author ptero
 *
 */
public final class StatEntity {

	/**
	 * 返回状态
	 */
	private boolean stat;

	/**
	 * 返回信息
	 */
	private String message;

	/**
	 * @return the stat
	 */
	public final boolean isStat() {
		return stat;
	}

	/**
	 * @param stat
	 *            the stat to set
	 */
	public final void setStat(boolean stat) {
		this.stat = stat;
	}

	/**
	 * @return the message
	 */
	public final String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public final void setMessage(String message) {
		this.message = message;
	}
}
