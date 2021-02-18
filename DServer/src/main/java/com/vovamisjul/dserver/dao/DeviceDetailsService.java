package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.models.DeviceDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class DeviceDetailsService implements UserDetailsService {

    @Autowired
    private DeviceDao deviceDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DeviceDetails user = deviceDao.getDeviceDetails(username);
        if (user==null) {
            throw new UsernameNotFoundException(username);
        }
        return user;
    }
}
