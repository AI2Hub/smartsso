package openjoe.smart.sso.server.service;

import openjoe.smart.sso.server.entity.User;
import openjoe.smart.stage.core.entity.Page;
import openjoe.smart.stage.mybatisplus.service.BaseService;

import java.util.Collection;
import java.util.List;

/**
 * 用户服务接口
 * 
 * @author Joe
 */
public interface UserService extends BaseService<User> {
	
	/**
	 * 启用禁用操作
	 * @param isEnable 是否启用
	 * @param idList 用户ID集合
	 * @return
	 */
	public void enable(Boolean isEnable, List<Long> idList);
	
	/**
	 * 重置密码
	 * @param password 初始化密码(已加密)
	 * @param idList 
	 */
	public void resetPassword(String password, List<Long> idList);

	/**
	 * 查询分页列表
	 * @param account 登录名
	 * @param name 姓名
	 * @param officeId 机构ID
	 * @return
	 */
	public Page<User> selectPage(String account, String name, Long officeId, Long current, Long size);
	
	/**
	 * 根据登录名和应用ID查询
	 * @param account 登录名
	 * @return
	 */
	public User selectByAccount(String account);
	
	/**
	 * 更新密码
	 * 
	 * @param id
	 *            用户ID
	 * @param newPassword
	 *            新密码
	 * @return
	 */
	public void updatePassword(Long id, String newPassword);

	public void deleteByIds(Collection<Long> idList);
}
