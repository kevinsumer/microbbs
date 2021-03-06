package me.micro.bbs.user.support;

import me.micro.bbs.security.Role;
import me.micro.bbs.security.support.PermissionService;
import me.micro.bbs.security.support.RoleService;
import me.micro.bbs.user.User;
import me.micro.bbs.user.UserForm;
import me.micro.bbs.user.UserProfile;
import me.micro.bbs.user.UserSocial;
import me.micro.bbs.util.ShortUUID;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 *
 *
 * Created by microacup on 2016/11/21.
 */
/*@Service*/
@Transactional
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserSocialRepository socialRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("没有找到该用户");
        }

        Set<Role> roles = roleService.findByUser(user.getId());
        user.setRoles(roles);
        return user;
    }

    public User findByNick(String nick) {
        return userRepository.findByNick(nick);
    }

    public User findOne(Long id) {
        return userRepository.findOne(id);
    }

    public User addUser(UserForm user) {
        // TODO 添加ROLE

        return null;
    }

    // 第三方登录，新注册
    public User newUserAndUserSocial(UserForm userForm) {
        Date now = new Date();

        // 确保username，nick唯一, 处理后的再相同就看人品了, 这里故意减少一次查询
        int exists = userRepository.countByUsernameOrNick(userForm.getUsername(), userForm.getNick());
        if (exists > 0) {
            userForm.setUsername(userForm.getUsername() + getRandom(5));
            userForm.setNick(userForm.getNick() + getRandom(5));
        }

        User user = new User();
        user.setUsername(userForm.getUsername());
        user.setNick(userForm.getNick());
        user.setInfo(userForm.getInfo());
        user.setCellphone(userForm.getCellphone());
        user.setEmail(userForm.getEmail());
        user.setName(userForm.getName());
        user.setGender(userForm.getGender());
        user.setChannel(userForm.getChannel());
        user.setRegisterTime(now);
        user.setClient(userForm.getClient());
        String openId = generateOpenId();
        user.setOpenId(openId);
        user.setPassword(passwordEncoder.encode(user.getUsername()));

        // 角色
        Role userRole = roleService.findByCode(RoleService.BUILDIN_USER);
        Set<Role> roles = new HashSet<Role>(){{add(userRole);}};
        user.setRoles(roles);
        User saved = userRepository.save(user);

        // 资料
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(user.getId());
        profileRepository.save(userProfile);

        UserSocial userSocial = new UserSocial();
        userSocial.setNickname(userForm.getOpenNickname());
        userSocial.setCreateTime(now);
        userSocial.setOpenid(userForm.getOpenid());
        userSocial.setUser(saved);
        userSocial.setSource(userForm.getSource());
        socialRepository.save(userSocial);
        return user;
    }

    private String generateOpenId() {
        try {
            return new MD5Generator().generateValue();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }

        return ShortUUID.uuid();
    }

    /**
     * 生成随机验证码
     *
     * @param size 验证码长度
     * @return
     */
    public static String getRandom(int size){
        Random rd = new Random();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<size;i++){
            sb.append(Math.abs(rd.nextInt()%10));
        }
        return sb.toString();
    }

}
