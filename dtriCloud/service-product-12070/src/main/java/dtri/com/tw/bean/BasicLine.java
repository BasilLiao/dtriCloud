package dtri.com.tw.bean;

//============================
//BasicLine：解析 basic & items 的共用結構
//============================
public class BasicLine {
	private String material;
	private String qty;
	private String process;
	private String level;
	private String bisgid; // 舊 basic 才用得到，新 basic 不帶群組資訊

	// 舊資料格式：mat_qty_process_level_bisgid
	public static BasicLine parseOld(String raw) {
		// limit = 5 => 避免後面欄位內含 "_" 被拆太多段
		String[] parts = raw.split("_", 5);
		// 新建
		BasicLine line = new BasicLine();
		line.material = parts.length > 0 ? parts[0] : "";
		line.qty = parts.length > 1 ? parts[1] : "1";
		line.process = parts.length > 2 ? parts[2] : "ASM";
		line.level = parts.length > 3 ? parts[3] : "1";
		line.bisgid = parts.length > 4 ? parts[4] : null;

		return line;
	}

	// 新資料格式：mat_qty_process_level
	public static BasicLine parseNew(String raw) {
		String[] parts = raw.split("_", 4);
		// 新建
		BasicLine line = new BasicLine();
		line.material = parts.length > 0 ? parts[0] : "";
		line.qty = parts.length > 1 ? parts[1] : "1";
		line.process = parts.length > 2 ? parts[2] : "ASM";
		line.level = parts.length > 3 ? parts[3] : "1";

		return line;
	}

// basic 寫回用（不帶 bisgid）
	public String toBasicString() {
		return material + "_" + qty + "_" + process + "_" + level;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public String getQty() {
		return qty;
	}

	public void setQty(String qty) {
		this.qty = qty;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getBisgid() {
		return bisgid;
	}

	public void setBisgid(String bisgid) {
		this.bisgid = bisgid;
	}

}