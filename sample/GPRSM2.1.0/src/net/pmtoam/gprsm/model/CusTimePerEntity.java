package net.pmtoam.gprsm.model;

/**
 * 用户查询自定义时间段实体类
 * 
 * @author 王月星
 *
 */
public class CusTimePerEntity {

	private int userId;       // 用户ID(程序将来可扩展)
	private int startTime;    // 查询时间段的起始时间
	private int endTime;      // 查询时间段的结束时间

	public CusTimePerEntity() {
		super();
	}

	public CusTimePerEntity(int userId, int startTime, int endTime) {
		super();
		this.userId = userId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "CusTimePerEntity [userId=" + userId + ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}

	public int getUserId() {
		return userId;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

}
