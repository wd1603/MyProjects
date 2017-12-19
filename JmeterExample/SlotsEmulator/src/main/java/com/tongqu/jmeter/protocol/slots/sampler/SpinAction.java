package com.tongqu.jmeter.protocol.slots.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.tongqu.base.msg.LoopMsgPack;
import com.tongqu.base.msg.MsgPack;
import com.tongqu.base.msg.NetCmd;
import com.tongqu.base.msg.NetCmdClient;
import com.tongqu.jmeter.protocol.slots.network.ReqMsg;
import com.tongqu.jmeter.protocol.slots.network.RespMsg;
import com.tongqu.jmeter.protocol.slots.network.SlotsClient;

public class SpinAction implements JavaSamplerClient {

	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private SpinReq req;
	private SpinResp resp;
	private SampleResult results;
	
	private JMeterVariables vars;
	private SlotsClient client;
	private long slotUpdateTime;
	private String userId;
	
	private int bet;
	
	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();
		params.addArgument("bet", "${bet}");
		return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext ctx) {
		//发送请求
		resp.readParamFrom(client.request(req));
		
		//修改状态
		if (resp.result == 1) {
			if (resp.miniGameId > 0) {
				vars.put("state", "2");
				log.info(userId + "spin成功, 下次开始choose.");
			} else {
				vars.put("state", "1");
				log.info(userId + "spin成功, 下次开始spin.");
			}
			results.setSuccessful(true);
		} else {
			log.info("Spin失败，原因(resp.message)=" + resp.message);
			results.setSuccessful(false);
		}
		
		return results;
	}

	@Override
	public void setupTest(JavaSamplerContext ctx) {
		this.req = new SpinReq();
		this.resp = new SpinResp();
		this.results = new SampleResult();
		
		this.vars = JMeterContextService.getContext().getVariables();
		this.client = (SlotsClient) this.vars.getObject("SlotsClient");
		this.slotUpdateTime = Long.parseLong(vars.get("slotUpdateTime"));
		this.userId = vars.get("userId");
		
		this.bet = ctx.getIntParameter("bet");
	}

	@Override
	public void teardownTest(JavaSamplerContext ctx) {
		this.req = null;
		this.resp = null;
		this.results = null;
		this.vars = null;
		this.client = null;
		this.slotUpdateTime = 0;
		this.bet = 0;
	}

	private class SpinReq extends ReqMsg {
		@Override
		public int getMsgId() {
			return NetCmd.REQ | NetCmdClient.SushiSlots.SPIN;
		}
		
		@Override
		protected void writeParamTo(MsgPack msgPack) {
			msgPack.putByte(1);
			msgPack.putInt(1);
			msgPack.putInt(1);
			msgPack.putInt(bet);
			msgPack.putLong(slotUpdateTime);
			msgPack.putText("");
		}
	}
	private class SpinResp extends RespMsg {
		int result, miniGameId, freeTimes, randBet, wheelSetId;
		long userCoin, addUpCoin, winCoin;
		String message;
		LoopMsgPack iconReplaceMsg, bonusOptMsg;
		
		@Override
		protected void readParamFrom(MsgPack msgPack) {
			msgPack.getLong();// TimeStamp Long Spin时间戳
			result = msgPack.getByte();// 0失败 1成功
			if (result == 1) {
				message = msgPack.getText();// 提示信息
				winCoin = msgPack.getLong();// WinCoin Long 本次赢金数量
				addUpCoin = msgPack.getLong();// AddupCoin Long 本次的累计金币数
				userCoin = msgPack.getLong();// CoinCnt Long 当前金币数量
				msgPack.getLong();// WinExp Long 本次获得经验数量
				msgPack.getInt();// ExpLv Int 个人的经验等级
				msgPack.getLong();// SelfExp Long 个人在该等级的经验量
				msgPack.getLong();// CurrLvTotalExp Long 当前等级总经验漕大小
				msgPack.getInt();// WinWireCnt Int 中的线数
				msgPack.getInt();// WinStatus Int 赢的状态
				miniGameId = msgPack.getInt();// 默认为0,1转盘,2freeSpin状态下中龙蛋  // SlotMiniGameID Int 触发的小游戏id
				freeTimes = msgPack.getInt();// RemainFreeSpinCnt Int 剩余free spin数量
				msgPack.getInt();// AllFreeSpinCnt Int 本轮Spin中free spin总次数 
				msgPack.getLoopMsgPack();// ColumnIconOrder Loop 每列停的时候Icon显示
				msgPack.getLoopMsgPack();// WinLines Loop 所有赢得位置显示序列
				msgPack.getByte();
				iconReplaceMsg = msgPack.getLoopMsgPack();// spin随机芥末位置
				bonusOptMsg = msgPack.getLoopMsgPack();// bonus随机芥末位置
				wheelSetId = msgPack.getInt();//curWheelSetInfo.getId()
				msgPack.getLoopMsgPack();
				msgPack.getInt();
				msgPack.getByte();
				msgPack.getByte();
				msgPack.getInt();
				msgPack.getByte();
				msgPack.getInt();
				msgPack.getInt();
			} else {
				message = msgPack.getText();// 提示信息
				winCoin = msgPack.getLong();// WinCoin Long 本次赢金数量
				addUpCoin = msgPack.getLong();// AddupCoin Long 本次的累计金币数
				userCoin = msgPack.getLong();// CoinCnt Long 当前金币数量
			}
		}
	}
}
