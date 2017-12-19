package com.tongqu.jmeter.protocol.slots.network;

import com.tongqu.base.msg.MsgPack;

public abstract class RespMsg {
	public void fromMsgPack(MsgPack msgPack) {
		readParamFrom(msgPack);
	}

	/** 子类读取自己的参数 */
	protected abstract void readParamFrom(MsgPack msgPack);
}
