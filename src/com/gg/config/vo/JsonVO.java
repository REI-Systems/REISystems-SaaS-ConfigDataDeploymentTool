package com.gg.config.vo;

/**
 * 
 * @author Shubhangi Shinde, VijayaLaxmi
 *
 */
public class JsonVO {
	private SrcVO srcVo;
	private TargetVO targeteVO;
	private SrcToTargetVO srcToTargetVO;
	private MigrationItemVO migrationItemVO;

	public SrcVO getSrcVo() {
		return srcVo;
	}

	public void setSrcVo(SrcVO srcVo) {
		this.srcVo = srcVo;
	}

	public TargetVO getTargeteVO() {
		return targeteVO;
	}

	public void setTargeteVO(TargetVO targeteVO) {
		this.targeteVO = targeteVO;
	}

	public SrcToTargetVO getSrcToTargetVO() {
		return srcToTargetVO;
	}

	public void setSrcToTargetVO(SrcToTargetVO srcToTargetVO) {
		this.srcToTargetVO = srcToTargetVO;
	}

	public MigrationItemVO getMigrationItemVO() {
		return migrationItemVO;
	}

	public void setMigrationItemVO(MigrationItemVO migrationItemVO) {
		this.migrationItemVO = migrationItemVO;
	}

}
