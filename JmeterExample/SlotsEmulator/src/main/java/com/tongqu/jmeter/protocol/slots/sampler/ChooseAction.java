package com.tongqu.jmeter.protocol.slots.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.tongqu.base.msg.MsgPack;
import com.tongqu.base.msg.NetCmd;
import com.tongqu.base.msg.NetCmdClient;
import com.tongqu.jmeter.protocol.slots.network.ReqMsg;
import com.tongqu.jmeter.protocol.slots.network.RespMsg;
import com.tongqu.jmeter.protocol.slots.network.SlotsClient;

public class ChooseAction implements JavaSamplerClient {

	private static final Logger log = LoggingManager.getLoggerForClass();
	private static final String PARAM_OPTION_ID = "optionId";
	
	private ChooseReq req;
	private ChooseResp resp;
	private SampleResult results;
	
	private JMeterVariables vars;
	private SlotsClient client;
	private String userId;
	
	private int optionId;
	
	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();
		params.addArgument(PARAM_OPTION_ID, "2");
		return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext ctx) {
		//发送请求
		resp.readParamFrom(client.request(req));
		
		//修改状态
		vars.put("state", "1");
		log.info(userId + "choose成功, 下次开始spin.");
		
		//返回结果
		results.setSuccessful(true);
		return results;
	}

	@Override
	public void setupTest(JavaSamplerContext ctx) {
		this.req = new ChooseReq();
		this.resp = new ChooseResp();
		this.results = new SampleResult();
		this.vars = JMeterContextService.getContext().getVariables();
		this.client = (SlotsClient) this.vars.getObject("SlotsClient");
		this.userId = this.vars.get("userId");
		
		this.optionId = ctx.getIntParameter(PARAM_OPTION_ID);
	}

	@Override
	public void teardownTest(JavaSamplerContext ctx) {
		this.req = null;
		this.resp = null;
		this.results = null;
		this.vars = null;
		this.client = null;
		this.userId = null;
		this.optionId = -1;
	}

	private class ChooseReq extends ReqMsg {
		@Override
		public int getMsgId() {
			return NetCmd.REQ | NetCmdClient.SushiSlots.MINI_GAME_TURNING;
		}
		
		@Override
		protected void writeParamTo(MsgPack msgPack) {
			msgPack.putByte(1);//主游戏ID
			msgPack.putInt(1);//游戏版本号
			msgPack.putInt(1);//房间ID
			msgPack.putInt(optionId);//选择ID
			msgPack.putText("");//锦标赛ID
		}
	}
	private class ChooseResp extends RespMsg {
		int wheelSetId, freeTimes;
		long userCoin, addUpCoin;
		@Override
		protected void readParamFrom(MsgPack resp) {
			freeTimes = resp.getInt();// RemainFreeSpinCnt Int 剩余free spin数量
			//resp.getByte();// Result Byte 是否成功 0失败1成功
			//resp.getText();// ResultMsg Text 提示信息
			//userCoin = resp.getLong();// CoinCnt Long 个人金币数量
			//addUpCoin = resp.getLong();// AddupCoin Long 本次的累计金币数
			//resp.getInt();// TurnAngle Int 转盘停止的角度
			//resp.getInt();// TurnId Int 转盘停止的Id
			resp.getLoopMsgPack();// ColumnIconOrder Loop 每列停的时候Icon显示
			resp.getLoopMsgPack();// WinLines Loop 所有赢得位置显示序列
			//resp.getLong();// MiniUpdateTime Long 小游戏更新时间戳 服务器最后修改小游戏的时间
			//resp.getLoopMsgPack();// MinigameTurn Loop 小游戏转盘数据
			//randBet = resp.getInt();//randomBet	Int	随机的倍数	默认是0
			//resp.getInt();//middleAngle
			wheelSetId = resp.getInt();//SevenSlotsMachine.DEFAULT_WHEEL_SET_ID
			resp.getLoopMsgPack();
			resp.getInt();
			resp.getByte();
			resp.getByte();
			resp.getInt();
			resp.getByte();
			resp.getInt();
			resp.getInt();
		}
	}
}
