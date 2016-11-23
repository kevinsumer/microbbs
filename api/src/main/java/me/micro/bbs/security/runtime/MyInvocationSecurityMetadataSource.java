package me.micro.bbs.security.runtime;

import me.micro.bbs.security.Permission;
import me.micro.bbs.security.support.PermissionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Service
public class MyInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    Logger log = Logger.getLogger(MyInvocationSecurityMetadataSource.class);

    @Autowired
    private PermissionService permissionService;

    private HashMap<String, Collection<ConfigAttribute>> map = null;

    /**
     * 加载资源，初始化资源变量
     */
    public void loadResourceDefine() {
        map = new HashMap<>();
        Collection<ConfigAttribute> array;
        ConfigAttribute cfg;
        List<Permission> permissions = permissionService.findAll();
        for (Permission permission : permissions) {
            array = new ArrayList<>();
            cfg = new SecurityConfig(permission.getCode());
            array.add(cfg);
            map.put(permission.getUrl(), array);
        }
        log.info("security info load success!!");
    }


    /**
     * 根据路径获取访问权限的集合接口
     *
     * @param object
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object)
            throws IllegalArgumentException {
        if (map == null) loadResourceDefine();
        HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();
        AntPathRequestMatcher matcher;
        String resUrl;
        for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext(); ) {
            resUrl = iter.next();
            matcher = new AntPathRequestMatcher(resUrl);
            if (matcher.matches(request)) {
                return map.get(resUrl);
            }
        }
        return null;
    }

    /**
     * @return
     */
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null; // XXX 全都支持，这里就不写了。
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
