package com.tongqu.jmeter.protocol.slots.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.tongqu.base.msg.MsgPack;
import com.tongqu.base.msg.NetCmdClient;
import com.tongqu.jmeter.protocol.slots.network.ReqMsg;
import com.tongqu.jmeter.protocol.slots.network.RespMsg;
import com.tongqu.jmeter.protocol.slots.network.SlotsClient;

public class LoginAction implements JavaSamplerClient {

	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private SlotsClient client;
	private SampleResult results;
	
	@Override
	public Arguments getDefaultParameters() {
		//JMeterContext jmctx = JMeterContextService.getContext();
		//JMeterVariables vars = jmctx.getVariables();
		//vars.get("userId");
		//vars.get("nickName");
		//vars.get("password");
		
		Arguments params = new Arguments();
		params.addArgument("userId", "${userId}");
		params.addArgument("nickName", "${nickName}");
		params.addArgument("password", "${password}");
		params.addArgument("ip", "10.10.0.110");
		params.addArgument("port", "65002");
		return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext ctx) {
		int userId = ctx.getIntParameter("userId");
		String nickName= ctx.getParameter("nickName");
		MsgPack pack = client.request(new LoginReqMsg("android-dd" + userId, nickName, "666666", 0x03000000, "", "", String.valueOf(userId)));
		if (pack == null) {
			log.info(userId + "登录失败!");
			results.setSuccessful(false);
		} else {
			log.info(userId + "登录成功.");
			results.setSuccessful(true);
		}
		return results;
	}

	@Override
	public void setupTest(JavaSamplerContext ctx) {
		String ip = ctx.getParameter("ip");
		int port = ctx.getIntParameter("port");
		client = new SlotsClient();
		results = new SampleResult();
		
		client.connect(ip, port);
		
		JMeterContext jmctx = JMeterContextService.getContext();
		jmctx.getVariables().putObject("SlotsClient", client);
	}

	@Override
	public void teardownTest(JavaSamplerContext ctx) {
	}
	
	
	
	class LoginReqMsg extends ReqMsg {
		private final String imei;
		private final String nickName;
		private final String password;
		private final int platVerCode;
		private final String channelID;
		private final int platFromCode = 1;
		private final int nGameID = 1;
		private final String phoneType;
		private final String userID;
		public LoginReqMsg(String imei, String nickName, String password, int platVerCode, String channelID, String phoneType, String userID) {
			this.imei = imei;
			this.nickName = nickName;
			this.password = password;
			this.platVerCode = platVerCode;
			this.channelID = channelID;
			this.phoneType = phoneType;
			this.userID = userID;
		}
		@Override
		protected void writeParamTo(MsgPack msgPack) {
			msgPack.putText(imei);
			msgPack.putText(nickName);
			msgPack.putText(password);
			msgPack.putInt(platVerCode);
			msgPack.putText(channelID);
			msgPack.putByte(platFromCode);
			msgPack.putByte(nGameID);
			msgPack.putText(phoneType);
			msgPack.putInt(3);
			//msgPack.putText(userID);
		}
		@Override
		public int getMsgId() {
			return NetCmdClient.BASEID_LOGIN;
		}
	}
	class LoginRespMsg extends RespMsg {

		private int userId;
		private byte result;
		private String resultTxt;
		private byte isChanged;
		private String sNickName;
		private String sPhotoUrl;
		private long lCoin;
		private int unReadMsgCnt;
		private long sessionId;
		private int vipLevel;
		private int yuanbao;
		private byte[] ab;
		private int thirdPartPlatId;

		public int getUserId() {
			return userId;
		}

		public byte getResult() {
			return result;
		}

		public String getResultTxt() {
			return resultTxt;
		}

		public byte getIsChanged() {
			return isChanged;
		}

		public String getsNickName() {
			return sNickName;
		}

		public String getsPhotoUrl() {
			return sPhotoUrl;
		}

		public long getlCoin() {
			return lCoin;
		}

		public int getUnReadMsgCnt() {
			return unReadMsgCnt;
		}

		public long getSessionId() {
			return sessionId;
		}

		public int getVipLevel() {
			return vipLevel;
		}

		public int getYuanbao() {
			return yuanbao;
		}

		public byte[] getAb() {
			return ab;
		}

		public int getThirdPartPlatId() {
			return thirdPartPlatId;
		}

		@Override
		protected void readParamFrom(MsgPack msgPack) {
			userId = msgPack.getInt();
			result = msgPack.getByte();
			resultTxt = msgPack.getText();
			isChanged = msgPack.getByte();
			sNickName = msgPack.getText();
			sPhotoUrl = msgPack.getText();
			lCoin = msgPack.getLong();
			unReadMsgCnt = msgPack.getInt();
			sessionId = msgPack.getLong();
			vipLevel = msgPack.getInt();
			yuanbao = msgPack.getInt();
			ab = msgPack.getByteArray();
			thirdPartPlatId = msgPack.getInt();
		}

	}
}
