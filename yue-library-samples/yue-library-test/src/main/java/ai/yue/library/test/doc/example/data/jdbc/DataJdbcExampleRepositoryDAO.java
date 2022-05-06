package ai.yue.library.test.doc.example.data.jdbc;

import ai.yue.library.data.jdbc.dao.AbstractRepository;
import ai.yue.library.data.jdbc.ipo.PageIPO;
import ai.yue.library.data.jdbc.vo.PageVO;
import ai.yue.library.test.dataobject.jdbc.TableExampleTestDO;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author  ylyue
 * @version 创建时间：2019年3月12日
 */
@Repository
public class DataJdbcExampleRepositoryDAO extends AbstractRepository<TableExampleTestDO> {

	@Override
	protected String tableName() {
		return "table_example_test";
	}
	
	/**
	 * 删除
	 * @param name
	 */
	public void deleteByName(String name) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("name", name);
		db.delete(tableName(), paramJson);
	}
	
	/**
	 * 删除-批量
	 * @param paramJsons
	 */
	public void deleteBatch(JSONObject[] paramJsons) {
		db.deleteBatch(tableName(), paramJsons);
	}
	
	/**
	 * 更新-ByName
	 * @param paramJson
	 */
	public void updateByName(JSONObject paramJson) {
		String[] conditions = {"name"};
		long updateRowsNumber = db.update(tableName(), paramJson, conditions);
		int expectedValue = 1;
		db.updateAndExpectedEqual(updateRowsNumber, expectedValue);
	}
	
	/**
	 * 更新-排序
	 * @param id
	 * @param move
	 */
	public void updateSort(Long id, Integer move) {
		String uniqueKeys = "name";
		db.updateSort(tableName(), id, move, uniqueKeys);
	}
	
	/**
	 * 列表
	 * @param id
	 * @return
	 */
	public List<JSONObject> list(Long id) {
		// 1. 处理参数
		JSONObject paramJson = new JSONObject();
		paramJson.put("id", id);

		// 2. 查询SQL
		String sql = "";

		// 3. 返回结果
		return db.queryForList(sql, paramJson);
	}
	
	/**
	 * 分页
	 * @param pageIPO
	 * @return
	 */
	public PageVO pageSql(PageIPO pageIPO) {
		String querySql = "";
		return db.pageSql(querySql, pageIPO);
	}
	
}
