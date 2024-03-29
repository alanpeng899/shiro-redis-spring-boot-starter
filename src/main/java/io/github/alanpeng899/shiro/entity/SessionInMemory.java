package io.github.alanpeng899.shiro.entity;

import java.util.Date;

import org.apache.shiro.session.Session;

/**
* @author pengyq<br>
*  创建时间：2019年5月29日<br> 
*  类说明：内存session对象
*/
public class SessionInMemory {
	
	private Session session;
    private Date createTime;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
