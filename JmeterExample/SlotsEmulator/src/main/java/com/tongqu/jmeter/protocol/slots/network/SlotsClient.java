package com.tongqu.jmeter.protocol.slots.network;

import java.net.InetSocketAddress;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.tongqu.base.msg.MsgPack;
import com.tongqu.base.msg.NetCmd;
import com.tongqu.base.msg.codec.MsgCodecFactory;

public class SlotsClient {
	
	private static final Logger log = LoggingManager.getLoggerForClass();
	private NioSocketConnector connector;
	private IoSession session;
	
	public SlotsClient() {
		//初始化参数
		this.connector = new NioSocketConnector();//建立连接
		this.connector.setConnectTimeoutMillis(5000);
		this.connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 4);
		this.connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MsgCodecFactory()));
		this.connector.getSessionConfig().setUseReadOperation(true);
	}
	
	public void connect(String ip, int port) {
		ConnectFuture future = connector.connect(new InetSocketAddress(ip, port));
		boolean connected = future.awaitUninterruptibly(5000);
		if (connected) {
			session = future.getSession();
			log.info("连接成功.");
		} else {
			log.info("连接失败!");
		}
	}
	
	public MsgPack request(ReqMsg req) {
		req.toMsgPack().sendTo(session);
		long sendTime = System.currentTimeMillis();

		while (true) {
			ReadFuture readFuture = session.read();

			if (readFuture.awaitUninterruptibly(5000)) {
				Object msg = readFuture.getMessage();
				MsgPack pack = (MsgPack) msg;
				if (!(pack instanceof MsgPack)) {
					continue;
				}

				if ((pack.getMsgID() & 0x000000FF) == 0 && (pack.getMsgID()) != 0x80000000) {
					continue;
				}

				if (pack.getMsgID() == (NetCmd.ACK | req.getMsgId()))
					return pack;
			}

			if (System.currentTimeMillis() - sendTime > 10000) {
				return null;
			}
		}
	}
}
