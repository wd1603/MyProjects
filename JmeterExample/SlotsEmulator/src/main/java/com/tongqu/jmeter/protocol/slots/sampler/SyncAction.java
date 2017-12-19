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

/**
 * 同步指令.
 * @author WYR
 */
public class SyncAction implements JavaSamplerClient {

	private static final Logger log = LoggingManager.getLoggerForClass();
	
	
	private SyncReq req;
	private SyncResp resp;
	private SampleResult results;
	
	private SlotsClient client;
	private JMeterVariables vars;
	private String userId;
	
	@Override
	public Arguments getDefaultParameters() {
		return null;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext ctx) {
		//发送请求
		resp.readParamFrom(client.request(req));
		
		//变更状态
		vars.put("slotUpdateTime", String.valueOf(resp.slotUpdateTime));
		if (resp.miniGameId > 0) {
			vars.put("state", "2");
			log.info(userId + "同步成功, 下次开始choose.");
		} else {
			vars.put("state", "1");
			log.info(userId + "同步成功, 下次开始spin.");
		}
		
		//返回结果
		results.setSuccessful(true);
		return results;
	}

	@Override
	public void setupTest(JavaSamplerContext ctx) {
		//使用参数
		this.req = new SyncReq();
		this.resp = new SyncResp();
		this.results = new SampleResult();
		
		this.vars = JMeterContextService.getContext().getVariables();
		this.client = (SlotsClient) this.vars.getObject("SlotsClient");
		this.userId = this.vars.get("userId");
	}

	@Override
	public void teardownTest(JavaSamplerContext arg0) {
		this.req = null;
		this.resp = null;
		this.client = null;
		this.vars = null;
		this.userId = null;
	}

	private class SyncReq extends ReqMsg {
		@Override
		public int getMsgId() {
			return NetCmd.REQ | NetCmdClient.SushiSlots.ROOM_TABLE_SYNC;
		}

		@Override
		protected void writeParamTo(MsgPack msgPack) {
			msgPack.putByte(1);
			msgPack.putInt(1);
			msgPack.putInt(1);
			msgPack.putInt(1);
			msgPack.putText("");
		}
	}
	private class SyncResp extends RespMsg {
		int miniGameId, freeTimes, randBet, wheelSetId;
		long userCoin, addUpCoin, winCoin, slotUpdateTime;
		@Override
		protected void readParamFrom(MsgPack resp) {
			resp.getLong();// TimeStamp Long spin时间戳
			resp.getInt();// RoomId Int 房间id
			resp.getText();// RoomName Text 当前房间名字
			resp.getInt();// RoomAllWire Int 房间总线数
			resp.getText();// 昵称
			resp.getText();// 头像url
			miniGameId = resp.getInt();// 默认为0 转盘1 // SlotMiniGameID Int 触发的小游戏id
			freeTimes = resp.getInt();// RemainFreeSpinCnt Int 剩余free spin数量
			userCoin = resp.getLong();// CoinCnt Long 个人金币数量
			addUpCoin = resp.getLong();// AddupCoin Long 本次的累计金币数
			resp.getInt();// ExpLv Int 个人的经验等级
			resp.getLong();// SelfExp Long 个人在该等级的经验量
			resp.getLong();// CurrLvTotalExp Long 当前等级总经验漕大小
			resp.getLoopMsgPack();// VisibleIconColumn Loop slot机器每列的图标数
			resp.getLoopMsgPack();// MachineStatusList Loop 本台机器有几种状态
			resp.getLoopMsgPack();// TableBetList Loop 可用的下注列表
			resp.getInt();// 如果下注数为0,说明玩家还没下注,默认第六个bet值 // 当前的下注数 // TableCurBet Int 你当前的下注数bets
			slotUpdateTime = resp.getLong();// SlotUpdateTime Long 老虎机更新时间戳
			winCoin = resp.getLong();// WinCoin Long 本次赢金数量
			resp.getInt();// WinStatus Int 赢的状态 1Mega Win 2Big Win
			resp.getLoopMsgPack();// ColumnIconOrder Loop 每列停的时候Icon显示
			resp.getLoopMsgPack();// WinLines Loop 所有赢得位置显示序列
			resp.getByte();
			resp.getLoopMsgPack();// 轮子图标替换
			resp.getLoopMsgPack();// MinigameTurn Loop 小游戏转盘数据
			resp.getInt();
			resp.getByte();// 奖券发放机-配置开关
			resp.getByte();// 奖券发放机-是否睡觉
			resp.getInt();// 奖券发放机-当日剩余数量
			resp.getByte();// 奖券发放机-上次转到图标id
			resp.getInt();// 奖券发放机-上次奖励数量
			resp.getInt();// 奖券发放机-奖券数量
			resp.getLong();// 奖券发放机-石化totalbet界限
		}
	}
}
