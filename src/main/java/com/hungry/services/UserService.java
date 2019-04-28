package com.hungry.services;

import java.sql.Timestamp;
import java.util.Date;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hungry.entities.AccessToken;
import com.hungry.entities.User;
import com.hungry.repositories.Debugger;
import com.hungry.repositories.UserRepository;
import com.hungry.services.util.CryptoMaster;
import com.hungry.services.util.SecurityMaster;
import com.hungry.services.util.TokenStatus;
import com.hungry.services.util.Type;

@Service
public class UserService {

	private static final Logger LOG = (Logger) LoggerFactory.getLogger(UserService.class);

	/**
	 * Autowired properties
	 */
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SecurityMaster securityMaster;
	@Autowired
	private CryptoMaster cryptoMaster;
	@Autowired
	public DbManagerService service;

	@Autowired
	private Debugger debugger;

	private static final int MAX_EXPIRES = 60;
	private static final int MIN_EXPIRES = 60;
	private static final int INVALID_USER_ID = 0;
	private static final String regExpr = "";

	private AccessToken accessToken;

	@Transactional
	public ResponseEntity<AccessToken> debug() {

		service.execution();
		/*
		 * List<User> users = debugger.findAll();
		 * 
		 * for (User user : users) { System.out.println(user); }
		 */
		/*
		 * userRepository.updateUserImage("liton", 1);
		 * System.out.println(debugger.findUserByPhoneNumber("01521515170"));
		 * 
		 * System.out.println(debugger.getLastInsertId());
		 * 
		 * Date date = new Date(); long time = date.getTime(); Timestamp ts = new
		 * Timestamp(time); accessToken = new
		 * AccessToken(securityMaster.token(TokenStatus.CREATED, 1, Type.USER),
		 * MAX_EXPIRES, ts);
		 */
		// return new ResponseEntity<AccessToken>(accessToken, HttpStatus.OK);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(accessToken);
	}

	@Transactional
	public ResponseEntity<AccessToken> register(User user) {

		
		try {
			LOG.debug( "register : user "+user.toString());
			
			//System.out.println(user);
			
			User result = userRepository.findUserByPhoneNumber(user.getPhone());			
			Date date = new Date();
			long time = date.getTime();
			Timestamp ts = new Timestamp(time);

			accessToken = null;
			
			if (result != null || user.getPassword().equals(null) || user.getPassword().equals("")) {
				LOG.debug( "register : findUserByPhoneNumber "+result.toString());
				return new ResponseEntity<AccessToken>(accessToken, HttpStatus.NOT_ACCEPTABLE);
			}
				
			//System.out.println(user);

			/**
			 * 
			 * phone number verification test future work
			 */

			user.setPassword(cryptoMaster.encrypt(user.getPassword()));
			user.setRegistrationDate(ts.toString());
			
			int maxUserId = userRepository.findMaxUserId();
			++maxUserId;
			
			LOG.debug( "register  maxUserId : "+maxUserId);
			accessToken = new AccessToken(securityMaster.token(TokenStatus.CREATED, maxUserId, Type.USER), MAX_EXPIRES, ts);
			user.setAccessToken(accessToken);
			userRepository.save(user);
			
		}catch (Exception e) {
			LOG.debug( "register  : "+e.getMessage());
			return new ResponseEntity<AccessToken>(accessToken, HttpStatus.NOT_ACCEPTABLE);
			// TODO: handle exception
		}
		
		return new ResponseEntity<AccessToken>(accessToken, HttpStatus.CREATED);
	}

	public ResponseEntity<AccessToken> authorizer(User user) {

		User result = userRepository.findUserByPhoneNumber(user.getPhone());
		accessToken = null;
		if (result == null)
			return new ResponseEntity<AccessToken>(accessToken, HttpStatus.NOT_FOUND);

		String password = result.getPassword();

		if (password.equals(null) || password.equals(""))
			return new ResponseEntity<AccessToken>(accessToken, HttpStatus.UNAUTHORIZED);

		password = cryptoMaster.decrypt(password);

		if (!password.equals(user.getPassword()))
			return new ResponseEntity<AccessToken>(accessToken, HttpStatus.UNAUTHORIZED);

		String token = result.getAccessToken().getAccessToken();
		int expires = (int) result.getAccessToken().getExpire();
		Timestamp timestamp = (Timestamp) result.getAccessToken().getAccessDate();

		Date date = new Date();
		long time = date.getTime();
		// Timestamp timestamp = new Timestamp(time);
		accessToken = new AccessToken(token, expires, timestamp);

		return new ResponseEntity<AccessToken>(accessToken, HttpStatus.OK);
	}

}
