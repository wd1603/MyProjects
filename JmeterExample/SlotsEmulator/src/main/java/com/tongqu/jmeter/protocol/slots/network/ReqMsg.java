package com.tongqu.jmeter.protocol.slots.network;

import com.tongqu.base.msg.MsgPack;
import com.tongqu.base.msg.NetCmd;

public abstract class ReqMsg {
	public MsgPack toMsgPack() {
		MsgPack pack = new MsgPack(NetCmd.REQ | getMsgId());

		writeParamTo(pack);

		return pack;
	}

	/** 子类写入自己的属性 */
	protected abstract void writeParamTo(MsgPack msgPack);

	/** 具体的消息id */
	public abstract int getMsgId();
}
